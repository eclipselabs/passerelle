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
 * A SenderChannel encapsulates a data sink (e.g. a java.io.Writer) and a
 * message generation strategy. Each generated message is sent out to the
 * channel's underlying sink.
 * 
 * @version 1.0
 * @author erwin
 */
public abstract class SenderChannel implements ISenderChannel, IMessageProvider {
  // ~ Instance/static variables
  // ..............................................................................................................................

  private static final Logger logger = LoggerFactory.getLogger(SenderChannel.class);
  private IMessageInterceptorChain interceptorChainOnEnter = null;
  private boolean open = false;
  private Collection<IMessageProvider> providers = new ArrayList<IMessageProvider>();
  private ISenderChannelHandler handler = null;

  public SenderChannel() {
    super();

    handler = new DefaultSenderChannelHandler(this);
  }

  public void setInterceptorChainOnEnter(IMessageInterceptorChain interceptorChain) {
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnEnter() - entry - chain :" + interceptorChain);
    this.interceptorChainOnEnter = interceptorChain;
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnEnter() - exit");
  }

  public Object getMessage() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("getMessage() - entry");

    Object message = null;
    Collection<IMessageProvider> providers = getProviders();
    if (providers == null || providers.size() == 0) {
      logger.error("getMessage() - No providers specified");
      throw new NoMoreMessagesException("No providers specified");
    }

    Iterator<IMessageProvider> iter = providers.iterator();
    while (iter.hasNext()) {
      logger.debug("getMessage() - Try to get provider");

      IMessageProvider provider = iter.next();
      if (provider == null) continue;

      try {
        message = provider.getMessage();
        logger.debug("getMessage() - Found message :" + message);
        break;
      } catch (NoMoreMessagesException e) {
        logger.debug("getMessage() - No message found");
      }
    }

    if (logger.isTraceEnabled()) logger.trace("getMessage() - exit - message :" + message);

    if (message == null) throw new NoMoreMessagesException();

    try {
      if (interceptorChainOnEnter != null) {
        return interceptorChainOnEnter.accept(message);
      } else {
        return message;
      }
    } catch (Exception e) {
      throw new NoMoreMessagesException(e.getMessage());
    }
  }

  public boolean isOpen() {
    return open;
  }

  public Collection<IMessageProvider> getProviders() {
    return providers;
  }

  public void addProvider(IMessageProvider provider) {
    if (logger.isTraceEnabled()) {
      logger.trace("addProvider() - entry - provider :" + provider);
    }
    providers.add(provider);
    if (logger.isTraceEnabled()) {
      logger.trace("addProvider() - exit");
    }
  }

  public void addProviders(Collection<IMessageProvider> providers) {
    if (logger.isTraceEnabled()) {
      logger.trace("addProviders() - entry - providers :" + providers);
    }
    providers.addAll(providers);
    if (logger.isTraceEnabled()) {
      logger.trace("addProviders() - exit");
    }
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    handler.close();
    open = false;

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  public boolean hasMessage() {
    if (logger.isTraceEnabled()) logger.trace("hasMessage() - entry");

    boolean ret = false;
    Collection<IMessageProvider> providers = getProviders();
    Iterator<IMessageProvider> iter = providers.iterator();
    while (iter.hasNext()) {
      IMessageProvider provider = iter.next();
      if (provider.hasMessage()) {
        ret = true;
        break;
      }
    }

    if (logger.isTraceEnabled()) logger.trace("hasMessage() - exit - result :" + ret);

    return ret;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    handler.open();
    open = true;

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public boolean removeProvider(IMessageProvider provider) {
    if (logger.isTraceEnabled()) logger.trace("removeProvider() - entry");
    boolean result = providers.remove(provider);
    if (logger.isTraceEnabled()) logger.trace("removeProvider() - exit - result :" + result);
    return result;
  }

  public void sendMessage(Object message) throws InterruptedException, ChannelException {
    if (logger.isTraceEnabled()) logger.trace("sendMessage() - entry - message :" + message);

    try {
      if (interceptorChainOnEnter != null) {
        doSendMessage(interceptorChainOnEnter.accept(message));
      } else {
        doSendMessage(message);
      }
    } catch (Exception e) {
      throw new ChannelException(e.getMessage());
    }

    if (logger.isTraceEnabled()) logger.trace("sendMessage() - exit");
  }

  /**
   * @param message
   * @throws Exception
   */
  protected abstract void doSendMessage(Object message) throws Exception;

  public void messageAvailable() {
    if (logger.isTraceEnabled()) logger.trace("messageAvailable() - entry");

    handler.messageAvailable();

    if (logger.isTraceEnabled()) logger.trace("messageAvailable() - exit");
  }
}