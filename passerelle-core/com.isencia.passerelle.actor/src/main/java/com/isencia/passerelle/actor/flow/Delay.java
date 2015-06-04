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
package com.isencia.passerelle.actor.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * Simple actor that reads input tokens and and forwards them to the output port after a configurable accumulating delay
 * (ms).
 * <p>
 * This actor picks a received message, then sleeps for the configured time, then sends out the received message. And
 * then does it again for the next received message.
 * </p>
 * <p>
 * The net-result is a throttling on a received stream of messages. I.e. when incoming messages arrive with a higher
 * frequency than the configured delay, this actor buffers them and sends them out one-by-one with approximately
 * constant time intervals. When the incoming message stream is "slower" than the configured delay, i.e. messages arrive
 * with an interval that is longer than the configured delay, this actor results in a constant delay for each message.
 * </p>
 * 
 * @author erwin
 */
public class Delay extends Transformer {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(Delay.class);
  public Parameter timeParameter = null;
  private int time = 0;

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container
   *          The container.
   * @param name
   *          The name of this actor.
   * @exception IllegalActionException
   *              If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the container already has an actor with this name.
   */
  public Delay(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    timeParameter = new Parameter(this, "time(s)", new IntToken(1));
    timeParameter.setTypeEquals(BaseType.INT);
    registerConfigurableParameter(timeParameter);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  public void doFire(ManagedMessage message) throws ProcessingException {
    try {
      if (time > 0) {
        Thread.sleep(time * 1000);
      }
    } catch (InterruptedException e) {
      // do nothing, means someone wants us to stop
    }
    sendOutputMsg(output, message);
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == timeParameter) {
      time = ((IntToken) timeParameter.getToken()).intValue();
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }
}