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
import com.isencia.message.IMessageReceiver;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author erwin
 */
public abstract class MessageReceiverSource extends Source {

  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(MessageReceiverSource.class);
  private IMessageReceiver messageReceiver = null;

  /**
   * Creates a new MessageReceiverSource object.
   * 
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public MessageReceiverSource(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * Returns the messageReceiver.
   * 
   * @return IMessageReceiver
   */
  public IMessageReceiver getMessageReceiver() {
    return messageReceiver;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    messageReceiver = createMessageReceiver();
    if (messageReceiver == null) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "MessageReceiver not created correctly.", this, null);
    } else {
      IMessageInterceptorChain interceptors = createInterceptorChainOnLeave();
      Collection<IReceiverChannel> channels = messageReceiver.getChannels();
      synchronized (channels) {
        Iterator<IReceiverChannel> iter = channels.iterator();
        while (iter.hasNext()) {
          IReceiverChannel element = iter.next();
          element.setInterceptorChainOnLeave(interceptors);
        }
      }
      messageReceiver.open();
      getLogger().debug("{} - Opened : {}", getFullName(), getMessageReceiver());
    }
  }

  protected void doWrapUp() throws TerminationException {
    super.doWrapUp();
    getMessageReceiver().close();
    getLogger().debug("{} - Closed : {}", getFullName(), getMessageReceiver());
  }

  /**
   * @return a chain of interceptors that are invoked when the msg leaves the message receiver
   */
  protected abstract IMessageInterceptorChain createInterceptorChainOnLeave();

  /**
   * @return the concrete message receiver encapsulated in this actor
   */
  protected abstract IMessageReceiver createMessageReceiver();

  protected ManagedMessage getMessage() throws ProcessingException {
    getLogger().trace("{} getMessage() - entry",getFullName());

    ManagedMessage res = null;
    try {
      if (messageReceiver != null)
        res = (ManagedMessage) messageReceiver.getMessage();
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting message from messageReceiver", this, e);
    }
    getLogger().trace("{} getMessage() - exit",getFullName());
    return res;
  }
}