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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;

/**
 * Keeps track of all sequences for which messages pass through it. Each message
 * in a sequence is maintained in cache until its complete sequence has passed.
 * Then the sequence is sent out in the form of a message containing an array of
 * all original msg contents. So the msg contents in a sequence had better been
 * of the same type!
 * 
 * @author erwin
 */
public class SequenceToArrayConverter extends Transformer {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(SequenceToArrayConverter.class);

  private Map<Long, SequenceTrace> sequences = new HashMap<Long, SequenceTrace>();

  /**
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public SequenceToArrayConverter(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    sequences.clear();
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    if (message.isPartOfSequence()) {
      SequenceTrace seqTrace = (SequenceTrace) sequences.get(message.getSequenceID());
      if (seqTrace == null) {
        seqTrace = new SequenceTrace(message.getSequenceID());
        sequences.put(seqTrace.getSequenceID(), seqTrace);
      }
      seqTrace.addMessage(message);
      if (seqTrace.isComplete()) {
        ManagedMessage resultMsg = null;
        try {
          ManagedMessage[] msgsInSeq = seqTrace.getMessagesInSequence();
          resultMsg = createMessage();
          List<Object> msgBodies = new ArrayList<Object>();
          for (int i = 0; i < msgsInSeq.length; i++) {
            ManagedMessage msg = msgsInSeq[i];
            resultMsg.addCauseID(msg.getID());
            msgBodies.add(msg.getBodyContent());
          }
          resultMsg.setBodyContent(msgBodies.toArray(), ManagedMessage.objectContentType);
          seqTrace.clear();
          sequences.remove(seqTrace.getSequenceID());
        } catch (MessageException e) {
          throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating result msg for completed sequence "+seqTrace.getSequenceID(), this, e);
        }
        sendOutputMsg(output, resultMsg);
      }
    } else {
      sendOutputMsg(output, message);
    }
  }
}
