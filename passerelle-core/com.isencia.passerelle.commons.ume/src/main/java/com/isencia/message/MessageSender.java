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
import com.isencia.util.BlockingReaderQueue;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;

/**
 * Base class for asynchronous message senders, using an intermediate
 * synchronized queue. This allows the send side clients to add new msgs
 * independently of the overhead of the actual sending operation. TODO: put a
 * max size on the queue.
 * 
 * @author erwin
 */
public class MessageSender implements IMessageSender {
  private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);

  private BlockingReaderQueue queue = null;
  private Collection<ISenderChannel> channels = null;
  private MessageLoop msgLoop = null;
  private boolean open = false;

  public MessageSender() {
    queue = new BlockingReaderQueue(new FIFOQueue());
    channels = new ArrayList<ISenderChannel>();
  }

  /**
   * For this asynchronous message sender implementation, the sendMessage just
   * puts msgs on an intermediate queue. A separate thread is continuously
   * monitoring the queue and grabbing msgs and sending them out.
   * 
   * @throws IllegalStateException If the MessageSender is not in the open state
   * @throws InterruptedException If the thread is interrupted during a put() on
   *           the queue.
   * @see IMessageSender#sendMessage(Object)
   */
  public boolean sendMessage(Object message) {
    if (logger.isTraceEnabled()) logger.trace("sendMessage() - entry - Message: " + message);

    if (!open) throw new IllegalStateException("MessageSender is not open");

    boolean ret = queue.put(message);

    if (logger.isTraceEnabled()) logger.trace("sendMessage() - exit - ");

    return ret;
  }

  /**
   * Indicates whether the msg queue has any pending msgs
   * 
   * @return boolean
   */
  protected boolean hasMessages() {
    return !queue.isEmpty();
  }

  /**
   * Get the first message from the msg queue. The queue.get() is a blocking
   * call, returning when a msg is available on the queue. After calling
   * close(), getMessage() will only call on the queue if it is non-empty. Once
   * it is empty, getMessage() returns null.
   * 
   * @return Object the first message from the queue
   * @throws NoMoreMessagesException if the queue is empty
   * @throws InterruptedException If the thread is interrupted during the
   *           blocking get() on the queue.
   */
  protected Object getMessage() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - entry");
    }
    if (open || hasMessages())
      try {
        Object result = queue.get();
        if (logger.isTraceEnabled()) {
          logger.trace("getMessage() - exit - Result :" + result);
        }
        return result;
      } catch (EmptyQueueException e) {
        throw new NoMoreMessagesException(e.toString());
      }
    else {
      throw new NoMoreMessagesException("No more messages");
    }
  }

  /**
   * @param message
   * @throws InterruptedException
   * @throws ChannelException
   */
  protected void dispatchMessage(Object message) throws InterruptedException, ChannelException {

    if (logger.isTraceEnabled()) logger.trace("dispatchMessage() - entry - Dispatching:\n" + message);

    Iterator<ISenderChannel> chItr = getChannels().iterator();
    while (chItr.hasNext()) {
      ISenderChannel ch = chItr.next();
      ch.sendMessage(message);
    }

    if (logger.isTraceEnabled()) logger.trace("dispatchMessage() - exit");
  }

  public void open() {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    Iterator<ISenderChannel> chItr = getChannels().iterator();
    while (chItr.hasNext()) {
      ISenderChannel ch = chItr.next();
      try {
        ch.open();
      } catch (ChannelException e) {
        logger.error("open() - Error opening channel", e);
      }
    }

    if (!open) {
      // adjust state before starting the loop
      open = true;

      // start the msg loop
      msgLoop = new MessageLoop();
      msgLoop.start();
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public void close() {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    // adjust state before closing channels
    open = false;
    if (!hasMessages()) {
      // msgLoop is in wait, so interrupt it
      msgLoop.interrupt();
    } else {
      // wait for the loop to finish processing
      // pending messages
      try {
        logger.debug("close() - Joining msg loop...");
        msgLoop.join();
      } catch (InterruptedException e) {
        // do nothing
      }
    }

    msgLoop = null;

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  public void addChannel(ISenderChannel newChannel) {
    if (logger.isTraceEnabled()) {
      logger.trace("addChannel() - entry - channel :" + newChannel);
    }
    getChannels().add(newChannel);
    if (logger.isTraceEnabled()) {
      logger.trace("addChannel() - exit");
    }
  }

  public void closeChannels() {
    if (logger.isTraceEnabled()) {
      logger.trace("closeChannels() - entry");
    }
    Iterator<ISenderChannel> chItr = getChannels().iterator();
    while (chItr.hasNext()) {
      ISenderChannel ch = chItr.next();
      try {
        ch.close();
      } catch (ChannelException e) {
        logger.error("closeChannels() - Error closing channel", e);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("closeChannels() - exit");
    }
  }

  public Collection<ISenderChannel> getChannels() {
    return channels;
  }

  public boolean removeChannel(ISenderChannel newChannel) {
    if (logger.isTraceEnabled()) {
      logger.trace("removeChannel() - entry - channel :" + newChannel);
    }
    try {
      boolean res = getChannels().remove(newChannel);
      if (logger.isTraceEnabled()) {
        logger.trace("removeChannel() - exit - Result :" + res);
      }
      return res;
    } catch (UnsupportedOperationException e) {
      logger.error("removeChannel()", e);
      return false;
    }
  }

  public boolean isOpen() {
    return open;
  }

  /**
   * MessageLoop Inner class that implements a separate thread loop for sending
   * out messages from the internal queue.
   * 
   * @author erwin
   */
  class MessageLoop extends Thread {
    /**
     * @see Runnable#run()
     */
    public void run() {
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.run() - entry");
      }
      try {
        Object msg = null;
        boolean loop = true;
        int msgNr = 1;
        while (loop) {
          msg = getMessage();
          if (msg == null) {
            loop = false;
          } else {
            logger.info("MessageLoop.run() - sending msg " + (msgNr++) + ":" + msg);
            dispatchMessage(msg);
          }
        }
      } catch (InterruptedException e) {
        // do nothing, just stop the loop
      } catch (Exception e) {
        logger.error("MessageLoop.run()", e);
      } finally {
        // close all channels
        closeChannels();
      }
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.run() - exit");
      }
    }

  }

}