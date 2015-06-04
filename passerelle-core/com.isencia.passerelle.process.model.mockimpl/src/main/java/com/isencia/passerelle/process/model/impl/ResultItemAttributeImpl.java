/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.ResultItem;

/**
 * @author "puidir"
 * 
 */
public class ResultItemAttributeImpl extends AttributeImpl implements Comparable<ResultItemAttributeImpl> {

	public static final String SCOPE_RESULT_ITEM = "resultitem";

	private static final long serialVersionUID = 1L;

	private Long id;

	private ResultItemImpl<?> resultItem;

	public ResultItemAttributeImpl() {
	}

	public ResultItemAttributeImpl(ResultItem<?> resultItem, String name, String value) {
		super(name, value);
		if (resultItem instanceof ResultItemImpl<?>) {
			this.resultItem = (ResultItemImpl<?>) resultItem;
		}
		resultItem.putAttribute(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	public ResultItem<?> getResultItem() {
		return resultItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultItemAttributeImpl rhs) {
		return new CompareToBuilder().append(id, rhs.id).append(version, rhs.version).toComparison();
	}

	public String getScope() {
		return SCOPE_RESULT_ITEM;
	}
	
	
	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(id).append(getName()).append(getValueAsString()).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ResultItemAttributeImpl)) {
			return false;
		}
		ResultItemAttributeImpl rhs = (ResultItemAttributeImpl) arg0;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.getName(), rhs.getName())
				.append(this.getValueAsString(), rhs.getValueAsString()).isEquals();
	}
}
