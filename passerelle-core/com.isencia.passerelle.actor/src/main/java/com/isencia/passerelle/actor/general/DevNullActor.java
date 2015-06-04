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
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An actor that discards all incoming msgs. Useful to e.g. link to an
 * error-output of one or more actors, to implement an "ignore error" behaviour.
 * It can be configured to completely ignore all incoming messages, or
 * alternatively to just log them in the log files.
 * 
 * @author delerw
 */
public class DevNullActor extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(DevNullActor.class);

  public Port input;
  public Parameter logReceivedMessages;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DevNullActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);

    logReceivedMessages = new Parameter(this, "Log received messages", new BooleanToken(false));
    logReceivedMessages.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(logReceivedMessages, "checkbox");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:white;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n" +

        "<rect x=\"-6\" y=\"-5\" width=\"12\" height=\"15\" style=\"fill:lightgrey;stroke:grey\"/>\n"
        + "<line x1=\"-1\" y1=\"-9\" x2=\"1\" y2=\"-9\" style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"-7\" y1=\"-7\" x2=\"7\" y2=\"-7\" style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-2\" y1=\"-5\" x2=\"-2\" y2=\"10\" style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"2\" y1=\"-5\" x2=\"2\" y2=\"10\" style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-5\" y1=\"12\" x2=\"5\" y2=\"12\" style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
  
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      if (((BooleanToken) logReceivedMessages.getToken()).booleanValue()) {
        ManagedMessage msg = request.getMessage(input);
        getLogger().info("{} Discarding msg {}",getFullName(), msg);
      }
    } catch (IllegalActionException e) {
      getLogger().error("Error reading parameter value", e);
    }
  }

}
