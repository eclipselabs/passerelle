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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * Registers itself as an ErrorCollector.
 * <p>
 * For each received exception :
 * <ul>
 * <li>log the exception
 * <li>send out the exception msg via errorText output
 * <li>if the error context is a ManagedMessage, send it out via the messageInError output
 * </ul>
 * </p>
 * 
 * @author delerw
 */
public class ErrorObserver extends Actor implements ErrorCollector {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(ErrorObserver.class);

  private BlockingQueue<PasserelleException> errors = new LinkedBlockingQueue<PasserelleException>();

  /**
   * For each received exception/error, the error is sent out in a new Passerelle ManagedMessage via this output port.
   */
  public Port errorOutput;

  /**
   * For each received exception/error, the error text message is sent out via this output port.
   */
  public Port errorTextOutput;

  /**
   * For each received exception/error, if the error context is a ManagedMessage, send it out via the messageInError output
   */
  public Port messageInErrorOutput;

  public ErrorObserver(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    errorOutput = PortFactory.getInstance().createOutputPort(this, "errorMsg");
    errorTextOutput = PortFactory.getInstance().createOutputPort(this, "errorText");
    messageInErrorOutput = PortFactory.getInstance().createOutputPort(this, "messageInError");

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

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    getDirectorAdapter().addErrorCollector(this);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // ErrorReceiver has no data input ports,
    // so it's like a Source in the days of the original Actor API.
    // The BlockingQueue (errors) is our data feed.
    try {
      PasserelleException e = errors.poll(1, TimeUnit.SECONDS);
      if (e != null) {
        sendOutErrorInfo(response, e);
        drainErrorsQueueTo(response);
      }
    } catch (InterruptedException e) {
      // should not happen,
      // or if it does only when terminating the model execution
      // and with an empty queue, so we can just finish then
      requestFinish();
    }
  }

  private void sendOutErrorInfo(ProcessResponse response, PasserelleException e) throws ProcessingException {
    ManagedMessage errorMsg = createErrorMessage(e);
    response.addOutputMessage(errorOutput, errorMsg);

    ManagedMessage msg = null;
    if (e.getContext() instanceof ManagedMessage) {
      msg = (ManagedMessage) e.getContext();
      response.addOutputMessage(messageInErrorOutput, msg);
    }
    try {
      response.addOutputMessage(errorTextOutput, createMessage(e.getMessage(), "text/plain"));
    } catch (MessageException e1) {
      // should not happen, but...
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error generating error text output", this, msg, e1);
    }
  }

  private void drainErrorsQueueTo(ProcessResponse response) throws ProcessingException {
    while (!errors.isEmpty()) {
      PasserelleException e = errors.poll();
      if (e != null) {
        sendOutErrorInfo(response, e);
      } else {
        break;
      }
    }
  }
  
  @Override
  protected void triggerFirstIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }
  @Override
  protected void triggerNextIteration() throws IllegalActionException {
    // no unconditional triggering here, dude!
  }

  public void acceptError(PasserelleException e) {
    try {
      errors.put(e);
      super.triggerNextIteration();
    } catch (InterruptedException e1) {
      // should not happen,
      // or if it does only when terminating the model execution
      getLogger().error("Receipt interrupted for ", e);
    } catch (IllegalActionException e2) {
      getLogger().error("Failed to trigger next iteration ", e2);
      getLogger().error("Error received ", e);
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    getDirectorAdapter().removeErrorCollector(this);
    try {
      drainErrorsQueueTo(null);
    } catch (Exception e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error draining remaining error queue " + errors, this, e);
    }
    super.doWrapUp();
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
