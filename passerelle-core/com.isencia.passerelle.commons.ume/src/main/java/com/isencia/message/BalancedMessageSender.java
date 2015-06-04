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
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;
import com.isencia.util.IQueue;

/**
 * Base class for asynchronous message senders, using an intermediate
 * synchronized queue. This allows the send side clients to add new msgs
 * independently of the overhead of the actual sending operation. TODO: put a
 * max size on the queue.
 * 
 * @version 1.0
 * @author erwin
 */
public class BalancedMessageSender implements IMessageSender, IMessageProvider {

  private static final Logger logger = LoggerFactory.getLogger(BalancedMessageSender.class);
  private Collection<ISenderChannel> channels = null;
  private boolean open = false;
  private IQueue queue = null;

  /**
   * Creates a new BalancedMessageSender object.
   */
  public BalancedMessageSender() {
    queue = new FIFOQueue();
    channels = new ArrayList<ISenderChannel>();
  }

  public Collection<ISenderChannel> getChannels() {
    return channels;
  }

  public Object getMessage() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("getMessage() - entry");

    synchronized (queue) {
      try {
        Object object = queue.get();
        if (logger.isTraceEnabled()) logger.trace("getMessage() - exit - Result :" + object);

        return object;
      } catch (EmptyQueueException e) {
        throw new NoMoreMessagesException(e.toString());
      }
    }
  }

  public boolean isOpen() {
    return open;
  }

  public void addChannel(ISenderChannel newChannel) {
    synchronized (channels) {
      channels.add(newChannel);
      newChannel.addProvider(this);
    }
  }

  public void close() {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    synchronized (channels) {
      open = false;

      Iterator<ISenderChannel> chItr = channels.iterator();
      while (chItr.hasNext()) {
        ISenderChannel ch = chItr.next();
        try {
          ch.close();
        } catch (ChannelException e) {
          logger.error("close() - Error closing channel", e);
        }
      }
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  public synchronized boolean hasMessage() {
    synchronized (queue) {
      return !queue.isEmpty();
    }
  }

  public void open() {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    synchronized (channels) {
      // (re)open all channels
      Iterator<ISenderChannel> chItr = getChannels().iterator();
      while (chItr.hasNext()) {
        ISenderChannel ch = chItr.next();
        try {
          ch.open();
        } catch (ChannelException e) {
          logger.error("open() - Error opening channel", e);
        }
      }

      open = true;
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public boolean removeChannel(ISenderChannel newChannel) {
    if (logger.isTraceEnabled()) logger.trace("removeChannel() - entry - channel :" + newChannel);
    try {
      synchronized (channels) {
        if (logger.isTraceEnabled()) logger.trace("removeChannel() - exit");
        return channels.remove(newChannel);
      }
    } catch (UnsupportedOperationException e) {
      logger.error("removechannel()", e);
      return false;
    }
  }

  /**
   * For this asynchronous message sender implementation, the sendMessage just
   * puts msgs on an intermediate queue. A separate thread is continuously
   * monitoring the queue and grabbing msgs and sending them out.
   * 
   * @see IMessageSender#sendMessage(Object)
   */
  public boolean sendMessage(Object message) {
    if (logger.isTraceEnabled()) logger.trace("sendMessage() - entry - Message: " + message);

    boolean ret = false;
    if (!open) throw new IllegalStateException("sendMessage() - MessageSender is not open");

    synchronized (queue) {
      ret = queue.put(message);
      if (ret) {
        Iterator<ISenderChannel> chItr = channels.iterator();
        while (chItr.hasNext()) {
          ISenderChannel ch = chItr.next();
          logger.debug("sendMessage() - Send message on channel ");
          ch.messageAvailable();
        }
      }
    }

    if (logger.isTraceEnabled()) logger.trace("sendMessage() - exit");

    return ret;
  }

  /**
   * Indicates whether the msg queue has any pending msgs
   * 
   * @return boolean
   */
  protected boolean hasMessages() {
    synchronized (queue) {
      return !queue.isEmpty();
    }
  }
}