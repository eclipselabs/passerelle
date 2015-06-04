/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import com.isencia.passerelle.process.model.Identifiable;

/**
 * @author "puidir"
 *
 */
public class ClobItem implements Identifiable {

	private Long id;
	
	private String value;
	
	public ClobItem() {	
	}
	
	public ClobItem(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
}
