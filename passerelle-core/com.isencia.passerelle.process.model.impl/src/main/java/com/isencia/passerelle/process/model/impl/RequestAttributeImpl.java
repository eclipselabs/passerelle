/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_REQUESTATTRIBUTE")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("RequestAttributeImpl")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RequestAttributeImpl extends AttributeImpl implements Comparable<RequestAttributeImpl> {
	private static final long serialVersionUID = 1L;

	public static final String REQUEST = "request";

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_requestattribute")
	private Long id;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter
	// methods
	@ManyToOne
	@JoinColumn(name = "REQUEST_ID")
	private RequestImpl request;

	public static final String _REQUEST = "request";

	public RequestAttributeImpl() {
	}

	public RequestAttributeImpl(Request request, String name, String value) {
		super(name, value);
		this.request = (RequestImpl) request;

		this.request.putAttribute(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.isencia.passerelle.process.model.Identifiable#getId()
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the request
	 */
	public RequestImpl getRequest() {
		return request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(RequestAttributeImpl rhs) {
		return new CompareToBuilder().append(id, rhs.id).append(version, rhs.version).toComparison();
	}

	public String getScope() {
		return REQUEST;
	}

	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(id).append(getName()).append(getValueAsString()).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof RequestAttributeImpl)) {
			return false;
		}
		RequestAttributeImpl rhs = (RequestAttributeImpl) arg0;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.getName(), rhs.getName())
				.append(this.getValueAsString(), rhs.getValueAsString()).isEquals();
	}

}
