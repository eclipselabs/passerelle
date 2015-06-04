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
package com.isencia.passerelle.actor.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * @author dirk jacobs
 */
public class Timer extends Actor {

  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(Timer.class);

  public Parameter timeParameter = null;
  public Port setInputPort = null;
  public Port resetInputPort = null;
  public Port outputPort = null;

  private int time = 0;
  private PortHandler setHandler = null;
  private PortHandler resetHandler = null;

  private boolean setPresent = false;
  private boolean set = false;
  private boolean reset = false;

  public Timer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    timeParameter = new Parameter(this, "time", new IntToken(10));
    timeParameter.setTypeEquals(BaseType.INT);
    registerConfigurableParameter(timeParameter);

    setInputPort = PortFactory.getInstance().createInputPort(this, "set", null);
    resetInputPort = PortFactory.getInstance().createInputPort(this, "reset", null);
    outputPort = PortFactory.getInstance().createOutputPort(this, "output");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"

        + "<circle cx=\"0\" cy=\"0\" r=\"16\"" + "style=\"fill:white\"/>\n" + "<line x1=\"0\" y1=\"-14\" x2=\"0\" y2=\"-12\"/>\n"
        + "<line x1=\"0\" y1=\"12\" x2=\"0\" y2=\"14\"/>\n" + "<line x1=\"-14\" y1=\"0\" x2=\"-12\" y2=\"0\"/>\n"
        + "<line x1=\"12\" y1=\"0\" x2=\"14\" y2=\"0\"/>\n" + "<line x1=\"0\" y1=\"-7\" x2=\"0\" y2=\"0\"/>\n"
        + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\"/>\n" + "</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);

    if (attribute == timeParameter) {
      time = ((IntToken) timeParameter.getToken()).intValue() * 1000;
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();

    // If something connected to the set port, install a handler
    if (setInputPort.getWidth() > 0) {

      setHandler = createPortHandler(setInputPort, new PortListenerAdapter() {
        public void tokenReceived() {
          getLogger().debug("{} - Set Event received", getFullName());

          Token token = setHandler.getToken();
          if (token != null && !token.isNil()) {
            set = true;
            performNotify();
          }
        }
      });
      if (setHandler != null) {
        setPresent = true;
        setHandler.start();
      }
    }
    // If something connected to the reset port, install a handler
    if (resetInputPort.getWidth() > 0) {
      resetHandler = createPortHandler(resetInputPort, new PortListenerAdapter() {
        public void tokenReceived() {
          Token token = resetHandler.getToken();
          getLogger().debug("{} - Reset Event received", getFullName());

          if (token != null && !token.isNil()) {
            reset = true;
            performNotify();
          }
        }
      });
      if (resetHandler != null) {
        resetHandler.start();
      }
    }

  }

  private synchronized void performNotify() {
    notify();
  }

  private synchronized void performWait(int time) {
    try {
      if (time == -1)
        wait();
      else
        wait(time);
    } catch (InterruptedException e) {
      requestFinish();
    }
  }

  protected void doFire() throws ProcessingException {
    if (!setPresent) {
      performWait(time);
      if (reset || isFinishRequested()) {
        reset = false;
        return;
      }
    } else {
      // Wait until set
      isFiring = false;
      while (!set && !isFinishRequested()) {
        performWait(-1);
      }

      if (isFinishRequested())
        return;

      isFiring = true;
      set = false;
      performWait(time);
      if (reset || isFinishRequested()) {
        reset = false;
        return;
      }
    }

    // Send trigger message
    ManagedMessage message = createTriggerMessage();
    try {
      message.setBodyContent(Long.toString(time), "text/plain");
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output msg", this, message, e);
    }
    sendOutputMsg(outputPort, message);
  }

  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    return "generated timed trigger.";
  }

  protected String getExtendedInfo() {
    return "period: " + time + " s";
  }

  protected void doStopFire() {
    performNotify();
  }
}