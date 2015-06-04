/*
 * (c) Copyright 2001-2007, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.actor.v3;

import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * In the new Passerelle Actor API, the MessageOutputContext is a generic container
 * for attributes etc that are related to one specific generated output message for an actor.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class MessageOutputContext {

	private int index;
	private Port port;
	private ManagedMessage message;
	
	public MessageOutputContext(int index, Port port, ManagedMessage message) {
		super();
		this.index = index;
		this.port = port;
		this.message = message;
	}
	
	public int getIndex() {
		return index;
	}

	public Port getPort() {
		return port;
	}

	public ManagedMessage getMessage() {
		return message;
	}
}
