/*
 * (c) Copyright 2001-2007, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.actor.v3;

import com.isencia.passerelle.message.ManagedMessage;

/**
 * In the new Passerelle Actor API, the MessageInputContext is a generic container
 * for attributes etc that are related to one specific message input for an actor.
 * 
 * 
 * @author erwin.de.ley@isencia.be
 */
public class MessageInputContext {
	private int portIndex;
	private String portName;
	private ManagedMessage msg;
	// indicates whether this input has already been processed by an actor
	private boolean processed;

	/**
	 * 
	 * @param portIndex
	 * @param portName
	 * @param msg
	 */
	public MessageInputContext(int portIndex, String portName, ManagedMessage msg) {
		super();
		this.portIndex = portIndex;
		this.portName = portName;
		this.msg = msg;
		
		// nothing to be done with empty messages!
		processed=(msg==null);
	}

	public ManagedMessage getMsg() {
		return msg;
	}

	public int getPortIndex() {
		return portIndex;
	}

	public String getPortName() {
		return portName;
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	void setProcessed(boolean processed) {
		this.processed = processed;
	}
}
