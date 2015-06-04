/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 *
 */
public class ResultBlockAttributeImpl extends AttributeImpl implements Comparable<ResultBlockAttributeImpl> {

	public static final String SCOPE_RESULTBLOCK = "resultblock";

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private ResultBlockImpl resultBlock;
	
	public ResultBlockAttributeImpl() {
	}
	
	public ResultBlockAttributeImpl(ResultBlock resultBlock, String name, String value) {
		super(name, value);
		this.resultBlock = (ResultBlockImpl)resultBlock;
		
		this.resultBlock.putAttribute(this);
	}
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	public ResultBlock getResultBlock() {
		return resultBlock;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResultBlockAttributeImpl rhs) {
		return new CompareToBuilder()
			.append(id, rhs.id)
			.append(version, rhs.version).toComparison();
	}

	public String getScope() {
		return SCOPE_RESULTBLOCK;
	}
	
	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(id).append(getName()).append(getValueAsString()).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ResultBlockAttributeImpl)) {
			return false;
		}
		ResultBlockAttributeImpl rhs = (ResultBlockAttributeImpl) arg0;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.getName(), rhs.getName())
				.append(this.getValueAsString(), rhs.getValueAsString()).isEquals();
	}
	
}
