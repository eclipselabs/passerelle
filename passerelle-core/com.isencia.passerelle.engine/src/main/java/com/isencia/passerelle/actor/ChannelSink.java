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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ChannelException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.interceptor.MessageToTextConverter;

/**
 * Base class for all Passerelle Sinks that use a ISenderChannel. Sub-classes must implement createChannel(), returning
 * a completely defined ISenderChannel of the desired type. Typically, sub-classes will also define their specific
 * Passerelle Parameters, and will then override the default attributeChanged() method to handle changes in parameter
 * values. openChannel() and closeChannel() may sometimes be overridden to add specific open/close processing.
 * ChannelSink provides a fully functional doFire() implementation that uses the ISenderChannel to send out any messages
 * received on the actor's input.
 * 
 * @author Erwin De Ley
 */
public abstract class ChannelSink extends Sink {

  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(ChannelSink.class);

  private ISenderChannel sendChannel = null;

  /**
   * Constructor for ChannelSink.
   * 
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public ChannelSink(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * Returns the sender channel.
   * 
   * @return ISenderChannel
   */
  public ISenderChannel getChannel() {
    return sendChannel;
  }

  protected void sendMessage(ManagedMessage message) throws ProcessingException {
    try {
      if (message != null) {
        getChannel().sendMessage(message);
      } else {
        requestFinish();
      }
    } catch (InterruptedException e) {
      // do nothing, just means we've got to stop
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error sending msg on channel", this, message, e);
    }
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    ISenderChannel res = null;
    try {
      res = createChannel();
    } catch (ChannelException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not created correctly.", this, e);
    }
    if (res == null) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not created correctly.", this, null);
    } else {
      // just to make sure...
      try {
        closeChannel(getChannel());
      } catch (ChannelException e) {
        throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not initialized correctly.", this, e);
      }
      sendChannel = res;
      if (!isPassThrough()) {
        IMessageInterceptorChain interceptors = createInterceptorChain();
        sendChannel.setInterceptorChainOnEnter(interceptors);
      }
    }
  }

  protected boolean doPreFire() throws ProcessingException {
    boolean res = true;
    // Channel open must not be done in initialize()
    // All initialize() invocations on the actors in a model
    // are done sequentially in 1 thread. So if a certain channel
    // depends on another channel's open status, this might fail if
    // the respective initialize() methods are invoked in the wrong order.
    // prefire() is called in separate threads per actor. Then a channel
    // can implement retries during open(), giving the other actors the time
    // to open their channels as well.
    try {
      if (!getChannel().isOpen()) {
        openChannel(getChannel());
        getLogger().debug("{} - Opened : {}", getFullName(), getChannel());
      }
    } catch (ChannelException e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not opened correctly.", this, e);
    }
    res = res && super.doPreFire();
    return res;
  }

  @Override
  protected void doStop() {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new RuntimeException(new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Sender channel not closed correctly.", this, e));
    }
    super.doStop();
  }

  protected void doWrapUp() throws TerminationException {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error closing channel.", this, e);
    }
  }

  /**
   * Factory method to be implemented per specific type of sink
   * 
   * @return a new instance of a sender channel of the relevant type
   * @throws ChannelException
   * @throws InitializationException
   */
  protected abstract ISenderChannel createChannel() throws ChannelException, InitializationException;

  /**
   * This method can be overridden to define some custom mechanism to convert outgoing passerelle messages into some
   * desired format. The method is called by this base class, only when it is not configured in "pass-through" mode. By
   * default, creates an interceptor chain containing a simple message-body-to-text conversion.
   * 
   * @return
   */
  protected IMessageInterceptorChain createInterceptorChain() {
    IMessageInterceptorChain interceptors = new MessageInterceptorChain();
    interceptors.add(new MessageToTextConverter());
    return interceptors;
  }

  protected String getExtendedInfo() {
    return null;
  }

  /**
   * Overridable method to allow modification of channel closing.
   * 
   * @param ch
   * @throws ChannelException
   */
  protected void closeChannel(ISenderChannel ch) throws ChannelException {
    getLogger().trace("{} closeChannel() - entry", getFullName());
    if ((ch != null) && ch.isOpen()) {
      ch.close();
    }
    getLogger().trace("{} closeChannel() - exit", getFullName());
  }

  /**
   * Overridable method to allow modification of channel opening.
   * 
   * @param ch
   * @throws ChannelException
   */
  protected void openChannel(ISenderChannel ch) throws ChannelException {
    getLogger().trace("{} openChannel() - entry", getFullName());
    if (ch != null) {
      ch.open();
    }
    getLogger().trace("{} openChannel() - exit", getFullName());
  }
}