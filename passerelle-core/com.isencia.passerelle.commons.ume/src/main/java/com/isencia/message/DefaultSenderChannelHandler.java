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

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultSenderChannelHandler A channel handler implementation that loops over
 * all message providers of the channel, each time it is triggered by a
 * messageAvailable() invocation. Channel handlers are used in combination with
 * sender channels in pull mode.
 * 
 * @author erwin
 */
public class DefaultSenderChannelHandler implements ISenderChannelHandler {
  private final static Logger logger = LoggerFactory.getLogger(DefaultSenderChannelHandler.class);

  private ISenderChannel channel = null;
  private MessageLoop msgLoop = null;
  private boolean open = false;

  /**
   * @param channel
   */
  public DefaultSenderChannelHandler(ISenderChannel channel) {
    super();
    this.channel = channel;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannelHandler#messageAvailable()
   */
  public void messageAvailable() {
    if (msgLoop != null) {
      msgLoop.trigger();
    } else {
      logger.error("messageAvailable() - Msg loop not initialized");
    }
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannelHandler#close()
   */
  public void close() {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    if (isOpen()) {
      // wait for the loop to finish processing
      // pending messages
      if (msgLoop != null) {
        msgLoop.interrupt();
        try {
          logger.debug("close() - Joining msg loop...");
          msgLoop.join();
        } catch (InterruptedException e) {
          // do nothing
        }
      }
      open = false;
    }
    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannelHandler#open()
   */
  public void open() {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (!isOpen()) {
      // start the msg loop
      open = true;
      msgLoop = new MessageLoop();
      msgLoop.start();
    }
    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannelHandler#isOpen()
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * MessageLoop will loop over all providers until they have no more messages
   * available, for a number of times as specified by the triggercount. Each
   * finished loop decreases the triggercount. Each new messageAvailable()
   * notification on SenderChannel increases the triggercount.
   */
  class MessageLoop extends Thread {

    private int triggerCount = 0;

    /**
     * @see Runnable#run()
     */
    public void run() {
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.run() - entry");
      }
      try {
        boolean foundMsg = true;
        while (foundMsg || !isInterrupted()) {
          foundMsg = false;
          consumeTrigger();
          Iterator<IMessageProvider> providerItr = channel.getProviders().iterator();
          while (providerItr.hasNext()) {
            IMessageProvider provider = providerItr.next();
            boolean noMoreMessages = false;
            while (!noMoreMessages) {
              try {
                Object message = provider.getMessage();
                foundMsg = true;
                try {
                  channel.sendMessage(message);
                } catch (Exception e) {
                  logger.error("Error sending message " + message, e);
                }
              } catch (NoMoreMessagesException e) {
                noMoreMessages = true;
              }
            }
          }
        }
      } catch (InterruptedException e) {
      }

      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.run() - exit");
      }
    }

    /**
     * Increments the trigger count and releases all wait-locks
     */
    public synchronized void trigger() {
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.trigger() - entry");
      }
      triggerCount++;
      this.notifyAll();
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.trigger() - exit");
      }
    }

    /**
     * Decrements the trigger count. If no triggers available, waits for one.
     */
    public synchronized void consumeTrigger() throws InterruptedException {
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.consumeTrigger() - entry");
      }
      while (triggerCount <= 0) {
        this.wait();
      }
      triggerCount--;
      if (logger.isTraceEnabled()) {
        logger.trace("MessageLoop.consumeTrigger() - exit");
      }
    }
  }

}
