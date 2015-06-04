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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;

/**
 * @author "puidir"
 * 
 */
public abstract class ResultItemImpl<V extends Serializable> implements ResultItem<V> {

	public String getScope() {
		return getType();
	}

	public String getType() {
		if (getResultBlock() == null) {
			return null;
		}
		return getResultBlock().getType();
	}

	private static final long serialVersionUID = 1L;

	private Long id;

	private Date creationTS;

	private String name;
	protected String value;
	private String unit;
	private Map<String, AttributeImpl> attributes = new ConcurrentHashMap<String, AttributeImpl>();
	private ResultBlockImpl resultBlock;
	private String colour;
	private Integer level;

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

	public Long getId() {
		return id;
	}

	public Date getCreationTS() {
		return creationTS != null ? creationTS : (resultBlock != null ? resultBlock.getCreationTS() : null);
	}

	public String getName() {
		return name;
	}

	public String getValueAsString() {
		return value;
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
		return attributes.put(attribute.getName(), (AttributeImpl) attribute);
	}

	public Iterator<String> getAttributeNames() {
		return attributes.keySet().iterator();
	}
	public Set<Attribute> getAttributes() {
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

	public ResultBlock getResultBlock() {
		return resultBlock;
	}

	public Integer getLevel() {
		return level;
	}
	

	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(id).append(name).append(unit)
				.append(value).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof ResultItemImpl)) {
			return false;
		}
		ResultItemImpl rhs = (ResultItemImpl) arg0;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.name,
				rhs.name).append(this.unit, rhs.unit).append(this.value,
				rhs.value).isEquals();
	}

  @Override
  public String toString() {
    return "ResultItemImpl [id=" + id + ", name=" + name + ", value=" + value + ", unit=" + unit + ", colour=" + colour + "]";
  }
  @Override
  public ResultItemImpl<V> clone() throws CloneNotSupportedException {
  ResultItemImpl<V> clone = (ResultItemImpl<V>)super.clone();
  
  // clone attributes
    clone.attributes = new HashMap<String,AttributeImpl>(attributes.size());
    for (Entry<String,AttributeImpl> entry : attributes.entrySet())
      clone.attributes.put(entry.getKey(),entry.getValue().clone());  
  return(clone);
  }
}
