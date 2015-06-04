/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;

/**
 * @author "puidir"
 *
 */

@MappedSuperclass
public abstract class AttributeImpl implements Attribute {

	private static final long serialVersionUID = 1L;
	private static final int MAX_CHAR_SIZE = 250;

	@Version
	protected Integer version;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
	private Date creationTS;

	@Column(name = "NAME", nullable = false, unique = false, updatable = false, length = 250)
	private String name;
	
	@Column(name = "VALUE", nullable = false, unique = false, updatable = false, length = MAX_CHAR_SIZE)
	private String value;
	
	@OneToOne(optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "LOB_ID", unique = true, nullable = true, updatable = false)
	private ClobItem clobItem;

  public static final String _ID = "id";
  public static final String _NAME = "name";
  public static final String _VALUE = "valueAsString";

	protected AttributeImpl() {
	}
	
	protected AttributeImpl(String name, String value) {
		this.creationTS = new Date();
		this.name = name;
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.value = value;
		}
	}
	
	@Override
	public AttributeImpl clone() throws CloneNotSupportedException {
		AttributeImpl clone = (AttributeImpl)super.clone();
		
		if (clobItem != null)
			clone.clobItem = clobItem.clone();
		
		return(clone);
	}
	
	public Date getCreationTS() {
		return creationTS;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return getValueAsString();
	}

	public String getValueAsString() {
		if (clobItem != null && clobItem.getValue() != null) {
			return clobItem.getValue();
		}

		return value;
	}
	
}
