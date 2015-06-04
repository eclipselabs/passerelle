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
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ChannelException;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.interceptor.TextToMessageConverter;
import com.isencia.passerelle.message.xml.XmlMessageHelper;

/**
 * Base class for all Passerelle Sources that use a IReceiverChannel. Sub-classes must implement createChannel(),
 * returning a completely defined IReceiverChannel of the desired type. Typically, sub-classes will also define their
 * specific Passerelle Parameters, and will then override the default attributeChanged() method to handle changes in
 * parameter values. openChannel() and closeChannel() may sometimes be overriden to add specific open/close processing.
 * 
 * @author erwin
 */
public abstract class ChannelSource extends Source {

  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(ChannelSource.class);

  private IReceiverChannel receiverChannel = null;
  public Parameter passThroughParam = null;
  private boolean passThrough = false;

  /**
   * Constructor for Source.
   * 
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public ChannelSource(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    passThroughParam = new Parameter(this, "PassThrough", new BooleanToken(false));
    passThroughParam.setTypeEquals(BaseType.BOOLEAN);
    registerExpertParameter(passThroughParam);
    new CheckBoxStyle(passThroughParam, "style");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  /**
   * Triggered whenever e.g. a parameter has been modified.
   * 
   * @param attribute
   *          The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}",getFullName(),attribute);
    if (attribute == passThroughParam) {
      passThrough = ((BooleanToken) passThroughParam.getToken()).booleanValue();
    } else
      super.attributeChanged(attribute);
    getLogger().trace("{} attributeChanged() - exit",getFullName());
  }

  /**
   * Returns the current receiver channel.
   * 
   * @return IReceiverChannel
   */
  public IReceiverChannel getChannel() {
    return receiverChannel;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    IReceiverChannel res = null;
    try {
      res = createChannel();
    } catch (ChannelException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Receiver channel not created correctly.", this, e);
    }
    if (res == null) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Receiver channel not created correctly.", this, null);
    } else {
      // just to make sure...
      try {
        closeChannel(getChannel());
      } catch (ChannelException e) {
        throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Receiver channel not initialized correctly.", this, e);
      }
      receiverChannel = res;
      if (!isPassThrough()) {
        IMessageInterceptorChain interceptors = createInterceptorChain();
        getChannel().setInterceptorChainOnLeave(interceptors);
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
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Receiver channel not opened correctly.", this, e);
    }
    res = res && super.doPreFire();
    return res;
  }

  protected boolean doPostFire() throws ProcessingException {
    try {
      if (hasNoMoreMessages()) {
        closeChannel(getChannel());
        getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
      }
    } catch (ChannelException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Receiver channel not closed correctly.", this, e);
    }
    return super.doPostFire();
  }
  
  @Override
  protected void doStop() {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new RuntimeException(new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Receiver channel not closed correctly.", this, e));
    }
    super.doStop();
  }

  protected void doWrapUp() throws TerminationException {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Receiver channel not closed correctly.", this, e);
    }
    super.doWrapUp();
  }

  /**
   * Factory method to be implemented per specific type of source
   * 
   * @return a new instance of a receiver channel of the relevant type
   * @throws ChannelException
   * @throws InitializationException
   */
  protected abstract IReceiverChannel createChannel() throws ChannelException, InitializationException;

  /**
   * This method can be overridden to define some custom mechanism to convert incoming messages of some specific format
   * into standard passerelle messages. The method is called by this base class, only when it is not configured in
   * "pass-through" mode. In pass-through mode, we assume that the incoming message is a xml serialization of a
   * passerelle message, as generated by a passerelle sink in pass-through mode. By default, creates an interceptor
   * chain containing a simple text-to-message conversion.
   * 
   * @see XmlMessageHelper.getXMLFromMessage() for the expected xml format in pass-through mode
   * @return
   */
  protected IMessageInterceptorChain createInterceptorChain() {
    IMessageInterceptorChain interceptors = new MessageInterceptorChain();
    interceptors.add(new TextToMessageConverter(this));
    return interceptors;
  }

  protected ManagedMessage getMessage() throws ProcessingException {
    getLogger().trace("{} getMessage() - entry",getFullName());
    ManagedMessage res = null;
    try {
      if (isPassThrough())
        res = XmlMessageHelper.fillMessageContentFromXML(createMessage(), (String) getChannel().getMessage());
      else {
        try {
          res = (ManagedMessage) getChannel().getMessage();
        } catch (NoMoreMessagesException e) {
          // ignore, just return null and the source will finish
          // its life-cycle automatically
        }
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting message from channel", this, e);
    }
    getLogger().trace("{} getMessage() - exit",getFullName());
    return res;
  }

  /**
   * Overridable method to allow modification of channel closing.
   * 
   * @param ch
   *          the channel to be closed
   * @throws ChannelException
   */
  protected void closeChannel(IReceiverChannel ch) throws ChannelException {
    getLogger().trace("{} closeChannel() - entry",getFullName());
    if ((ch != null) && ch.isOpen()) {
      ch.close();
    }
    getLogger().trace("{} closeChannel() - exit",getFullName());
  }

  /**
   * Overridable method to allow modification of channel opening.
   * 
   * @param ch
   *          the channel to be opened
   * @throws ChannelException
   */
  protected void openChannel(IReceiverChannel ch) throws ChannelException {
    getLogger().trace("{} openChannel() - entry",getFullName());
    if ((ch != null) && !ch.isOpen()) {
      ch.open();
    }
    getLogger().trace("{} openChannel() - exit",getFullName());
  }

  /**
   * Returns the passThrough flag, indicating whether messages should be sent on as received or should be wrapped in a
   * std Passerelle envelope.
   * 
   * @return the passThrough flag
   */
  public boolean isPassThrough() {
    return passThrough;
  }

  /**
   * Sets the passThrough, indicating whether messages should be sent on as received or should be wrapped in a std
   * Passerelle envelope.
   * 
   * @param the
   *          passThrough value
   */
  public void setPassThrough(boolean passThrough) {
    this.passThrough = passThrough;
  }

}