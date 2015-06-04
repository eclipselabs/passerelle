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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

/**
 * This actor expects an array of objects as message payload. It generates a
 * sequence of messages, each containing an individual entry of the incoming
 * array. If the incoming message does not contain an array, it is sent out
 * unmodified.
 * 
 * @author erwin
 */
public final class ArrayToSequenceConverter extends Transformer {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(ArrayToSequenceConverter.class);

  /**
   * @param container
   * @param name
   * @throws NameDuplicationException
   * @throws IllegalActionException
   */
  public ArrayToSequenceConverter(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  protected void doFire(ManagedMessage message) throws ProcessingException {
    try {
      Object content = message.getBodyContent();
      try {
        Object[] contentAsArray = (Object[]) content;
        Long seqID = MessageFactory.getInstance().createSequenceID();
        for (int i = 0; i < contentAsArray.length; ++i) {
          ManagedMessage elementMsg = MessageFactory.getInstance().createMessageInSequence(seqID, new Long(i), (i >= contentAsArray.length - 1),
              getStandardMessageHeaders());
          elementMsg.addCauseID(message.getID());
          elementMsg.setBodyContent(contentAsArray[i], ManagedMessage.objectContentType);
          sendOutputMsg(output, elementMsg);
        }
      } catch (ClassCastException e) {
        // it's not an array, so just send it out unmodified
        sendOutputMsg(output, message);
      }
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "", this, message, e);
    }
  }
}
