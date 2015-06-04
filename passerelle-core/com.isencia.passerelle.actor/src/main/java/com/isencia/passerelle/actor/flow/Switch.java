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
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;

/**
 * @author Dirk
 */
public class Switch extends Actor {

  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(Switch.class);

  private List<Port> outputPorts = null;
  private PortHandler selectHandler = null;

  public Parameter numberOfOutputs = null;
  public Port input;
  public Port select = null;

  private int outputCount = 0;
  private int selected = 0;

  public Switch(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    numberOfOutputs = new Parameter(this, "count", new IntToken(1));
    numberOfOutputs.setTypeEquals(BaseType.INT);

    input = PortFactory.getInstance().createInputPort(this, null);
    input.setMultiport(false);
    select = PortFactory.getInstance().createInputPort(this, "select", Integer.class);
    input.setMultiport(false);

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"-2\" cy=\"-7\" r=\"4\""
        + "style=\"fill:black\"/>\n" + "<line x1=\"-15\" y1=\"-5\" x2=\"15\" y2=\"-5\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"-15\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"5\" "
        + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"10\" x2=\"0\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:gray\"/>\n"
        + "<line x1=\"0\" y1=\"10\" x2=\"0\" y2=\"-5\" " + "style=\"stroke-width:1.0;stroke:gray\"/>\n" + "</svg>\n");
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == numberOfOutputs) {
      int newOutputCount = ((IntToken) numberOfOutputs.getToken()).intValue();
      if (newOutputCount != outputCount) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("{} change number of outputs from {} to {}", new Object[] {getFullName(), outputCount, newOutputCount});
        }

        if (outputPorts == null) {
          outputPorts = new ArrayList<Port>(5);
          for (int i = 0; i < newOutputCount; i++) {
            try {
              Port outputPort = (Port) getPort("output " + i);
              if (outputPort == null) {
                outputPort = PortFactory.getInstance().createOutputPort(this, "output " + i);
              }
              outputPorts.add(i, outputPort);
            } catch (NameDuplicationException e) {
              throw new IllegalActionException(e.toString());
            }
          }
        } else if (newOutputCount < outputCount) {
          for (int i = outputCount - 1; (i >= 0) && (i >= newOutputCount); i--) {
            try {
              ((Port) outputPorts.get(i)).setContainer(null);
              outputPorts.remove(i);
            } catch (NameDuplicationException e) {
              throw new IllegalActionException(e.toString());
            }
          }
        } else if (newOutputCount > outputCount) {
          for (int i = outputCount; i < newOutputCount; i++) {
            try {
              Port outputPort = (Port) getPort("output " + i);
              if (outputPort == null) {
                outputPort = PortFactory.getInstance().createOutputPort(this, "output " + i);
              }
              outputPorts.add(i, outputPort);
            } catch (NameDuplicationException e) {
              throw new IllegalActionException(e.toString());
            }
          }
        }
        outputCount = newOutputCount;
        if (selected >= outputCount) {
          selected = outputCount - 1;
        }
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void doFire() throws ProcessingException {
    int outNr = 0;
    ManagedMessage msg = null;
    try {
      msg = MessageHelper.getMessage(input);
    } catch (PasserelleException e) {
      throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error getting msg from MessageHelper.getMessageAsToken()", this, e);
    }
    if (msg == null) {
      requestFinish();
    } else {
      outNr = selected;
      if (selected < 0) {
        outNr = 0;
      } else if (selected >= outputCount) {
        outNr = outputCount - 1;
      }
      getLogger().debug("{} Selected {}  Using port {}", selected, outNr);
      try {
        sendOutputMsg((Port) outputPorts.get(outNr), msg);
        getLogger().trace("{} has sent message on {}" + msg);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error sending output msg", this, msg, e);
      }
    }
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    if (select.getWidth() > 0) {
      selectHandler = createPortHandler(select, new PortListenerAdapter() {
        public void tokenReceived() {
          Token selectToken = selectHandler.getToken();
          if (selectToken != null && !selectToken.isNil()) {
            try {
              ManagedMessage msg = MessageHelper.getMessageFromToken(selectToken);
              selected = ((Number) msg.getBodyContent()).intValue();
            } catch (NumberFormatException e) {
              // Do nothing. selected is unchanged
            } catch (Exception e) {
              // Do nothing. selected is unchanged
              getLogger().error("", e);
            }
            getLogger().debug("{} Event received : {}", getFullName(), selected);
          }
        }
      });
      selectHandler.start();
    }
  }
}