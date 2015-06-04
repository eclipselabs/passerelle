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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;

/**
 * Simple actor for testing, that mocks some work for each incoming msg,
 * by blocking for a configurable delay and then just forwarding the received msg.
 * The delay is split in a sequence of segments of 100ms. 
 * Inbetween segments, the actor checks if a shutdown has been requested.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class Delay extends Forwarder {
  private final static Logger LOGGER = LoggerFactory.getLogger(Delay.class);

  public Parameter timeParameter = null;
  
  private boolean fireInterrupted;
  private boolean flowExecutionStopped;

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container The container.
   * @param name The name of this actor.
   * @exception IllegalActionException If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException If the container already has an actor with this name.
   */
  public Delay(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    timeParameter = new Parameter(this, "time(ms)", new IntToken(1000));
    timeParameter.setTypeEquals(BaseType.INT);
    registerConfigurableParameter(timeParameter);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    fireInterrupted = false;
    try {
      int time = ((IntToken)timeParameter.getToken()).intValue();
      if (time > 0) {
        int count = time/100;
        for (int i = 0; i < count; ++i) {
          Thread.sleep(100);
          if (isFinishRequested() || fireInterrupted || flowExecutionStopped) {
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      // do nothing, means someone wants us to stop
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error in delay processing", this, e);
    }

    super.process(ctxt, request, response);
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

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
}
