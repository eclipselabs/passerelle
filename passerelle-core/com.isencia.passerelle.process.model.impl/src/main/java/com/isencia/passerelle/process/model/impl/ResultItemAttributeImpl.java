/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.ResultItem;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_RESULTITEMATTRIBUTE")
public class ResultItemAttributeImpl extends AttributeImpl implements Comparable<ResultItemAttributeImpl> {
	private static final long serialVersionUID = 1L;

	public static final String SCOPE_RESULT_ITEM = "resultitem";

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_resultitemattribute")
	private Long id;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter
	// methods
	@ManyToOne
	@JoinColumn(name = "RESULTITEM_ID")
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

	public ResultItemImpl<?> getResultItem() {
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
