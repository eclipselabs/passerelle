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
import com.isencia.message.interceptor.IMessageInterceptorChain;

/**
 * A ReceiverChannel encapsulates a data source (e.g. a java.io.Reader) and a
 * message extraction strategy. Each extracted message is sent out to the
 * channel's listeners.
 * 
 * @author erwin
 */
public abstract class ReceiverChannel extends Thread implements IReceiverChannel {

  private static final Logger logger = LoggerFactory.getLogger(ReceiverChannel.class);
  private IMessageInterceptorChain interceptorChainOnLeave = null;
  private Collection<IMessageListener> listeners = new ArrayList<IMessageListener>();
  private boolean open = false;

  public ReceiverChannel() {
  }

  public void setInterceptorChainOnLeave(IMessageInterceptorChain interceptorChain) {
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnLeave() - entry - chain :" + interceptorChain);
    this.interceptorChainOnLeave = interceptorChain;
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnLeave() - exit");
  }

  public Collection<IMessageListener> getListeners() {
    return listeners;
  }

  /**
   * Returns a new message from the channel, i.e. uses the channel in pull-mode.
   * The returned message will not be seen by any of the listeners that may be
   * registered. Before the msg is returned, it is processed by the registered
   * interceptor chain.
   * 
   * @return the new message
   * @throws ChannelException
   * @throws NoMoreMessagesException
   */
  public Object getMessage() throws ChannelException, NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("getMessage() - entry");

    Object msg = null;
    try {
      msg = doGetMessage();
      if (msg != null) {
        logger.debug("getMessage() - Received msg from channel : " + msg);
        if (interceptorChainOnLeave != null) msg = interceptorChainOnLeave.accept(msg);
      } else
        logger.debug("getMessage() - No message received");
    } catch (ChannelException e) {
      throw e;
    } catch (NoMoreMessagesException e) {
      throw e;
    } catch (Exception e) {
      throw new ChannelException(e.toString());
    }

    if (logger.isTraceEnabled()) {
      if (msg == null)
        logger.trace("getMessage() - exit - No message returned");
      else
        logger.trace("getMessage() - exit - Return msg : " + msg);
    }

    return msg;
  }

  public boolean isOpen() {
    return open;
  }

  /**
   * @param message
   * @throws InterruptedException
   * @throws Exception
   */
  public void acceptMessage(Object message) throws InterruptedException, Exception {
    if (logger.isTraceEnabled()) logger.trace("acceptMessage() - entry - message :" + message);

    synchronized (listeners) {
      Iterator<IMessageListener> lItr = listeners.iterator();
      if (message != null) {
        logger.debug("acceptMessage() - Accepted message from channel : " + message);
        if (interceptorChainOnLeave != null) message = interceptorChainOnLeave.accept(message);
      } else
        logger.debug("acceptMessage() - No message received");

      while (lItr.hasNext()) {
        lItr.next().acceptMessage(message, this);
      }
    }

    if (logger.isTraceEnabled()) logger.trace("acceptMessage() - exit");
  }

  public void addListener(IMessageListener newListener) {
    if (logger.isTraceEnabled()) logger.trace("addListener() - entry - listener :" + newListener);

    synchronized (listeners) {
      listeners.add(newListener);
    }

    if (logger.isTraceEnabled()) logger.trace("addListener() - exit");
  }

  public void addListeners(Collection<IMessageListener> newListeners) {
    if (logger.isTraceEnabled()) logger.trace("addListeners() - entry - listeners :" + newListeners);
    synchronized (listeners) {
      listeners.addAll(newListeners);
    }
    if (logger.isTraceEnabled()) logger.trace("addListeners() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");
    synchronized (listeners) {
      open = false;

      Iterator<IMessageListener> lItr = listeners.iterator();
      while (lItr.hasNext()) {
        lItr.next().sourceClosed(this);
      }
    }
    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");
    synchronized (listeners) {
      if (!open) {
        open = true;

        Iterator<IMessageListener> lItr = getListeners().iterator();
        while (lItr.hasNext()) {
          lItr.next().sourceOpened(this);
        }
      }
    }
    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public boolean removeListener(IMessageListener listener) {
    if (logger.isTraceEnabled()) logger.trace("removeListener() - entry - listener :" + listener);
    synchronized (listeners) {
      try {
        boolean result = listeners.remove(listener);
        if (logger.isTraceEnabled()) logger.trace("removeListener() - exit - result :" + result);
        return result;
      } catch (UnsupportedOperationException e) {
        logger.error("removeListener()", e);
        return false;
      }
    }
  }

  /**
   * Starts the channel in push mode. All messages that are found are dispatched
   * to all registered listeners.
   */
  public void run() {
    if (logger.isTraceEnabled()) logger.trace("run() - entry");

    try {
      if (!isOpen()) open();

      Object msg;
      while ((msg = doGetMessage()) != null) {
        acceptMessage(msg);
        if (Thread.currentThread().isInterrupted()) break;
      }
    } catch (InterruptedException e) {
    } catch (Exception e) {
      logger.error("run()", e);
    } finally {
      try {
        if (isOpen()) close();
      } catch (Exception e) {
      }
    }

    if (logger.isTraceEnabled()) logger.trace("run() - exit");
  }

  /**
   * Custom implementation per type of receiver channel to retrieve a new
   * message in a way compatible with the channel technology used.
   * 
   * @return the new message
   * @throws ChannelException
   * @throws NoMoreMessagesException
   */
  protected abstract Object doGetMessage() throws ChannelException, NoMoreMessagesException;
}