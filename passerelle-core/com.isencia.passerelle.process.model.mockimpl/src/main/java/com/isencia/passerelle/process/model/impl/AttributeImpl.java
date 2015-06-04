/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import com.isencia.passerelle.process.model.Attribute;

/**
 * @author "puidir"
 *
 */

public abstract class AttributeImpl implements Attribute {

	private static final long serialVersionUID = 1L;
	private static final int MAX_CHAR_SIZE = 4000;

	protected int version;
	
	private String name;
	
	private String value;
	
	private ClobItem clobItem;

	protected AttributeImpl() {
	}
	
	protected AttributeImpl(String name, String value) {
		this.name = name;
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.value = value;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		return getValueAsString();
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValueAsString()
	 */
	public String getValueAsString() {
		if (clobItem != null) {
			return clobItem.getValue();
		}

		return value;
	}
	
	 @Override
	  public AttributeImpl clone() throws CloneNotSupportedException {
	    AttributeImpl clone = (AttributeImpl)super.clone();

	    return(clone);
	  }
	
}
