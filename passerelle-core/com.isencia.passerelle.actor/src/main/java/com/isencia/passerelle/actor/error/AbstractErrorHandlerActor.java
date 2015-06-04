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
package com.isencia.passerelle.actor.error;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.dynaport.OutputPortSetterBuilder;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.ext.ErrorHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageOutputContext;

/**
 * Base class for all kinds of ErrorHandler actors, without input ports.
 * The output ports are configurable, matching the different error cases that can be handled by the implemented actor.
 * 
 * @author erwin
 */
public abstract class AbstractErrorHandlerActor extends Actor implements ErrorHandler {
  private static final long serialVersionUID = 1L;

  public OutputPortSetterBuilder outputPortBuilder;

  private BlockingQueue<MessageOutputContext> bufferedErrorOutputs = new LinkedBlockingQueue<MessageOutputContext>();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public AbstractErrorHandlerActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    outputPortBuilder = new OutputPortSetterBuilder(this, "outputPortBldr");

    setDaemon(true);

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:red;stroke:red\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<circle cx=\"0\" cy=\"0\" r=\"10\" style=\"fill:white;stroke-width:2.0\"/>\n"
        + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  protected void setOutputPortNames(String... portNames) {
    outputPortBuilder.setOutputPortNames(portNames);
  }

  protected void addErrorOutput(MessageOutputContext errorOutput) {
    bufferedErrorOutputs.add(errorOutput);
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    super.doPreInitialize();
    bufferedErrorOutputs.clear();
  }

  @Override
  final protected synchronized void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
  }
  
  @Override
  protected boolean doPostFire() throws ProcessingException {
    boolean res = super.doPostFire();
    // This actor has no data input ports,
    // so it's like a Source in the days of the original Actor API.
    // The BlockingQueue (errors) is our data feed.
    try {
      MessageOutputContext errOutput = bufferedErrorOutputs.poll(1, TimeUnit.SECONDS);
      if (errOutput != null) {
        sendOutErrorInfo(null, errOutput);
        drainErrorsQueueTo(null);
      }
    } catch (InterruptedException e) {
      // should not happen,
      // or if it does only when terminating the model execution
      // and with an empty queue, so we can just finish then
      requestFinish();
    }
    return  res || !(bufferedErrorOutputs.isEmpty());
  }

  protected boolean sendErrorMsgOnwardsVia(String outputName, ManagedMessage msg, PasserelleException error) {
    boolean result = false;
    if (!isFinishRequested()) {
      Object outputPort = getPort(outputName);
      if (outputPort == null || !(outputPort instanceof Port)) {
        getLogger().error("Error in actor's ports",
            new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error finding port for range " + outputName, this, error));
      } else {
        try {
          addErrorOutput(new MessageOutputContext((Port) outputPort, msg));
          super.triggerNextIteration();
          result = true;
        } catch (IllegalActionException e2) {
          getLogger().error("Failed to trigger next iteration ", e2);
          getLogger().error("Error received ", error);
        }
      }
    }
    return result;
  }

  private void sendOutErrorInfo(ProcessResponse response, MessageOutputContext msgOutputCtxt) throws ProcessingException {
    if (response != null) {
      response.addOutputContext(msgOutputCtxt);
    } else {
      sendOutputMsg(msgOutputCtxt.getPort(), msgOutputCtxt.getMessage());
    }
  }

  private synchronized void drainErrorsQueueTo(ProcessResponse response) throws ProcessingException {
    while (!bufferedErrorOutputs.isEmpty()) {
      MessageOutputContext errOutput = bufferedErrorOutputs.poll();
      if (errOutput != null) {
        sendOutErrorInfo(response, errOutput);
      } else {
        break;
      }
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    try {
      drainErrorsQueueTo(null);
    } catch (Exception e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error draining remaining error queue " + bufferedErrorOutputs, this, e);
    }
    super.doWrapUp();
  }

  @Override
  protected void triggerFirstIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }

  @Override
  protected void triggerNextIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }
}
