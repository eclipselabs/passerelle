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
import ptolemy.kernel.CompositeEntity;
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
import com.isencia.passerelle.message.internal.MessageContainer;

/**
 * This actor supports retry-loop constructions in a Passerelle sequence.
 * <p>
 * A maximal retry count can be configured. Each incoming message is sent out on one of 2 alternative output ports:
 * <ul>
 * <li>when the message has been received N times with N less-or-equal the configured retry count, it is sent out via
 * <code>retryOutput</code>.
 * <li>when it has been received more times than the configured retry count, it gets sent out via
 * <code>noRetryOutput</code>.
 * </ul>
 * </p>
 * <p>
 * The retry management is implemented by adding a named counter value as a message header. A message sent out via the
 * <code>retryOutput</code> should be sent back to the <code>input</code> port when a retry is desired. As it will
 * probably first have passed through one or more other actors, care must be taken to ensure that the message's headers
 * do not get lost/corrupted. Otherwise, e.g. if the counter header gets lost, there is a risk for eternal loops!
 * </p>
 * 
 * @author erwin
 */
public class RetryLoopActor extends Actor {
  private static final long serialVersionUID = 1L;
  private final Logger LOGGER = LoggerFactory.getLogger(RetryLoopActor.class);

  public Port input;
  public Port retryOutput;
  public Port noRetryOutput;

  public Parameter retryCountParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public RetryLoopActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    retryOutput = PortFactory.getInstance().createOutputPort(this, "retryOutput");
    noRetryOutput = PortFactory.getInstance().createOutputPort(this, "noRetryOutput");

    retryCountParameter = new Parameter(this, "Max retry count", new IntToken(3));

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"0\" cy=\"0\" r=\"10\""
        + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    MessageContainer msg = (MessageContainer) request.getMessage(input);
    String[] counterHdrValues = msg.getHeader(getFullName() + ".counter");
    int counter = 0;
    if (counterHdrValues != null && counterHdrValues.length > 0) {
      String counterHdrValue = counterHdrValues[0];
      try {
        counter = Integer.parseInt(counterHdrValue) + 1;
      } catch (NumberFormatException e) {
        // should not happen, means someone tampered with the header!!
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid counter header value " + counterHdrValue, this, msg, e);
      }
    }
    msg.setHeader(getFullName() + ".counter", Integer.toString(counter));
    int maxCounterValue = 0;
    try {
      maxCounterValue = ((IntToken) retryCountParameter.getToken()).intValue();
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error getting max counter value", this, msg, e);
    }
    if (counter < maxCounterValue) {
      response.addOutputMessage(retryOutput, msg);
    } else {
      response.addOutputMessage(noRetryOutput, msg);
    }
  }
}
