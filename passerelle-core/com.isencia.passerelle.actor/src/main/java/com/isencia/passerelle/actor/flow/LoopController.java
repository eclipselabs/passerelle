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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;

/**
 * An actor that can be configured with a loop count N, then forwards a received
 * input message N times, and does this in a synchronized way. I.e. the loop in
 * the model can send a feedback msg to trigger this loop controller to send the
 * data one more time, for the next loop iteration.
 * 
 * @author erwin
 */
public class LoopController extends Actor {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(LoopController.class);

  // input ports
  public Port countPort;
  public Port inputPort;
  public Port handledPort;
  // output port
  public Port outputPort;

  private boolean countPortExhausted = false;
  private boolean inputPortExhausted = false;
  private boolean handledPortExhausted = false;

  // Parameter
  public Parameter maxCountParam;
  // cfg via Parameter
  private int maxCount = 100;
  // maxCount or overridden by input msg content
  private int loopCount = maxCount;
  // actual index where the loop is currently
  private int currentLoopIndex = 0;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public LoopController(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    countPort = PortFactory.getInstance().createInputPort(this, "count", Integer.class);
    inputPort = PortFactory.getInstance().createInputPort(this, "input", null);
    handledPort = PortFactory.getInstance().createInputPort(this, "handled", null);
    outputPort = PortFactory.getInstance().createOutputPort(this, "output");

    maxCountParam = new Parameter(this, "Max Count", new IntToken(maxCount));
    maxCountParam.setTypeEquals(BaseType.INT);
    registerConfigurableParameter(maxCountParam);

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n"
        + "<line x1=\"10\" y1=\"0\" x2=\"7\" y2=\"-3\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"10\" y1=\"0\" x2=\"13\" y2=\"-3\" "
        + "style=\"stroke-width:2.0\"/>\n"
        + "</svg>\n");
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    countPortExhausted = !(countPort.getWidth() > 0);
    inputPortExhausted = false;
    handledPortExhausted = !(handledPort.getWidth() > 0);
    currentLoopIndex = 0;
    try {
      maxCount = ((IntToken) maxCountParam.getToken()).intValue();
      loopCount = maxCount;
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading maxCount", maxCountParam, e);
    }
  }

  protected void doFire() throws ProcessingException {
    ManagedMessage countMsg = null;
    ManagedMessage inputMsg = null;
    Long seqCount = null;
    // always read count first
    if (!countPortExhausted) {
      try {
        countMsg = MessageHelper.getMessage(countPort);
        if (countMsg != null) {
          if(getLogger().isDebugEnabled()) {
            getLogger().debug("{} doFire() - received {}", getFullName(), getAuditTrailMessage(countMsg, countPort));
          }
          Object content = countMsg.getBodyContent();
          if (content instanceof Number) {
            loopCount = ((Number) content).intValue();
          } else {
            // try to convert to a number
            try {
              loopCount = Integer.parseInt(content.toString());
            } catch (NumberFormatException e) {
              // ignore
            }
          }
          loopCount = (loopCount > maxCount) ? maxCount : loopCount;
        } else {
          countPortExhausted = true;
          getLogger().debug("{} doFire() - found exhausted port {}", countPort);
        }
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error reading msg content", this, countMsg, e);
      } catch (PasserelleException e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading from port", countPort, e);
      }
    }

    if (!inputPortExhausted) {
      try {
        inputMsg = MessageHelper.getMessage(inputPort);
        if (inputMsg != null) {
          getLogger().debug("{} doFire() - received {}", getFullName(), getAuditTrailMessage(inputMsg, inputPort));
          seqCount = MessageFactory.getInstance().createSequenceID();
        } else {
          inputPortExhausted = true;
          getLogger().debug("{} doFire() - found exhausted port {}",  inputPort.getName());
        }
      } catch (PasserelleException e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading from port", inputPort, e);
      }
    }
    if (inputMsg != null) {
      // send out first msg of the loop
      sendLoopData(inputMsg, null, seqCount, 0, (0 >= loopCount));
      // and now do the loop, each time after receiving a loop iteration
      // handled notification
      for (currentLoopIndex = 1; currentLoopIndex < loopCount; ++currentLoopIndex) {
        try {
          ManagedMessage handledMsg = null;
          // if the handled port would get exhausted
          // (or was not connected in the first place)
          // we just generate a rapid sequence of msgs
          if (!handledPortExhausted) {
            handledMsg = MessageHelper.getMessage(handledPort);
            if (handledMsg != null) {
              getLogger().debug("{} doFire() - received {}", getFullName(), getAuditTrailMessage(handledMsg, handledPort));
            } else {
              handledPortExhausted = true;
              getLogger().debug("{} doFire() - found exhausted port {}",  handledPort.getName());
            }
          }
          sendLoopData(inputMsg, handledMsg, seqCount, currentLoopIndex, (currentLoopIndex >= loopCount - 1));
        } catch (PasserelleException e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error reading from port", handledPort, e);
        }
      }
    }
    if (areAllInputsFinished()) {
      requestFinish();
    }
  }

  /**
   * @param inputMsg
   * @throws ProcessingException
   */
  private void sendLoopData(ManagedMessage inputMsg, ManagedMessage handledMsg, Long seqCount, int seqPos, boolean seqEnd) throws ProcessingException {
    ManagedMessage resultMsg = null;
    try {
      resultMsg = MessageFactory.getInstance().createMessageCopyInSequence(inputMsg, seqCount, new Long(seqPos), seqEnd);
      if (handledMsg != null) resultMsg.addCauseID(handledMsg.getID());
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating msg copy", this, inputMsg, e);
    }
    sendOutputMsg(outputPort, resultMsg);
  }

  /**
   * @return
   */
  private boolean areAllInputsFinished() {
    // just need to check the count and input ports
    // the handled port is no longer important when the 2 other are
    // exhausted anyway...
    return countPortExhausted && inputPortExhausted;
  }

  /**
   * @return the current index in the loop
   */
  public int getLoopCount() {
    return currentLoopIndex;
  }
}
