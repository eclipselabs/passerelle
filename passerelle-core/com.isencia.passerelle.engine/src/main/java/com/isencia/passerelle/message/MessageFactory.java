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
package com.isencia.passerelle.message;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.internal.ErrorMessageContainer;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.TriggerMessageContainer;

/**
 * MessageFactory TODO: class comment
 * 
 * @author erwin
 */
public class MessageFactory {

  private static final MessageFactory instance = new MessageFactory();
  private static long msgIdCounter = 0;
  private static long seqIDCounter = 0;

  public final static DateFormat timestampFormat = DateFormat.getDateTimeInstance();

  public static MessageFactory getInstance() {
    return instance;
  }

  public synchronized ManagedMessage createMessage() {
    MessageContainer newMsg = new MessageContainer();
    setStdHeaders(newMsg);

    return newMsg;
  }

  /**
   * Create a new msg and set addition header info. The headerProps map should contain string keys, matching the ManagedMessage.SystemHeader constants, and
   * string values. Any other entries are ignored. HEADER_ID, HEADER_VERSION, HEADER_TIMESTAMP_CREATION can not be overwritten though.
   * 
   * @param headerProps
   * @return
   */
  public synchronized ManagedMessage createMessage(Map headerProps) {
    MessageContainer newMsg = (MessageContainer) createMessage();
    setHeaders(newMsg, headerProps);
    return newMsg;
  }

  /**
   * Set the given header properties. HEADER_ID, HEADER_VERSION, HEADER_TIMESTAMP_CREATION can not be overwritten though.
   * 
   * @param newMsg
   * @param headerProps
   */
  private void setHeaders(MessageContainer newMsg, Map headerProps) {
    if (headerProps != null && headerProps.size() > 0) {
      Iterator hdrItr = headerProps.entrySet().iterator();
      while (hdrItr.hasNext()) {
        Map.Entry hdrEntry = (Map.Entry) hdrItr.next();
        try {
          String key = (String) hdrEntry.getKey();
          String value = (String) hdrEntry.getValue();
                    if(!(ManagedMessage.SystemHeader.HEADER_ID.equals(key)
                      || ManagedMessage.SystemHeader.HEADER_VERSION.equals(key)
                      || ManagedMessage.SystemHeader.HEADER_TIMESTAMP_CREATION.equals(key))) {
            newMsg.setHeader(key, value);
          }
        } catch (ClassCastException e) {
          // ignore
        }
      }
    }
  }

  public synchronized Long createSequenceID() {
    return new Long(seqIDCounter++);
  }

  public synchronized ManagedMessage createCorrelatedMessage(String correlationID) {
    MessageContainer newMsg = (MessageContainer) createMessage();
    newMsg.setHeader(ManagedMessage.SystemHeader.HEADER_CORRELATION_ID, correlationID);

    return newMsg;
  }

  public synchronized ManagedMessage createCorrelatedMessage(String correlationID, Map headerProps) {
    MessageContainer newMsg = (MessageContainer) createMessage(headerProps);
    newMsg.setHeader(ManagedMessage.SystemHeader.HEADER_CORRELATION_ID, correlationID);

    return newMsg;
  }

  public synchronized ManagedMessage createMessageInSequence(Long seqID, Long seqPos, boolean isSeqEnd) {
    MessageContainer newMsg = (MessageContainer) createMessage();
    newMsg.setSequenceID(seqID);
    newMsg.setSequencePosition(seqPos);
    newMsg.setSequenceEnd(isSeqEnd);

    return newMsg;
  }

  /**
   * Create a new msg and set addition header info. The headerProps map should contain string keys, matching the ManagedMessage.SystemHeader constants, and
   * string values. Any other entries are ignored. HEADER_ID, HEADER_VERSION, HEADER_TIMESTAMP_CREATION can not be overwritten though.
   * 
   * @param seqID
   * @param seqPos
   * @param isSeqEnd
   * @param headerProps
   * @return
   */
  public synchronized ManagedMessage createMessageInSequence(Long seqID, Long seqPos, boolean isSeqEnd, Map headerProps) {
    MessageContainer newMsg = (MessageContainer) createMessage(headerProps);
    newMsg.setSequenceID(seqID);
    newMsg.setSequencePosition(seqPos);
    newMsg.setSequenceEnd(isSeqEnd);

    return newMsg;
  }

  public synchronized ManagedMessage createErrorMessage(PasserelleException e) {
    ErrorMessageContainer newMsg = new ErrorMessageContainer(e);
    setStdHeaders(newMsg);
    return newMsg;
  }

  public synchronized ManagedMessage createErrorMessage(PasserelleException e, Map headerProps) {
    ErrorMessageContainer newMsg = (ErrorMessageContainer) createErrorMessage(e);
    setHeaders(newMsg, headerProps);

    return newMsg;
  }

  public synchronized ManagedMessage createTriggerMessage() {
    TriggerMessageContainer newMsg = new TriggerMessageContainer();
    setStdHeaders(newMsg);
    return newMsg;
  }

  public synchronized ManagedMessage createTriggerMessage(Map headerProps) {
    TriggerMessageContainer newMsg = (TriggerMessageContainer) createTriggerMessage();
    setHeaders(newMsg, headerProps);
    return newMsg;
  }

  /**
   * Create copied message, with copied headers and body without any change in values.
   * 
   * @param msg
   * @return
   * @throws MessageException
   */
  public synchronized ManagedMessage copyMessage(ManagedMessage msg) throws MessageException {
    if (msg != null && msg instanceof MessageContainer) {
      MessageContainer newMsg = ((MessageContainer) msg).copy();
      return newMsg;
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Create a new message, with new message ID but with copied values for other headers and for body. Indicate that this new message was caused by the original
   * msg.
   * 
   * @param msg
   * @return
   * @throws MessageException
   */
  public synchronized ManagedMessage createCausedCopyMessage(ManagedMessage msg) throws MessageException {
    if (msg != null && msg instanceof MessageContainer) {
      MessageContainer newMsg = ((MessageContainer) msg).copy();
      setStdHeaders(newMsg);
      newMsg.addCauseID(msg.getID());
      return newMsg;
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Create a new message with given sequence info and new std system headers, but for the rest
   * a copy of the original msg's data.
   * @param msg
   * @param seqID
   * @param seqPos
   * @param isSeqEnd
   * @return
   * @throws MessageException
   */
  public synchronized ManagedMessage createMessageCopyInSequence(ManagedMessage msg, Long seqID, Long seqPos, boolean isSeqEnd) throws MessageException {
    MessageContainer newMsg = (MessageContainer) copyMessage(msg);
    setStdHeaders(newMsg);
    newMsg.addCauseID(msg.getID());
    newMsg.setSequenceID(seqID);
    newMsg.setSequencePosition(seqPos);
    newMsg.setSequenceEnd(isSeqEnd);
    return newMsg;
  }

  /**
   * Create a new message with given sequence info, and for the rest a complete clone/copy
   * of the original msg's data, except increasing the version nr by 1.
   * @param msg
   * @param seqID
   * @param seqPos
   * @param isSeqEnd
   * @return
   * @throws MessageException
   */
  public synchronized ManagedMessage createMessageCloneInSequence(ManagedMessage msg, Long seqID, Long seqPos, boolean isSeqEnd) throws MessageException {
    MessageContainer newMsg = (MessageContainer) copyMessage(msg);
    setStdHeaders(newMsg);
    newMsg.addCauseID(msg.getID());
    newMsg.setSequenceID(seqID);
    newMsg.setSequencePosition(seqPos);
    newMsg.setSequenceEnd(isSeqEnd);
    return newMsg;
  }

  private void setStdHeaders(MessageContainer newMsg) {
    newMsg.setHeader(ManagedMessage.SystemHeader.HEADER_ID, Long.toString(msgIdCounter++));
    newMsg.setHeader(ManagedMessage.SystemHeader.HEADER_VERSION, "1");
    newMsg.setHeader(ManagedMessage.SystemHeader.HEADER_TIMESTAMP_CREATION, timestampFormat.format(new Date()));
  }

}
