/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_RESULTITEM")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("RESULT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ResultItemImpl<V extends Serializable> implements ResultItem<V> {
  private static final long serialVersionUID = 1L;

  public static final String _ID = "id";
  public static final String _NAME = "name";
  public static final String _VALUE = "valueAsString";
  public static final String _CREATION_TS = "creationTS";
  public static final String _UNIT = "unit";
  public static final String _DATA_TYPE = "dataType";
  public static final String _RESULT_BLOCK = "resultBlock";
  public static final String _RESULT_BLOCK_TYPE = "resultBlock.type";
  public static final String _COLOUR = "colour";
  public static final String _DISCRIMINATOR = "discriminator";
  public static final String _ATTRIBUTES = "attributes";
  public static final String _CLOB_ITEM = "clobItem";
  public static final String _LEVEL = "level";

  protected static final int MAX_CHAR_SIZE = 500;

  @Id
  @Column(name = "ID", nullable = false, unique = true, updatable = false)
  @GeneratedValue(generator = "pas_resultitem")
  private Long id;

  @Version
  private Integer version;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATION_TS", nullable = true, unique = false, updatable = false)
  private Date creationTS;

  @Column(name = "NAME", nullable = false, unique = false, updatable = false, length = 300)
  private String name;

  @Column(name = "VALUE", nullable = true, unique = false, updatable = false, length = MAX_CHAR_SIZE)
  protected String valueAsString;

  @Column(name = "UNIT", nullable = true, unique = false, updatable = false, length = 20)
  private String unit;

  @OneToMany(targetEntity = ResultItemAttributeImpl.class, mappedBy = "resultItem", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @MapKey(name = "name")
  private Map<String, AttributeImpl> attributes = ProcessUtils.emptyMap();

  // Remark: need to use the implementation class instead of the interface
  // here to ensure jpa implementations like EclipseLink will generate setter
  // methods
  @ManyToOne
  @JoinColumn(name = "RESULTBLOCK_ID")
  private ResultBlockImpl resultBlock;

  @Column(name = "COLOR", nullable = true, unique = false, updatable = true, length = 20)
  private String colour;

  @Column(name = "DETAILLEVEL", nullable = true, unique = false, updatable = true)
  private Integer level;

  @Column(name = "DTYPE", updatable = false)
  private String discriminator;

  @OneToOne(optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "LOB_ID", unique = true, nullable = true, updatable = false)
  protected ClobItem clobItem;

  public ResultItemImpl() {
  }

  protected ResultItemImpl(ResultBlock resultBlock, String name, String unit) {
    this(resultBlock, name, unit, new Date(), null);
  }

  protected ResultItemImpl(ResultBlock resultBlock, String name, String unit, Integer level) {
    this(resultBlock, name, unit, new Date(), level);
  }

  protected ResultItemImpl(ResultBlock resultBlock, String name, String unit, Date creationTS, Integer level) {
    this.creationTS = creationTS;
    this.resultBlock = (ResultBlockImpl) resultBlock;
    this.name = name;
    this.unit = unit;
    this.level = level;
    // TODO when resultblock is null then the TransientResultItemImpl should be used
    if (this.resultBlock != null)
      this.resultBlock.putItem(this);
  }

  @Override
  public ResultItemImpl<V> clone() throws CloneNotSupportedException {
    ResultItemImpl<V> clone = (ResultItemImpl<V>) super.clone();
    // clone attributes
    if (ProcessUtils.isInitialized(attributes)) {
      clone.attributes = new HashMap<String, AttributeImpl>(attributes.size());
      for (Entry<String, AttributeImpl> entry : attributes.entrySet())
        clone.attributes.put(entry.getKey(), entry.getValue().clone());
    }
    if (clobItem != null)
      clone.clobItem = clobItem.clone();
    return (clone);
  }

  public Long getId() {
    return id;
  }

  public Date getCreationTS() {
    return creationTS != null ? creationTS : (resultBlock != null ? resultBlock.getCreationTS() : null);
  }

  public Date getCreationTSAsIs() {
	  return(creationTS);
  }
  
  public void setCreationTS(Date creationTS) {
	this.creationTS = creationTS;
  }
  
  public String getName() {
    return name;
  }

  public String getValueAsString() {
    return valueAsString;
  }

  public Attribute getAttribute(String name) {
    return attributes.get(name);
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
      attributes = new HashMap<String, AttributeImpl>();
    return attributes.put(attribute.getName(), (AttributeImpl) attribute);
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

  public String getColour() {
    return colour;
  }

  public void setColour(String colour) {
    this.colour = colour;
  }

  public String getUnit() {
    return unit;
  }
  
  public void setUnit(String unit) {
	this.unit = unit;
  }

  public ResultBlockImpl getResultBlock() {
    return resultBlock;
  }

  public Integer getLevel() {
    return level;
  }
  
  public void setLevel(Integer level) {
	this.level = level;
  }

  public String getScope() {
    return getType();
  }

  public String getType() {
    if (getResultBlock() == null) {
      return null;
    }
    return getResultBlock().getType();
  }

  @SuppressWarnings("all")
  public int hashCode() {
    return new HashCodeBuilder(31, 71).append(id).append(name).append(unit).append(valueAsString).toHashCode();
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof ResultItemImpl)) {
      return false;
    }
    ResultItemImpl<?> rhs = (ResultItemImpl<?>) arg0;
    return new EqualsBuilder().append(this.id, rhs.id).append(this.name, rhs.name).append(this.unit, rhs.unit).append(this.valueAsString, rhs.valueAsString).isEquals();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[name=");
    builder.append(name);
    builder.append(", value=");
    builder.append(getValueAsString());
    if (colour != null) {
      builder.append(", colour=");
      builder.append(getColour());
    }
    builder.append("]");
    return builder.toString();
  }
}
