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
package com.isencia.passerelle.message.internal.sequence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import com.isencia.passerelle.message.ManagedMessage;


/**
 * Keeps track if a sequence has been "handled" in a Passerelle model.
 * The trace assumes that messages will be added in sequence, i.e.
 * in the right order and with all slots filled consecutively.
 * 
 * @author erwin
 */
public class SequenceTrace implements Traceable {

	private Long sequenceID;
	private Boolean handled=null;
	private Boolean complete=null;
	private Map<Long, MessageTrace> messageTraces = new HashMap<Long, MessageTrace>();
	private Map<Long, ManagedMessage> messagesBySeqPos = new TreeMap<Long, ManagedMessage>();

	/**
	 * @throws IllegalArgumentException if the sequenceID is null
	 */
	public SequenceTrace(Long sequenceID) {
		if(sequenceID==null)
			throw new IllegalArgumentException();
		
		this.sequenceID = sequenceID;
	}

	public synchronized boolean isHandled() {
		if(Boolean.TRUE.equals(handled))
			return true;
		
		boolean result = true;
		
		long expectedPos = 0;
		ManagedMessage seqMsg = null;
		MessageTrace seqMsgTrc = null;
		for (Iterator<ManagedMessage> iter = messagesBySeqPos.values().iterator(); iter.hasNext(); expectedPos++) {
			seqMsg = (ManagedMessage) iter.next();
			seqMsgTrc = (MessageTrace) messageTraces.get(seqMsg.getID());
			if(seqMsgTrc==null || !seqMsgTrc.isHandled() || (seqMsg.getSequencePosition()==null) || (seqMsg.getSequencePosition().longValue()!=expectedPos)) {
				result=false;
				break;
			}
		}
		if(result && seqMsg!=null && seqMsgTrc!=null) {
			result = seqMsg.isSequenceEnd() && seqMsgTrc.isHandled();
			if(result) {
				complete = Boolean.TRUE;
				handled = Boolean.TRUE;
			}
		}
		
		return result;
	}
	
	/**
	 * Checks whether all messages for the sequence have been received,
	 * i.e. no holes in the sequence positions, 
	 * and the sequence-end msg is present.
	 * 
	 * @return
	 */
	public synchronized boolean isComplete() {
		if(Boolean.TRUE.equals(complete))
			return true;
		
		boolean result = true;
		
		long expectedPos = 0;
		ManagedMessage seqMsg = null;
		for (Iterator<ManagedMessage> iter = messagesBySeqPos.values().iterator(); iter.hasNext(); expectedPos++) {
			seqMsg = (ManagedMessage) iter.next();
			if((seqMsg.getSequencePosition()==null) || (seqMsg.getSequencePosition().longValue()!=expectedPos)) {
				result=false;
				break;
			}
		}
		if(result && seqMsg!=null) {
			result = seqMsg.isSequenceEnd();
			if(result)
				complete = Boolean.TRUE;
		}
		
		return result;
	}

	/**
	 * @return Returns the sequenceID.
	 */
	public synchronized Long getSequenceID() {
		return sequenceID;
	}
	/**
	 * 
	 * @param message
	 * @throws IllegalArgumentException if the message does not belong to the defined sequence
	 * @throws IllegalStateException if the sequence was already marked as handled
	 */
	public synchronized void addMessage(ManagedMessage message){
		if(message==null) {
			throw new IllegalArgumentException("Null msg not allowed");
		}
		if(!sequenceID.equals(message.getSequenceID())) {
			throw new IllegalArgumentException("Message "+message.getID()+" with seqID "+message.getSequenceID()+
					"does not belong in sequence "+sequenceID);
		}
		if(complete!=null && complete.booleanValue())
			throw new IllegalStateException("sequence "+sequenceID+" already complete");
		
		handled=Boolean.FALSE;
		
		messageTraces.put(message.getID(), new MessageTrace(message));
		messagesBySeqPos.put(message.getSequencePosition(),message);
	}
	
	/**
	 * 
	 * @param message
	 * @throws IllegalArgumentException if the message does not belong to the defined sequence
	 * @throws IllegalStateException if the sequence was already marked as handled
	 */
	public synchronized void messageHandled(ManagedMessage message) {
		if(message==null) {
			throw new IllegalArgumentException("Null msg not allowed");
		}
		if(!sequenceID.equals(message.getSequenceID())) {
			throw new IllegalArgumentException("Message "+message.getID()+" with seqID "+message.getSequenceID()+
					"does not belong in sequence "+sequenceID);
		}
		if(Boolean.TRUE.equals(handled)) {
			throw new IllegalStateException("sequence "+sequenceID+" already handled completely");
		}
		MessageTrace msgTrace = (MessageTrace) messageTraces.get(message.getID());
		if(msgTrace!=null) {
			msgTrace.setHandled();
		} else {
			// weird, receiving a handled notification
			// before the message was added...
			// try to keep rolling...
			msgTrace = new MessageTrace(message);
			msgTrace.setHandled();
			messageTraces.put(message.getID(),msgTrace);
		}
	}
	
	/**
	 * Returns all messages currently present in the sequencetrace,
	 * in the right order. 
	 * The method does not guarantee that the sequence is complete!
	 * This should be checked separately with <code>isComplete()</code>.
	 * 
	 * @return all messages currently present in the sequencetrace,
	 * in the right order
	 */
	public synchronized ManagedMessage[] getMessagesInSequence() {
		return (ManagedMessage[]) messagesBySeqPos.values().toArray(new ManagedMessage[0]);
	}
	public synchronized void clear() {
		messagesBySeqPos.clear();
		messageTraces.clear();
		handled=null;
		complete=null;
	}
}
