/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 * 
 */
public class RequestAttributeImpl extends AttributeImpl implements Comparable<RequestAttributeImpl> {

	public static final String REQUEST = "request";

	private static final long serialVersionUID = 1L;

	private Long id;

	private RequestImpl request;

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
	public Request getRequest() {
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
