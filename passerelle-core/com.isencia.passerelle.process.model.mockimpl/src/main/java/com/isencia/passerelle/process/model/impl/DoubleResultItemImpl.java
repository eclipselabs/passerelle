/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 *
 */
public class DoubleResultItemImpl extends ResultItemImpl<Double> {

	private static final long serialVersionUID = 1L;

	public DoubleResultItemImpl() {
	}
	
	public DoubleResultItemImpl(ResultBlock resultBlock, String name, String unit, Double value) {
		super(resultBlock, name, unit);
		this.value = Double.toString(value);
	}
	
	public DoubleResultItemImpl(ResultBlock resultBlock, String name, Double value) {
		this(resultBlock, name, null, value);
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.ResultItem#getDataType()
	 */
	public String getDataType() {
		return DataTypes.DOUBLE;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.NamedValue#getValue()
	 */
	public Double getValue() {
		return Double.parseDouble(value);
	}

}
