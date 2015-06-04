/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.isencia.passerelle.process.model.Mutable;
import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@DiscriminatorValue("STRING_RESULT")
public class StringResultItemImpl extends ResultItemImpl<String> implements Mutable , Comparable<StringResultItemImpl> {

	private static final long serialVersionUID = 1L;

	public StringResultItemImpl() {
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, String unit, Date creationTS) {
		this(resultBlock, name, value, unit, creationTS, null);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, String unit, Date creationTS,
			Integer level) {
		super(resultBlock, name, unit, creationTS == null ? new Date() : creationTS, level);
		setValue(value);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, Date creationTS) {
		this(resultBlock, name, value, null, creationTS, null);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value) {
		this(resultBlock, name, value, new Date());
	}
	
	public int compareTo(StringResultItemImpl other) {
	    return this.getValueAsString().compareTo(other.getValueAsString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		if (clobItem != null && clobItem.getValue() != null) {
			return clobItem.getValue();
		}

		return valueAsString;
	}

	public String getValueAsString() {
		return getValue();
	}

	public void setValue(String value) {
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.valueAsString = value;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return DataTypes.STRING;
	}

}
