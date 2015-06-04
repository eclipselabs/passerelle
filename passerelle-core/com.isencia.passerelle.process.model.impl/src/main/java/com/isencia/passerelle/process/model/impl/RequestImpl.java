package com.isencia.passerelle.process.model.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;

@Cacheable(false)
@Entity
@Table(name = "PAS_REQUEST")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("REQUEST")
public class RequestImpl implements Request {

  public void setId(Long id) {
    this.id = id;
  }

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ID", nullable = false, unique = true, updatable = false)
  @GeneratedValue(generator = "pas_request")
  private Long id;

  @Version
  private Integer version;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
  private Date creationTS;

  @OneToMany(targetEntity = RequestAttributeImpl.class, mappedBy = "request", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @MapKey(name = "name")
  private Map<String, Attribute> attributes = ProcessUtils.emptyMap();

  // Remark: need to use the implementation class instead of the interface
  // here to ensure jpa implementations like EclipseLink will generate setter
  // methods
  // Remark: Cannot use optional = false here since we made TaskImpl extend
  // from RequestImpl
  @ManyToOne
  @JoinColumn(name = "CASE_ID")
  private CaseImpl requestCase;

  @Column(name = "CORRELATION_ID", nullable = true, unique = false, updatable = true, length = 250)
  private String correlationId;

  @Column(name = "TYPE", nullable = false, unique = false, updatable = false, length = 250)
  private String type;

  @OneToOne(targetEntity = ContextImpl.class, mappedBy = "request", cascade = CascadeType.ALL)
  private ContextImpl processingContext;

  @Column(name = "INITIATOR", nullable = false, unique = false, updatable = false, length = 250)
  private String initiator;

  @Column(name = "EXECUTOR", nullable = true, unique = false, updatable = true, length = 250)
  private String executor;

  @Column(name = "CATEGORY", nullable = true, unique = false, updatable = true, length = 250)
  private String dataTypes;

  @Column(name = "DTYPE", updatable = false)
  private String discriminator;

  public static final String _DISCRIMINATOR = "discriminator";
  public static final String _INITIATOR = "initiator";
  public static final String _EXECUTOR = "executor";
  public static final String _ID = "id";
  public static final String _ATTRIBUTES = "attributes";
  public static final String _CASE = "requestCase";
  public static final String _CORRELATION_ID = "correlationId";
  public static final String _TYPE = "type";
  public static final String _DATATYPES = "dataTypes";
  public static final String _CONTEXT = "processingContext";
  public static final String _REFERENCE = "requestCase.id";
  public static final String _EXTERNAL_REFERENCE = "requestCase.externalReference";
  public static final String _TASKS = "processingContext.tasks";
  public static final String _EVENTS = "processingContext.events";
  public static final String _CREATION_TS = "creationTS";
  public static final String _END_TS = "processingContext.endTS";
  public static final String _STATUS = "processingContext.status";
  public static final String _DURATION = "processingContext.durationInMillis";

  public RequestImpl() {
  }

  public RequestImpl(String initiator, String type) {
    this.creationTS = new Date();
    if (initiator == null) {
      this.initiator = "unknown";
    } else {
      this.initiator = initiator;
    }
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

  public RequestImpl(Case requestCase, String initiator, String type, String correlationId, String dataTypes) {
    this(requestCase, initiator, type);
    this.correlationId = correlationId;
    this.dataTypes = dataTypes;
  }

  public RequestImpl(Case requestCase, String initiator, String executor, String type, String correlationId, String dataTypes) {
    this(requestCase, initiator, type, correlationId, dataTypes);
    this.correlationId = correlationId;
    this.dataTypes = dataTypes;
    this.executor = executor;
  }

  public Long getId() {
    return id;
  }

  public Date getCreationTS() {
    return creationTS;
  }

  public String getInitiator() {
    return initiator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Request#getExecutor()
   */
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
    // Remark: can't use a CaseInsensitiveMap here because of lazy loading
    for (String attrName : attributes.keySet()) {
      if (attrName.equalsIgnoreCase(name)) {
        return attributes.get(attrName);
      }
    }
    return null;
  }

  public String getAttributeValue(String name) {
    Attribute attribute = getAttribute(name);
    if (attribute == null) {
      return null;
    }
    return attribute.getValueAsString();
  }

  public Attribute putAttribute(Attribute attribute) {
	  if (!ProcessUtils.isInitialized(attributes))
		  attributes = new HashMap<String,Attribute>();
    return attributes.put(attribute.getName(), attribute);
  }

  public Iterator<String> getAttributeNames() {
    return attributes.keySet().iterator();
  }

  public Set<Attribute> getAttributes() {
	if (!ProcessUtils.isInitialized(attributes)) {
		return ProcessUtils.emptySet();
	}
		
    return new HashSet<Attribute>(attributes.values());
  }

  public CaseImpl getCase() {
    return requestCase;
  }
  
  public void setCase(CaseImpl caze) {
    this.requestCase = caze;
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

  public ContextImpl getProcessingContext() {
    return processingContext;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[id=");
    builder.append(id);
    if (type != null) {
      builder.append(", type=");
      builder.append(type);
    }
    builder.append("]");
    return builder.toString();
  }
}
