/*
 * (c) Copyright 2001-2005, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.actor.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * ProcessRequest is a generic container for request data delivered to an actor.
 * It contains (inputport,message) pairs.
 *
 * @author erwin.de.ley@isencia.be
 */
public class ProcessRequest {

	private long iterationCount=0;
	private Map<String, MessageInputContext> inputContexts = new HashMap<String, MessageInputContext>();

	/**
	 *
	 */
	public ProcessRequest() {
		super();
	}

	/**
	 * @return Returns the iterationCount.
	 */
	public long getIterationCount() {
		return iterationCount;
	}

	/**
	 * @param iterationCount The iterationCount to set.
	 */
	public void setIterationCount(long iterationCount) {
		this.iterationCount = iterationCount;
	}

	public void addInputMessage(int inputIndex, String inputName, ManagedMessage inputMsg) {
		inputContexts.put(inputName, new MessageInputContext(inputIndex,inputName,inputMsg));
	}

	public void addInputContext(MessageInputContext msgCtxt) {
		if(msgCtxt!=null)
			inputContexts.put(msgCtxt.getPortName(), msgCtxt);
	}

	public ManagedMessage getMessage(Port inputPort) {
		if(inputPort!=null)
			return getMessage(inputPort.getName());
		else
			return null;
	}

	public ManagedMessage getMessage(String inputName) {
		if(inputName!=null) {
			MessageInputContext ctxt = inputContexts.get(inputName);
			return ctxt!=null?ctxt.getMsg():null;
		} else
			return null;
	}
	
	/**
	 * 
	 * @since Passerelle v4.1.1
	 * 
	 * @return all received input contexts
	 */
	public Iterator<MessageInputContext> getAllInputContexts() {
		return inputContexts.values().iterator();
	}
	
	/**
	 * 
	 * @return an indication whether this request contains at least one MessageInputContext
	 */
	public boolean isEmpty() {
		return inputContexts.isEmpty();
	}
	
	/**
	 * 
	 * @return an indication whether this request contains unprocessed MessageInputContexts
	 */
	public boolean hasSomethingToProcess() {
		boolean result = false;
		Collection<MessageInputContext> inpContexts = inputContexts.values();
		for (MessageInputContext messageInputContext : inpContexts) {
			if(result=!messageInputContext.isProcessed())
				break;
		}
		return result;
	}

	public String toString() {
		StringBuffer bfr = new StringBuffer();
		Collection<MessageInputContext> c = inputContexts.values();
		MessageInputContext[] inputs = c.toArray(new MessageInputContext[inputContexts.size()]);
		bfr.append("\n\tInput msgs:");
		for (int i = 0; i < inputs.length; i++) {
			MessageInputContext context = inputs[i];
			if(context!=null) {
				bfr.append("\n\t\t"+context.getPortName()+
						": msgID="+((context.getMsg()!=null && context.getMsg().getID()!=null )?context.getMsg().getID().toString():"null"));
			}
		}
		return bfr.toString();
	}

}
