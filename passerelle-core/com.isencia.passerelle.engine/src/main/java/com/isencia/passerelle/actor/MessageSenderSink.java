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
package com.isencia.passerelle.actor;

import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.IMessageSender;
import com.isencia.message.ISenderChannel;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.interceptor.MessageToTextConverter;

/**
 * @author erwin
 */
public abstract class MessageSenderSink extends Sink {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(MessageSenderSink.class);
  private IMessageSender messageSender = null;

  /**
   * Constructor for ChannelSink.
   * 
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public MessageSenderSink(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * 
   * @throws IllegalActionException
   */
  protected void sendMessage(ManagedMessage message) throws ProcessingException {
    if (message != null) {
      messageSender.sendMessage(message);
    } else {
      requestFinish();
    }
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    messageSender = createMessageSender();
    if (messageSender == null) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "MessageSender not created correctly.", this, null);
    } else {
      IMessageInterceptorChain interceptors = createInterceptorChainOnEnter();
      if (interceptors == null) {
        // default implementation
        if (!isPassThrough()) {
          interceptors = new MessageInterceptorChain();
          interceptors.add(new MessageToTextConverter());
        }
      }
      Collection<ISenderChannel> channels = messageSender.getChannels();
      Iterator<ISenderChannel> iter = channels.iterator();
      while (iter.hasNext()) {
        ISenderChannel element = iter.next();
        element.setInterceptorChainOnEnter(interceptors);
      }
      messageSender.open();
      getLogger().debug("{} - Opened : {}", getFullName(), getMessageSender());
    }
  }

  protected void doWrapUp() throws TerminationException {
    super.doWrapUp();
    getMessageSender().close();
    getLogger().debug("{} - Closed : {}", getFullName(), getMessageSender());
  }

  /**
   * @return a chain of interceptors that are invoked when the msg enters the message sender
   */
  protected abstract IMessageInterceptorChain createInterceptorChainOnEnter();

  /**
   * @return the message sender encapsulated by this actor
   */
  protected abstract IMessageSender createMessageSender();

  /**
   * Returns the messageSender.
   * 
   * @return IMessageSender
   */
  public IMessageSender getMessageSender() {
    return messageSender;
  }

}