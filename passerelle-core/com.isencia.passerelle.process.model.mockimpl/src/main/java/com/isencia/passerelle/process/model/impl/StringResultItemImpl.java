/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Date;

import com.isencia.passerelle.process.model.Mutable;
import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 * 
 */
public class StringResultItemImpl extends ResultItemImpl<String> implements Mutable {

	private static final long serialVersionUID = 1L;
	private static final int MAX_CHAR_SIZE = 4000;

	private ClobItem clobItem;

	public StringResultItemImpl() {
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, String unit, Date creationTS) {
		this(resultBlock, name, value, unit, creationTS, null);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, String unit, Date creationTS,
			Integer level) {
		super(resultBlock, name, unit, creationTS, level);
		setValue(value);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value, Date creationTS) {
		this(resultBlock, name, value, null, creationTS, null);
	}

	public StringResultItemImpl(ResultBlock resultBlock, String name, String value) {
		this(resultBlock, name, value, new Date());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public String getValue() {
		if (clobItem != null) {
			return clobItem.getValue();
		}

		return value;
	}

	public void setValue(String value) {
		if (value != null && value.length() > MAX_CHAR_SIZE) {
			this.clobItem = new ClobItem(value);
		} else {
			this.value = value;
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
