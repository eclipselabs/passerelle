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

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;

/**
 * Keeps track of all sequences for which messages pass through it:
 * <ul>
 * <li>each message in a sequence is maintained in cache until a feedback message arrives via the <code>handled</code> input port for that message
 * <li>when a sequence has completely been handled, the tracker generates a corresponding notification msg via its <code>seqFinished</code> output port.
 * </ul>
 * 
 * @author erwin
 */
public class SequenceTracker extends Transformer {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(SequenceTracker.class);

  private Map<Long, SequenceTrace> sequences = new HashMap<Long, SequenceTrace>();
  // flag to catch race conditions between threads of handled and input message
  // processing
  private boolean seqFinishedMsgPending = false;

  // input port via which we receive feedback
  // about messages that have been handled in the model
  public Port handled;
  private PortHandler handledHandler = null;

  // output port via which we send out
  // notifications that a certain sequence has been
  // completely handled
  public Port seqFinished;

  /**
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public SequenceTracker(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
    handled = PortFactory.getInstance().createInputPort(this, "handledMsg", null);
    seqFinished = PortFactory.getInstance().createOutputPort(this, "seqFinished");
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    sequences.clear();
    seqFinishedMsgPending = false;

    handledHandler = createPortHandler(handled, new PortListener() {
      public void tokenReceived() {
        Token handledToken = handledHandler.getToken();
        try {
          ManagedMessage message = MessageHelper.getMessageFromToken(handledToken);
          if (message != null)
            acceptHandledMessage(message);
        } catch (PasserelleException e) {
          LOGGER.error("Error getting message from handled port", e);
        }
      }
      public void noMoreTokens() {
        LOGGER.trace("{} handled port exhausted", getFullName());
      }
    });

    if (handled.getWidth() > 0) {
      handledHandler.start();
    } else {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "handled port not connected", this, null);
    }
  }

  /**
   * @param message
   * @throws IllegalActionException
   */
  protected void acceptHandledMessage(ManagedMessage message) {
    SequenceTrace seqTrace = (SequenceTrace) sequences.get(message.getSequenceID());
    if (seqTrace == null) {
      // notify our director about the problem
      getDirectorAdapter().reportError(this, new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Received message feedback for unknown sequence " + message.getSequenceID(), this, message, null));
    } else {
      seqTrace.messageHandled(message);
      boolean seqCompletelyFinished = seqTrace.isHandled();
      if (seqCompletelyFinished) {
        seqFinishedMsgPending = true;
        try {
          sequences.remove(message.getSequenceID());
          ManagedMessage seqFinishedNotification = createTriggerMessage();
          seqFinishedNotification.setSequenceID(message.getSequenceID());
          seqFinishedNotification.setSequenceEnd(true);

          sendOutputMsg(seqFinished, seqFinishedNotification);
        } catch (ProcessingException e) {
          try {
            sendErrorMessage(e);
          } catch (IllegalActionException e1) {
            // can't do much more...
            LOGGER.error("Error sending error msg", e1);
          }
        } finally {
          seqFinishedMsgPending = false;
        }
      }
    }
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    try {
      if (message.isPartOfSequence()) {
        SequenceTrace seqTrace = (SequenceTrace) sequences.get(message.getSequenceID());
        if (seqTrace == null) {
          seqTrace = new SequenceTrace(message.getSequenceID());
          sequences.put(seqTrace.getSequenceID(), seqTrace);
        }
        seqTrace.addMessage(message);
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error processing msg", this, message, e);
    }
    sendOutputMsg(output, message);
  }

  protected boolean doPostFire() throws ProcessingException {
    // as this actor is typically used in loop-like constructs
    // the handled port will probably not get exhausted by itself
    // (it will receive a feedback relation from an actor that's
    // getting its input from the output of this one)
    // so we need another criterium to determine the end of this actor's
    // meaningful lifetime: e.g. whether there are still sequences active
    // return super.doPostFire() || handledInputStillAlive;
    return super.doPostFire() || (handled.getWidth() > 0 && (!sequences.isEmpty() || seqFinishedMsgPending));
  }
}
