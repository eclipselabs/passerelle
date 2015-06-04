package com.isencia.passerelle.process.model.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;

public class RequestImpl implements Request {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Map<String, Attribute> requestAttributes = new ConcurrentHashMap<String, Attribute>();
	private CaseImpl requestCase;
	private String correlationId;
	private String type;
	private Context processingContext;
	private String initiator;
	private String executor;
	private String dataTypes;

	public RequestImpl() {
	}

	public RequestImpl(String initiator, String type) {
		this.initiator = initiator;
		this.processingContext = new ContextImpl(this);
		this.type = type;
	}

	public RequestImpl(String initiator, String type, String correlationId) {
		this(initiator, type);
		this.correlationId = correlationId;
	}

	public RequestImpl(Case requestCase, String initiator, String type) {
		this(initiator, type);
		this.requestCase = (CaseImpl) requestCase;

		this.requestCase.addRequest(this);
	}

	public RequestImpl(Case requestCase, String initiator, String type, String correlationId) {
		this(requestCase, initiator, type);
		this.correlationId = correlationId;
	}

	public RequestImpl(Case requestCase, String initiator, String type, String correlationId, String category) {
		this(requestCase, initiator, type);
		this.correlationId = correlationId;
		this.dataTypes = category;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
    this.id = id;
  }

	public String getInitiator() {
		return initiator;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(String dataTypes) {
		this.dataTypes = dataTypes;
	}

	public Attribute getAttribute(String name) {
		return requestAttributes.get(name);
	}

  public String getAttributeValue(String name) {
    Attribute attribute = getAttribute(name);
    if (attribute == null) {
      return null;
    }
    return attribute.getValueAsString();
  }

	public Attribute putAttribute(Attribute attribute) {
		return requestAttributes.put(attribute.getName(), attribute);
	}

	public Iterator<String> getAttributeNames() {
		return requestAttributes.keySet().iterator();
	}
	public Set<Attribute> getAttributes() {
		return new HashSet<Attribute>(requestAttributes.values());
	}

	public Case getCase() {
		return requestCase;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getType() {
		return type;
	}

	public Context getProcessingContext() {
		return processingContext;
	}

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RequestImpl [id=" + id + ", correlationId=" + correlationId
        + ", type=" + type + ", initiator=" + initiator + ", executor=" + executor + ", category=" + dataTypes 
        + ", \n requestAttributes=" + requestAttributes + ", \n requestCase=" + requestCase + "]";
  }

  @Override
  public Date getCreationTS() {
    return getProcessingContext().getCreationTS();
  }
	
	
}
