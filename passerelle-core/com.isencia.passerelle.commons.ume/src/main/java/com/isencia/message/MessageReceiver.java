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
package com.isencia.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.util.BlockingReaderQueue;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;

/**
 * Base class for asynchronous message receivers, using an intermediate
 * synchronized queue. TODO: put a max size on the queue.
 * 
 * @version 1.0
 * @author erwin
 */
public class MessageReceiver implements IMessageReceiver {

  private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

  // Flag used to indicate whether the receiver should close itself
  // when all its channels have closed.
  private boolean autoClose = false;
  private Collection<IReceiverChannel> channels = null;

  // flag used to simulate simple state machine during closing phase
  private boolean isClosing = false;
  private boolean open = false;
  private Set<IReceiverChannel> openChannels = null;
  private BlockingReaderQueue queue = null;

  /**
   * Creates a new MessageReceiver object.
   */
  public MessageReceiver() {
    this(false);
  }

  /**
   * Creates a new MessageReceiver object.
   * 
   * @param autoClose Flag used to indicate whether the receiver should close
   *          itself automatically when all its channels have closed.
   */
  public MessageReceiver(boolean autoClose) {
    queue = new BlockingReaderQueue(new FIFOQueue());
    channels = new ArrayList<IReceiverChannel>();
    openChannels = new HashSet<IReceiverChannel>();
    this.autoClose = autoClose;
  }

  public Collection<IReceiverChannel> getChannels() {
    return channels;
  }

  public Object getMessage() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - entry");
    }
    if (isOpen() || hasMessage())
      try {
        Object result = queue.get();
        if (logger.isTraceEnabled()) {
          logger.trace("getMessage() - exit");
        }
        return result;
      } catch (EmptyQueueException e) {
        throw new NoMoreMessagesException(e.toString());
      }
    else
      throw new NoMoreMessagesException("No more messages");
  }

  public boolean isOpen() {
    synchronized (openChannels) {
      return open;
    }
  }

  public void acceptMessage(Object message, IReceiverChannel source) {
    if (logger.isTraceEnabled()) logger.trace("acceptMessage() - entry - Message: " + message);

    queue.put(message);
    if (logger.isTraceEnabled()) logger.trace("acceptMessage() - exit");
  }

  public void addChannel(IReceiverChannel channel) {
    if (logger.isTraceEnabled()) {
      logger.trace("addChannel() - entry - channel :" + channel);
    }
    channel.addListener(this);
    synchronized (channels) {
      channels.add(channel);
    }

    if (isOpen()) {
      // also open the channel immediately
      try {
        channel.open();
      } catch (ChannelException e) {
        logger.error("addChannel() - Error opening channel " + channel, e);
        channel.removeListener(this);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("addChannel() - exit");
    }
  }

  public void close() {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    synchronized (channels) {
      if (getChannels().size() > 0) {
        Iterator<IReceiverChannel> chItr = channels.iterator();
        while (chItr.hasNext()) {
          IReceiverChannel ch = chItr.next();
          try {
            ch.close();
          } catch (ChannelException e) {
            logger.error("close() - Error closing channel", e);
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

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageReceiver#hasMessage()
   */
  public boolean hasMessage() {
    return !queue.isEmpty();
  }

  public void open() {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    synchronized (channels) {
      // (re)open all channels
      Iterator<IReceiverChannel> chItr = getChannels().iterator();
      while (chItr.hasNext()) {
        IReceiverChannel ch = chItr.next();
        try {
          ch.open();
        } catch (ChannelException e) {
          logger.error("open() - Error opening channel " + ch, e);
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

  public boolean removeChannel(IReceiverChannel channel) {
    if (logger.isTraceEnabled()) {
      logger.trace("removeChannel() - entry - channel :" + channel);
    }
    synchronized (channels) {
      try {
        ((ReceiverChannel) channel).interrupt();
        boolean result = channels.remove(channel);
        if (logger.isTraceEnabled()) {
          logger.trace("removeChannel - exit - result :" + result);
        }
        return result;
      } catch (UnsupportedOperationException e) {
        logger.error("removeChannel()", e);
        return false;
      }
    }
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
      logger.debug("sourceClosed() - Message receiver closed");
    }

    if (logger.isTraceEnabled()) logger.trace("sourceClosed() - exit - Open = " + open);
  }

  public void sourceOpened(IReceiverChannel source) {
    if (logger.isTraceEnabled()) logger.trace("sourceOpened() - entry - source :" + source);

    synchronized (openChannels) {
      openChannels.add(source);
      if (!open) logger.debug("sourceOpened() - Message receiver opened");

      open = true;
    }

    if (logger.isTraceEnabled()) logger.trace("sourceOpened() - exit");
  }
}