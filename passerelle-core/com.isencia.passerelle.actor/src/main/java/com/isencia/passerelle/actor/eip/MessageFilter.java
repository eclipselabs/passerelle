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

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An abstract base class for message filter actors.
 * <p>
 * A Filter actor is an implementation of EIP's Message Filter pattern. 
 * An EIP Message Filter typically evaluates incoming messages according to some boolean condition. 
 * If the condition evaluates to true, the message is sent along in the sequence, and if it's false the message is dropped.
 * </p>
 * <p>
 * Whereas in the plain world-of-EIP a pure Message Filter is a useful concept, this is not the case in typical Passerelle use cases. 
 * Messages should not be dropped, but they should potentially be processed in alternative ways. 
 * For this purpose, filter actors typically provide two output ports : a MATCH and a NOMATCH. 
 * When the filter condition evaluates to true, the message is sent out via MATCH, if it's false it is sent out via NOMATCH.
 * </p>
 * <p>
 * Such a need is typically addressed in EIP by using a Message Router. 
 * A filter can be considered as a "degenerate" case of a router with only two options, controlled by a boolean condition. 
 * In the rare cases where "false" messages can be dropped, this just means that the NOMATCH output port will not be connected to a next actor.
 * </p>
 */
public abstract class MessageFilter extends Actor {
  private static final long serialVersionUID = 1L;
  public Port input;
  public Port outputMatch;
  public Port outputNoMatch;
  public StringParameter outputMatchPortNameParameter;
  public StringParameter outputNoMatchPortNameParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MessageFilter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // create the ports with their default names
    input = PortFactory.getInstance().createInputPort(this, null);
    outputMatch = PortFactory.getInstance().createOutputPort(this, "match");
    outputNoMatch = PortFactory.getInstance().createOutputPort(this, "noMatch");
    // provide parameters to be able to customize the output port names
    outputMatchPortNameParameter = new StringParameter(this,"Name for match output");
    outputMatchPortNameParameter.setExpression(outputMatch.getName());
    outputNoMatchPortNameParameter = new StringParameter(this,"Name for noMatch output");
    outputNoMatchPortNameParameter.setExpression(outputNoMatch.getName());
  }

  /**
   * Each time an actor attribute is changed, the actor is notified about this via this callback,
   * containing the changed attribute.
   * <br/>
   * By checking if the attribute is one of our defined parameters, the impact of the change
   * can be assessed and the actor can react accordingly, e.g. as in this case by renaming a port.
   */
  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if(outputMatchPortNameParameter==attribute) {
      // The port's display name is what is made visible in the graphical editors,
      // and what is used in the log files to trace message flows etc.
      // The normal name of a model element serves as unique identifier in a Passerelle model,
      // and is used to refer to an element when needed. 
      // E.g. a channel between two actors refers explicitly to the names of the linked output port and input port.
      // So changing it risks breaking a model's referential integrity
      // A port's display name can be freely changed, without breaking anything.
      outputMatch.setDisplayName(outputMatchPortNameParameter.getExpression());
      // To ensure that a changed display name is made visible in the graph editor,
      // this call is required.
      outputMatch.propagateValues();
    } else if(outputNoMatchPortNameParameter==attribute) {
      outputNoMatch.setDisplayName(outputNoMatchPortNameParameter.getExpression());
      outputNoMatch.propagateValues();
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }
  
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage inputMsg = request.getMessage(input);
    try {
      if(isMatchingFilter(inputMsg)) {
        response.addOutputMessage(outputMatch, inputMsg);
      } else {
        response.addOutputMessage(outputNoMatch, inputMsg);
      }
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error matching filter", this, inputMsg, e);
    }
  }
  
  /**
   * 
   * @param msg the message to be filtered
   * @return true/false depending on whether the implemented filter condition matches the message or not
   * 
   * @throws Exception
   */
  protected abstract boolean isMatchingFilter(ManagedMessage msg) throws Exception;

}
