/* Copyright 2012 - iSencia Belgium NV

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
package com.isencia.passerelle.process.actor;

import java.util.concurrent.LinkedBlockingDeque;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
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
 * An actor that maintains a history of received msgs in a stack with optionally limited capacity.
 * <p> 
 * For a case with limited capacity, when all capacity is used,
 * the oldest message is dropped when a new one arrives.
 * </p>
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class MessageHistoryStack extends Actor {

  public Port input;
  public Parameter historySizeParameter;
  
  private LinkedBlockingDeque<ManagedMessage> messageHistory;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MessageHistoryStack(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);

    historySizeParameter = new Parameter(this, "History size");
    historySizeParameter.setTypeEquals(BaseType.INT);
  }
  
  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    
    if(messageHistory!=null) {
      // This can happen when a model is executed multiple times,
      // then we need to ensure that the history is "new" for each execution!
      // Remark that for a "normal" actor, resource clean-up is done in doWrapup().
      // But as the goal of this actor is to be able to consult message history,
      // also after model termination (e.g. in test scripts),
      // cleanup in wrapup phase would not be particularly useful...
      messageHistory.clear();
      messageHistory = null;
    }
    try {
      if(historySizeParameter.getToken()!=null) {
        int capacity = ((IntToken)historySizeParameter.getToken()).intValue();
        if (capacity>0) {
          // only when a positive size is set, do we use this to limit history size
          messageHistory = new LinkedBlockingDeque<ManagedMessage>(capacity);
        }
      }
      if(messageHistory==null) {
        messageHistory = new LinkedBlockingDeque<ManagedMessage>();
      }
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error constructing message history deque", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    if(messageHistory.remainingCapacity()<1) {
      messageHistory.pollLast();
    }
    messageHistory.push(msg);
  }
  
  /**
   * Retrieves and removes the most recently received message from the stored history, 
   * or returns null if the history is empty.
   * 
   * @return
   */
  public ManagedMessage poll() {
    return messageHistory.poll();
  }
}
