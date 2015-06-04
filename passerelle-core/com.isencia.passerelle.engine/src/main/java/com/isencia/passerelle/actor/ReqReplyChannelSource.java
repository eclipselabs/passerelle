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
import ptolemy.data.Token;
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
import com.isencia.message.requestreply.IMessage;
import com.isencia.message.requestreply.IRequestReplyChannel;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.interceptor.MessageToTextConverter;
import com.isencia.passerelle.message.xml.XmlMessageHelper;

/**
 * Base class for all Passerelle Sources that use a IReceiverChannel. Sub-classes must implement createChannel(), returning a completely defined
 * IReceiverChannel of the desired type. Typically, sub-classes will also define their specific Passerelle Parameters, and will then override the default
 * attributeChanged() method to handle changes in parameter values. openChannel() and closeChannel() may sometimes be overridden to add specific open/close
 * processing.
 * 
 * @author erwin
 */
public abstract class ReqReplyChannelSource extends Source {
  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(ReqReplyChannelSource.class);

  private IRequestReplyChannel receiverChannel = null;
  public Parameter passThroughParam = null;
  private boolean passThrough = true;

  public Port replyPort = null;
  private PortHandler replyHandler = null;

  /**
   * Constructor for Source.
   * 
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public ReqReplyChannelSource(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    replyPort = PortFactory.getInstance().createInputPort(this, "reply", null);
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
   * @param attribute The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    LOGGER.trace("{} attributeChanged() - entry : {}", getFullName(), attribute);

    if (attribute == passThroughParam) {
      passThrough = ((BooleanToken) passThroughParam.getToken()).booleanValue();
    } else
      super.attributeChanged(attribute);

    LOGGER.trace("{} attributeChanged() - exit", getFullName());
  }

  /**
   * Returns the current receiver channel.
   * 
   * @return IReceiverChannel
   */
  public IRequestReplyChannel getChannel() {
    return receiverChannel;
  }

  /**
   * @todo improve termination handling with a combination of end of source and end of sink processing
   */
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    if (replyPort.getWidth() > 0) {
      replyHandler = createPortHandler(replyPort, new PortListenerAdapter() {
        public void tokenReceived() {
          Token token = replyHandler.getToken();
          if (token != null && !token.isNil()) {
            try {
              ManagedMessage message = MessageHelper.getMessageFromToken(token);
              getLogger().debug("{} - received reply msg : {}", getFullName(), getAuditTrailMessage(message, replyPort));
              try {
                getChannel().sendResponse(message, message.getCorrelationID());
              } catch (ChannelException e) {
                try {
                  sendErrorMessage(new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Failed to send reply msg on channel", ReqReplyChannelSource.this, message, e));
                } catch (IllegalActionException e1) {
                  getLogger().error("Error sending error msg", e);
                  getLogger().error("Error sending response msg", e1);
                }
              }
            } catch (Exception e) {
              getLogger().error("Error handling received token "+token, e);
            }
          }
        }
      });
      // Start handling the port
      replyHandler.start();
    }
    IRequestReplyChannel res = null;
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
      // This does not work for request reply:
      // - the converter is called by the regular receiver channel when it invokes its interceptor chain,
      // BEFORE the ReqReplyChannel wrapper has the chance to define the correlation ID
      // - we can not add the correlation ID after the msg construction, as this is a protected system header field
      // -> so we need to receive the fully defined request msg, and THEN invoke the message construction, passing the
      // correlation ID into the MessageFactory.
      // This is done inside the getMessage()
      if (!isPassThrough()) {
        IMessageInterceptorChain interceptors = new MessageInterceptorChain();
        interceptors.add(new MessageToTextConverter());
        getChannel().setInterceptorChainForResponse(interceptors);
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
        getLogger().debug("{} - Opened : {}",getFullName(),getChannel());
      }
    } catch (ChannelException e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Receiver channel not opened correctly.", this, e);
    }
    return res && super.doPreFire();
  }

  protected boolean doPostFire() throws ProcessingException {
    try {
      if (hasNoMoreMessages()) {
        closeChannel(getChannel());
      }
    } catch (ChannelException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Receiver channel not closed correctly.", this, e);
    }
    return super.doPostFire();
  }

  protected void doWrapUp() throws TerminationException {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}",getFullName(),getChannel());
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
  protected abstract IRequestReplyChannel createChannel() throws ChannelException, InitializationException;

  protected ManagedMessage getMessage() throws ProcessingException {
    getLogger().trace("{} getMessage() - entry", getFullName());

    ManagedMessage res = null;

    try {
      IMessage msg = getChannel().receiveRequest();
      if (isPassThrough())
        res = XmlMessageHelper.fillMessageContentFromXML(createMessage(), (String) msg.getMessage());
      else {
        // this allows to directly link the output port to the reply port
        // as the msg contains its own ID as correlationID
        res = MessageFactory.getInstance().createCorrelatedMessage(msg.getCorrelationID().toString(), getStandardMessageHeaders());
        res.setBodyContentPlainText(msg.getMessage().toString());
      }

    } catch (NoMoreMessagesException e) {
      // ignore, just return null and the source will finish
      // its life-cycle automatically
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error getting message from channel", this, e);
    }

    getLogger().trace("{} getMessage() - exit", getFullName());
    return res;
  }

  /**
   * Overridable method to allow modification of channel closing.
   * 
   * @param ch the channel to be closed
   * @throws ChannelException
   */
  protected void closeChannel(IReceiverChannel ch) throws ChannelException {
    getLogger().trace("{} closeChannel() - entry", getFullName());
    if ((ch != null) && ch.isOpen()) {
      ch.close();
    }
    getLogger().trace("{} closeChannel() - exit", getFullName());
  }

  /**
   * Overridable method to allow modification of channel opening.
   * 
   * @param ch the channel to be opened
   * @throws ChannelException
   */
  protected void openChannel(IReceiverChannel ch) throws ChannelException {
    getLogger().trace("{} openChannel() - entry", getFullName());
    if ((ch != null) && !ch.isOpen()) {
      ch.open();
    }
    getLogger().trace("{} openChannel() - exit", getFullName());
  }

  /**
   * Returns the passThrough flag, indicating whether messages should be sent on as received or should be wrapped in a std Passerelle envelope.
   * 
   * @return the passThrough flag
   */
  public boolean isPassThrough() {
    return passThrough;
  }

  /**
   * Sets the passThrough, indicating whether messages should be sent on as received or should be wrapped in a std Passerelle envelope.
   * 
   * @param the passThrough value
   */
  public void setPassThrough(boolean passThrough) {
    this.passThrough = passThrough;
  }

}