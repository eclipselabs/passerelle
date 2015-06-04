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

package com.isencia.passerelle.actor.eip;

import java.util.Collection;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author delerw
 *
 */
public abstract class MessageRouter extends Actor {
  private static final long serialVersionUID = 1L;
  public Port input;
  public Port defaultOutput;
  public OutputPortConfigurationExtender outputPortCfgExt;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MessageRouter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    defaultOutput = PortFactory.getInstance().createOutputPort(this, "defaultOutput");
    outputPortCfgExt = new OutputPortConfigurationExtender(this, "output port configurer");
  }
  
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage inputMsg = request.getMessage(input);
    try {
      boolean foundSelectedPort=false;
      String portName = routeToPort(outputPortCfgExt.getOutputPortNames(), inputMsg);
      if(portName!=null) {
        Port selectedPort = (Port) getPort(portName);
        if(selectedPort!=null) {
          response.addOutputMessage(selectedPort, inputMsg);
          foundSelectedPort=true;
        }
      }
      if(!foundSelectedPort)
        response.addOutputMessage(defaultOutput, inputMsg);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error matching filter", this, inputMsg, e);
    }
  }
  
  /**
   * The implementation should determine for each received msg,
   * on which port it must be sent out.
   * <br/>
   * Remark that the actual output ports can be configured, 
   * so the logic of this method's implementation should be able to figure out
   * somehow which ports are present etc.
   * <br/>
   * Typically we assume that the logic is based on some kind of scripting that
   * can also be easily changed to match the configured output ports.
   * <p>
   * If the returned name is null or does not map to a known output port, the message will
   * be sent on the defaultOutput port.
   * </p>
   * 
   * @param availablePortNames
   * @param msg
   * @return the port name on which the message should be sent out
   */
  protected abstract String routeToPort(Collection<String> availablePortNames, ManagedMessage msg);
}
