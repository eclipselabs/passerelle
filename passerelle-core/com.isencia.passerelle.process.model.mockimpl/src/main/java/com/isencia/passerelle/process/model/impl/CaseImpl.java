/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;

/**
 * @author "puidir"
 * 
 */
public class CaseImpl implements Case {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String externalReference;

	private List<Request> requests = new ArrayList<Request>();

	public CaseImpl() {
	}

	public CaseImpl(String externalReference) {
		this.externalReference = externalReference;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
    this.id = id;
  }

	public String getExternalReference() {
		return externalReference;
	}

	public Collection<Request> getRequests() {
		// This avoids concurrent modifications
		List<Request> requestCopies = new ArrayList<Request>();
		requestCopies.addAll(requests);
		return requestCopies;
	}

	public synchronized void addRequest(Request request) {
		this.requests.add(request);
	}

  @Override
  public String toString() {
    return "CaseImpl [id=" + id + ", externalReference=" + externalReference + "]";
  }
	
}
