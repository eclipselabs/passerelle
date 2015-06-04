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
package com.isencia.passerelle.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Filter actors provide binary routing logic based on some boolean logic,
 * which must be provided by concrete actor implementations.
 * 
 * @author erwin
 */
public abstract class Filter extends Actor {
  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(Filter.class);

  /**
   * Holds the last received message
   */
  private ManagedMessage message = null;
  private Token token = null;

  /**
   * The input port. This base class imposes no type constraints except that the type of the input cannot be greater than the type of the outputOK.
   */
  public Port input;
  private PortHandler inputHandler = null;

  /**
   * The outputOK port. By default, the type of this port is constrained to be at least that of the input.
   */
  public Port outputOk;

  /**
   * The outputNOTOK port. By default, the type of this port is constrained to be at least that of the input.
   */
  public Port outputNotOk;

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container The container.
   * @param name The name of this actor.
   * @exception IllegalActionException If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException If the container already has an actor with this name.
   */
  public Filter(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    input = PortFactory.getInstance().createInputPort(this, null);
    outputOk = PortFactory.getInstance().createOutputPort(this, "outputOk");
    outputNotOk = PortFactory.getInstance().createOutputPort(this, "outputNotOk");

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"10\" y1=\"-15\" x2=\"10\" y2=\"-8\" "
        + "style=\"stroke-width:3.0\"/>\n" + "<line x1=\"10\" y1=\"8\" x2=\"10\" y2=\"15\" " + "style=\"stroke-width:3.0\"/>\n"
        + "<line x1=\"-10\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-3\" x2=\"15\" y2=\"0\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "<line x1=\"-10\" y1=\"-10\" x2=\"5\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"0\" y1=\"-13\" x2=\"5\" y2=\"-10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"0\" y1=\"-7\" x2=\"5\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-10\" y1=\"10\" x2=\"5\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"0\" y1=\"7\" x2=\"5\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"0\" y1=\"13\" x2=\"5\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    if (input.getWidth() > 0) {
      inputHandler = createPortHandler(input);
      inputHandler.start();
    } else {
      requestFinish();
    }
  }

  /**
   * @param msg
   * @return boolean indicating whether the message matches the filter
   * @throws FilterException
   */
  protected abstract boolean isMatchingFilter(Object msg) throws FilterException;

  protected boolean doPreFire() throws ProcessingException {
    boolean result = true;
    token = inputHandler.getToken();
    if (token == null) {
      message = null;
    } else if (!token.isNil()) {
      try {
        message = MessageHelper.getMessageFromToken(token);
      } catch (PasserelleException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Error getting message from input " + token, this, e);
      }
    } else {
      result = false;
    }
    return result && super.doPreFire();
  }

  public void doFire() throws ProcessingException {
    if (message != null) {
      notifyStartingFireProcessing();
      try {
        boolean matchFound = false;
        try {
          matchFound = isMatchingFilter(message);
        } catch (FilterException e) {
          getLogger().error("Error applying filter condition", e);
        }
        if (matchFound) {
          try {
            outputOk.broadcast(token);
          } catch (Exception e) {
            throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE,"Error sending msg on OK output", this, message, e);
          }
          getLogger().debug("{} - Sent message OK  {}:", getFullName(), token);
          getAuditLogger().debug("Sent message OK");
        } else {
          try {
            outputNotOk.broadcast(token);
          } catch (Exception e) {
            throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE,"Error sending msg on NotOK output", this, message, e);
          }
          getLogger().debug("{} - Sent message NOT OK  {}:", getFullName(), token);
          getAuditLogger().debug("Sent message NOT OK");
        }
      } finally {
        notifyFinishedFireProcessing();
      }
    } else {
      requestFinish();
    }
  }
}
