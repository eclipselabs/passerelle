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
package com.isencia.passerelle.actor.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;

/**
 * Converts a series of incoming into an outgoing message containing an array
 * with the contents of the incoming msgs. The array is generated when a trigger
 * msg is received. Remark that this actor can not guarantee exact array
 * contents in the standard PN domain (or Passerelle's extension of it), as
 * there's no certainty on the order in which data msgs and the trigger may
 * arrive. For tighter control, use message sequences as input together with the
 * SequenceToArrayConverter.
 * 
 * @author erwin
 */
public class MessagesToArrayConverter extends Transformer {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(MessagesToArrayConverter.class);

  public final static String TRIGGER_PORT = "trigger";

  public Port trigger = null;
  private boolean triggerConnected = false;
  private PortHandler triggerHandler = null;
  private List<ManagedMessage> msgQueue = new ArrayList<ManagedMessage>();

  /**
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public MessagesToArrayConverter(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    trigger = PortFactory.getInstance().createInputPort(this, TRIGGER_PORT, null);
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    msgQueue.clear();
    triggerConnected = trigger.getWidth() > 0;
    if (triggerConnected) {
      triggerHandler = createPortHandler(trigger, new PortListener() {
        public void tokenReceived() {
          getLogger().trace("{} tokenReceived() - entry - received trigger", getFullName());
          try {
            ManagedMessage triggerMsg = MessageHelper.getMessageFromToken(triggerHandler.getToken());
            if (triggerMsg != null) {
              try {
                flushQueue();
              } catch (MessageException e) {
                try {
                  sendErrorMessage(e);
                } catch (IllegalActionException e1) {
                  // can't do much more...
                  LOGGER.error("Error sending error msg", e1);
                }
              } catch (ProcessingException e) {
                try {
                  sendErrorMessage(e);
                } catch (IllegalActionException e1) {
                  // can't do much more...
                  LOGGER.error("Error sending error msg", e1);
                }
              }
            }
          } catch (PasserelleException e) {
            LOGGER.error("Error getting message from trigger port", e);
          }
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{} tokenReceived() - exit", getFullName());
          }
        }
        public void noMoreTokens() {
          LOGGER.trace("{} trigger exhausted", getFullName());
        }
      });
      triggerHandler.start();
    }
  }

  /**
   * Returns the triggerConnected.
   * 
   * @return boolean
   */
  public boolean isTriggerConnected() {
    return triggerConnected;
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    if (isTriggerConnected()) {
      msgQueue.add(message);
      getLogger().debug("{} queued message {}", getFullName(), getAuditTrailMessage(message, null));
    } else {
      sendOutputMsg(output, message);
    }
  }

  protected void doWrapUp() throws TerminationException {
    try {
      // one last chance to get any pending msgs out
      flushQueue();
    } catch (Exception e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error flushing queue", this, e);
    }
    super.doWrapUp();
  }

  private void flushQueue() throws MessageException, ProcessingException {
    getLogger().debug("{} flushQueue() - entry", getFullName());
    if (!msgQueue.isEmpty()) {
      ManagedMessage resultMsg = createMessage();
      List<Object> msgBodies = new ArrayList<Object>();
      for (Iterator<ManagedMessage> iter = msgQueue.iterator(); iter.hasNext();) {
        ManagedMessage msg = (ManagedMessage) iter.next();
        resultMsg.addCauseID(msg.getID());
        msgBodies.add(msg.getBodyContent());
      }
      resultMsg.setBodyContent(msgBodies.toArray(), ManagedMessage.objectContentType);
      msgQueue.clear();
      sendOutputMsg(output, resultMsg);
    }
    getLogger().debug("{} flushQueue() - exit", getFullName());
  }
}
