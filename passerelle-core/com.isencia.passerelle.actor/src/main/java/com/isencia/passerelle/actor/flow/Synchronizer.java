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

import java.util.ArrayList;
import java.util.List;
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
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;

/**
 * An actor that synchronizes the messages on all input ports, and then sends them onwards via the corresponding output ports. It has one fixed input port, the
 * syncInput port which is multi-channel. This allows to resolve bootstrap issues for synchronized loops, e.g. combining the loop feedback msg and an ordinary
 * start-up trigger. Besides this port, it can have a configurable, equal nr of extra input and output ports. The extra input ports are all single-channel.
 * 
 * @author erwin
 */
public class Synchronizer extends Actor {
  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(Synchronizer.class);

  public static final String NUMBER_OF_PORTS = "Extra nr of ports";
  public static final String INPUTPORTPREFIX = "input";
  public static final String OUTPUTPORTPREFIX = "output";

  public Port syncInput = null;
  private PortHandler syncInputHandler = null;
  private List<Port> inputPorts = null;
  private List<Port> outputPorts = null;
  private List<Boolean> finishRequests = null;

  public Parameter numberOfPorts = null;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Synchronizer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    syncInput = PortFactory.getInstance().createInputPort(this, "syncInput", null);
    // Create the lists to which the ports can be added
    inputPorts = new ArrayList<Port>(5);
    outputPorts = new ArrayList<Port>(5);
    finishRequests = new ArrayList<Boolean>(5);

    // Create the parameters
    numberOfPorts = new Parameter(this, NUMBER_OF_PORTS, new IntToken(0));
    numberOfPorts.setTypeEquals(BaseType.INT);

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"15\" "
        + "style=\"stroke-width:3.0\"/>\n" +

        "<line x1=\"-15\" y1=\"0\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"-3\" x2=\"-1\" y2=\"0\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"3\" x2=\"-1\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-15\" y1=\"-10\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-5\" y1=\"-13\" x2=\"-1\" y2=\"-10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"-7\" x2=\"-1\" y2=\"-10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-15\" y1=\"10\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n"
        + "<line x1=\"-5\" y1=\"7\" x2=\"-1\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:red\"/>\n" + "<line x1=\"-5\" y1=\"13\" x2=\"-1\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:red\"/>\n" +

        "<line x1=\"1\" y1=\"0\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-3\" x2=\"15\" y2=\"0\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "<line x1=\"1\" y1=\"-10\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-13\" x2=\"15\" y2=\"-10\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"-7\" x2=\"15\" y2=\"-10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "<line x1=\"1\" y1=\"10\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"7\" x2=\"15\" y2=\"10\" "
        + "style=\"stroke-width:2.0;stroke:blue\"/>\n" + "<line x1=\"10\" y1=\"13\" x2=\"15\" y2=\"10\" " + "style=\"stroke-width:2.0;stroke:blue\"/>\n"
        + "</svg>\n");

  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
  
  protected void doInitialize() throws InitializationException {
    for (int i = 0; i < finishRequests.size(); ++i) {
      finishRequests.set(i, Boolean.FALSE);
    }
    syncInputHandler = createPortHandler(syncInput);
    if (syncInput.getWidth() > 0) {
      syncInputHandler.start();
    }
  }

  protected void doFire() throws ProcessingException {
    // just loop over all input ports
    // when we've passed all of them, this means
    // we've seen messages on all of them
    // and then we just need to forward the messages
    // on corresponding output ports
    isFiring = false;
    Token token = syncInputHandler.getToken();
    isFiring = true;

    if (token != null && !token.isNil()) {
      getLogger().debug("{} doFire() - received msg on {}", getFullName(), syncInput);
      int nrPorts = inputPorts.size();
      ManagedMessage[] messages = new ManagedMessage[nrPorts];
      for (int i = 0; i < nrPorts; ++i) {
        if (!((Boolean) finishRequests.get(i)).booleanValue()) {
          Port inputPort = (Port) inputPorts.get(i);
          try {
            ManagedMessage msg = MessageHelper.getMessage(inputPort);
            if (msg != null) {
              messages[i] = msg;
              getLogger().debug("{} doFire() - received {}", getFullName(), getAuditTrailMessage(msg, inputPort));
            } else {
              finishRequests.set(i, Boolean.TRUE);
              getLogger().debug("{} doFire() - found exhausted port {}", getFullName(), inputPort);
            }
          } catch (PasserelleException e) {
            throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error reading from port", inputPort, e);
          }
        }
      }
      for (int i = 0; i < nrPorts; ++i) {
        if (!((Boolean) finishRequests.get(i)).booleanValue()) {
          Port outputPort = (Port) outputPorts.get(i);
          ManagedMessage msg = messages[i];
          if (msg != null) {
            sendOutputMsg(outputPort, msg);
          }
        }
      }
      if (areAllInputsFinished()) {
        requestFinish();
      }
    } else {
      requestFinish();
    }
  }

  /**
   * @return
   */
  private boolean areAllInputsFinished() {
    boolean result = true;
    for (int i = 0; i < finishRequests.size(); ++i) {
      result = result && ((Boolean) finishRequests.get(i)).booleanValue();
    }
    return result;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    // Change numberOfOutputs
    if (attribute == numberOfPorts) {
      int nrPorts = inputPorts.size();
      int newPortCount = ((IntToken) numberOfPorts.getToken()).intValue();
      if (newPortCount < nrPorts) {
        for (int i = nrPorts - 1; i >= newPortCount; --i) {
          try {
            ((Port) inputPorts.get(i)).setContainer(null);
            inputPorts.remove(i);
            ((Port) outputPorts.get(i)).setContainer(null);
            outputPorts.remove(i);
            finishRequests.remove(i);
          } catch (NameDuplicationException e) {
            // should never happen for a setContainer(null)
          }
        }
      } else if (newPortCount > nrPorts) {
        for (int i = nrPorts; i < newPortCount; ++i) {
          try {
            String inputPortName = INPUTPORTPREFIX + i;
            String outputPortName = OUTPUTPORTPREFIX + i;
            // need this extra step because Ptolemy maintains and loads
            // duplicate stuff from the moml
            // both the nr-of-ports parameter is in there, and all dynamically
            // cfg ports themselves
            // and these might have been loaded before the attributeChanged is
            // invoked...
            Port extraInputPort = (Port) getPort(inputPortName);
            if (extraInputPort == null) {
              extraInputPort = PortFactory.getInstance().createInputPort(this, inputPortName, null);
              extraInputPort.setMultiport(false);
            }
            Port extraOutputPort = (Port) getPort(outputPortName);
            ;
            if (extraOutputPort == null) {
              extraOutputPort = PortFactory.getInstance().createOutputPort(this, outputPortName);
            }
            inputPorts.add(extraInputPort);
            outputPorts.add(extraOutputPort);
            finishRequests.add(Boolean.FALSE);
          } catch (NameDuplicationException e) {
            LOGGER.error("", e);
            throw new IllegalActionException(this, e, "Error for index " + i);
          }
        }
      }
      nrPorts = newPortCount;
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }
}
