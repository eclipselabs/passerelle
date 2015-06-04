/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.message.requestreply;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.IMessageListener;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.ReceiverChannel;
import com.isencia.util.BlockingReaderQueue;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;

/**
 * First trial to implement a request/reply feature in our UME. Currently, this
 * should be able to route responses to the correct sender channel, matching the
 * receiver channel from which the request was received. But we don't have
 * correct ordering yet... REMARK: Current implementation uses VERY simplistic
 * generation of correlation ID, resulting in unique IDs during an application's
 * life-time, but not across application relaunches.
 * 
 * @author erwin
 */
public class RequestReplier implements IRequestReplier {

  class RequestListener implements IMessageListener {
    public void acceptMessage(Object request, IReceiverChannel source) throws InterruptedException, Exception {
      if (logger.isTraceEnabled()) logger.trace("acceptMessage() - entry - Message: " + request + " Source: " + source);

      // try to find channel pair that corresponds to the source
      ReceiverSenderChannelPair pair = channels.get(source);
      if (pair != null) {
        IMessage msg = new RequestMessage(request);
        responseChannels.put(msg.getCorrelationID(), pair);
        queue.put(msg);
      } else
        throw new IllegalArgumentException("Unrecognized source " + source);

      if (logger.isTraceEnabled()) logger.trace("acceptMessage() - exit");
    }

    public void sourceOpened(IReceiverChannel source) {
      if (logger.isTraceEnabled()) logger.trace("sourceOpened() - entry - source :" + source);

      synchronized (openChannels) {
        openChannels.add(source);
        if (!open) logger.debug("sourceOpened() - RequestReplier opened");

        open = true;
      }

      if (logger.isTraceEnabled()) logger.trace("sourceOpened() - exit");
    }

    public void sourceClosed(IReceiverChannel source) {
      if (logger.isTraceEnabled()) logger.trace("sourceClosed() - entry - source :" + source);

      synchronized (openChannels) {
        openChannels.remove(source);
        open = !((openChannels.size() <= 0) && (autoClose || isClosing));
      }

      // If all channels are closed, perform a trigger on the queue
      // to release blocking get calls
      if (!isOpen()) {
        isClosing = false;
        queue.trigger();
        logger.debug("sourceClosed() - RequestReplier closed");
      }

      if (logger.isTraceEnabled()) logger.trace("sourceClosed() - exit - Open = " + open);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(RequestReplier.class);

  // Flag used to indicate whether the receiver should close itself
  // when all its channels have closed.
  private boolean autoClose = false;
  // map containing the channel pairs,
  // with the rcv channel as key
  private Map<IReceiverChannel, ReceiverSenderChannelPair> channels = null;

  // flag used to simulate simple state machine during closing phase
  private boolean isClosing = false;
  private boolean open = false;
  private Set<IReceiverChannel> openChannels = null;
  private BlockingReaderQueue queue = null;
  private RequestListener listener = null;

  // data structure to lookup response destination,
  // based on request msg correlation ID
  // contains channel pairs with correlationID as key
  private Map<Object, ReceiverSenderChannelPair> responseChannels = null;

  public RequestReplier() {
    this(false);
  }

  /**
   * Creates a new RequestReplier object.
   * 
   * @param autoClose Flag used to indicate whether the receiver should close
   *          itself automatically when all its receiver channels have closed.
   */
  public RequestReplier(boolean autoClose) {
    queue = new BlockingReaderQueue(new FIFOQueue());
    channels = new HashMap<IReceiverChannel, ReceiverSenderChannelPair>();
    openChannels = new HashSet<IReceiverChannel>();
    responseChannels = new HashMap<Object, ReceiverSenderChannelPair>();
    this.autoClose = autoClose;
    listener = new RequestListener();
  }

  public boolean hasMessage() {
    return !queue.isEmpty();
  }

  public IMessage receiveRequest() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) {
      logger.trace("receiveRequest() - entry");
    }
    if (isOpen() || hasMessage()) {
      Object msg = null;
      try {
        msg = queue.get();
        RequestMessage result = (RequestMessage) msg;
        if (logger.isTraceEnabled()) {
          logger.trace("receiveRequest() - exit");
        }
        return result;
      } catch (EmptyQueueException e) {
        throw new NoMoreMessagesException(e.toString());
      } catch (ClassCastException e) {
        logger.error("Invalid message in queue, dropped " + msg, e);
        // try to get next msg
        return receiveRequest();
      }
    } else
      throw new NoMoreMessagesException("No more messages");
  }

  public boolean sendResponse(Object response, Object correlationID) {
    // try to find destination for the response
    ReceiverSenderChannelPair destPair = responseChannels.get(correlationID);
    if (destPair != null && destPair.getSenderChannel() != null) {
      try {
        destPair.getSenderChannel().sendMessage(response);
        // remove the entry for the correlatedRequest
        // which we maintained just to wait for the
        // response
        responseChannels.remove(correlationID);
        return true;
      } catch (Exception e) {
        logger.error("", e);
        return false;
      }
    } else
      return false;
  }

  public void addChannelPair(ReceiverSenderChannelPair channelPair) {
    if (logger.isTraceEnabled()) {
      logger.trace("addChannelPair() - entry - channel :" + channelPair);
    }
    channelPair.addListener(listener);
    synchronized (channels) {
      channels.put(channelPair.getReceiverChannel(), channelPair);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("addChannelPair() - exit");
    }
  }

  /**
   * Remark that this method removes the channel pair immediately! So any late
   * response messages, arriving afterwards will no longer be sent!!!
   */
  public boolean removeChannelPair(ReceiverSenderChannelPair channelPair) {
    if (logger.isTraceEnabled()) {
      logger.trace("removeChannelPair() - entry - channelPair :" + channelPair);
    }
    synchronized (channels) {
      try {
        ((ReceiverChannel) channelPair.getReceiverChannel()).interrupt();
        boolean result = (channels.remove(channelPair.getReceiverChannel()) != null);
        if (logger.isTraceEnabled()) {
          logger.trace("removeChannelPair - exit - result :" + result);
        }
        channelPair.removeListener(listener);
        return result;
      } catch (UnsupportedOperationException e) {
        logger.error("removeChannelPair()", e);
        return false;
      }
    }
  }

  public Collection<ReceiverSenderChannelPair> getChannelPairs() {
    return channels.values();
  }

  public void open() {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    synchronized (channels) {
      // (re)open all channels
      Iterator<ReceiverSenderChannelPair> chItr = channels.values().iterator();
      while (chItr.hasNext()) {
        ReceiverSenderChannelPair pair = chItr.next();
        try {
          pair.open();
        } catch (ChannelException e) {
          logger.error("open() - Error opening channelPair " + pair, e);
        }
      }
    }

    // set state immediately
    // to ensure blocking getMessage() if no channels are registered
    // and also already during the interval it takes for the channels
    // to call sourceOpened() to indicate that they are ready
    open = true;
    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public boolean isOpen() {
    synchronized (openChannels) {
      return open;
    }
  }

  public void close() {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    synchronized (channels) {
      if (channels.size() > 0) {
        Iterator<ReceiverSenderChannelPair> chItr = channels.values().iterator();
        while (chItr.hasNext()) {
          ReceiverSenderChannelPair pair = chItr.next();
          try {
            pair.close();
          } catch (ChannelException e) {
            logger.error("open() - Error closing channelPair " + pair, e);
          }
        }
      } else {
        // set state immediately
        open = false;
      }
    }

    isClosing = true;
    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

}
