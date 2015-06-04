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
package com.isencia.passerelle.actor.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * Produce a counter output.
 * 
 * @author erwin
 */

public class Counter extends Transformer {
  private static final long serialVersionUID = 3712075950718358876L;

  private static Logger LOGGER = LoggerFactory.getLogger(Counter.class);

  public Parameter startValueParam;
  private long startValue = 0;
  private long value = 0;
  public Port reset = null;
  private PortHandler resetHandler = null;
  private boolean isReset = true;

  /**
   * Construct a constant source with the given container and name. Create the <i>value</i> parameter, initialize its
   * value to the default value of an IntToken with value 1.
   * 
   * @param container
   *          The container.
   * @param name
   *          The name of this actor.
   * @exception IllegalActionException
   *              If the entity cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the container already has an actor with this name.
   */
  public Counter(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    startValueParam = new Parameter(this, "start", new LongToken(1));
    startValueParam.setTypeEquals(BaseType.LONG);
    registerConfigurableParameter(startValueParam);

    reset = PortFactory.getInstance().createInputPort(this, "reset", null);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    // If something connected to the reset port, install a handler
    if (reset.getWidth() > 0) {
      resetHandler = createPortHandler(reset, new PortListenerAdapter() {
        public void tokenReceived() {
          Token token = resetHandler.getToken();
          if (token != null && !token.isNil()) {
            getLogger().debug("{} Reset Event received", getFullName());
            isReset = true;
          }
        }
      });
      resetHandler.start();
    }
    setValue(getStartValue());
  }

  /**
   * @param attribute
   *          The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == startValueParam) {
      LongToken valueToken = (LongToken) startValueParam.getToken();
      if (valueToken != null) {
        setStartValue(valueToken.longValue());
        getLogger().debug("{} Counter start value changed to {}", getFullName(), getStartValue());
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    if (isReset) {
      setValue(getStartValue());
      isReset = false;
    }
    ManagedMessage newMsg = null;
    try {
      newMsg = createMessage(Long.toString(value++), "text/plain");
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating msg with counter", this, newMsg, e);
    }
    sendOutputMsg(output, newMsg);
  }

  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      return "sent message with count " + message.getBodyContentAsString();
    } catch (MessageException e) {
      getLogger().error("Error converting msg to string", e);
      return "";
    }
  }
  /**
   * Returns the value.
   * 
   * @return long
   */
  public long getValue() {
    return value;
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          The value to set
   */
  public void setValue(long value) {
    this.value = value;
  }

  /**
   * Returns the startValue.
   * 
   * @return long
   */
  public long getStartValue() {
    return startValue;
  }

  /**
   * Sets the startValue.
   * 
   * @param startValue
   *          The startValue to set
   */
  public void setStartValue(long startValue) {
    this.startValue = startValue;
  }

}