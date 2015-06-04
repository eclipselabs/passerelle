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

package com.isencia.passerelle.testsupport.actor;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * 
 * Simple actor for testing, that mocks some work for each incoming msg,
 * by blocking for a configurable time.
 * 
 * @author erwin
 * 
 * @deprecated Just use the Delay actor that has similar functionality.
 *
 */
@SuppressWarnings("serial")
public class MockWorker extends Actor {
  
  public Parameter workTimeParameter;
  public Port input;
  public Port output;
  
  private boolean fireInterrupted;
  private boolean flowExecutionStopped;
  
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MockWorker(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
    
    workTimeParameter = new Parameter(this,"Work time (ms)", new IntToken(1000));
    registerConfigurableParameter(workTimeParameter);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    try {
      fireInterrupted = false;
      
      int workTime = ((IntToken)workTimeParameter.getToken()).intValue();
      if(workTime>1000) {
        // hmmm, lots of work to mock...
        // we'd better be ready for a possible work interruption!
        // let's sleep in slots of 100 ms, checking for interruptions inbetween
        long expectedWorkFinishedTime = System.currentTimeMillis() + workTime;
        while(!fireInterrupted && !flowExecutionStopped) {
          Thread.sleep(100);
          if(System.currentTimeMillis() >= expectedWorkFinishedTime) {
            break;
          }
        }
      } else {
        Thread.sleep(workTime);
      }
    } catch (IllegalActionException e) {
      throw new ProcessingException("Error reading work time value", msg, e);
    } catch (InterruptedException e) {
      // forget it, just let the Passerelle engine decide how to progress
    }
    response.addOutputMessage(output, request.getMessage(input));
  }
  
  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    fireInterrupted = false;
    flowExecutionStopped = false;
  }
  
  @Override
  protected void doStopFire() {
    super.doStopFire();
    fireInterrupted = true;
  }
  
  @Override
  protected void doStop() {
    super.doStop();
    flowExecutionStopped = true;
  }

}
