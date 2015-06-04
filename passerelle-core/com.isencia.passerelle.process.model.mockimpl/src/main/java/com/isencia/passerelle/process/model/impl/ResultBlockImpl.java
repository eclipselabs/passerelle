/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
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
import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 * 
 */
public class ResultBlockImpl implements ResultBlock {

  private static final long serialVersionUID = 1L;

  private Long id;
  private TaskImpl task;
  private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private String colour;
  private Date creationTS;
  private String type;
  private Map<String, ResultItem<?>> resultItems = new ConcurrentHashMap<String, ResultItem<?>>();

  public ResultBlockImpl() {
  }

  public ResultBlockImpl(Task task, String type) {
    this(task, type, new Date());
  }

  public ResultBlockImpl(Task task, String type, Date creationTS) {
    this.creationTS = creationTS;
    this.task = (TaskImpl) task;
    this.type = type;

    this.task.addResultBlock(this);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
    return attributes.put(attribute.getName(), attribute);
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

  public Date getCreationTS() {
    return creationTS;
  }

  public String getType() {
    return type;
  }

  public ResultItem<?> putItem(ResultItem<?> item) {
    return resultItems.put(item.getName(), item);
  }

  public Collection<ResultItem<?>> getAllItems() {
    return Collections.unmodifiableCollection(getResultItemMap().values());
  }

  public Collection<ResultItem<?>> getMatchingItems(Matcher<ResultItem<?>> matcher) {
    Collection<ResultItem<?>> results = new HashSet<ResultItem<?>>();
    for (ResultItem<?> item : getResultItemMap().values()) {
      if (matcher.matches(item))
        results.add(item);
    }
    return results;
  }

  public Set<ResultItem> getResultItems() {
    return new HashSet<ResultItem>(getResultItemMap().values());
  }

  public ResultItem<?> getItemForName(String name) {
    return getResultItemMap().get(name);
  }

  public Task getTask() {
    return task;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Resultblock [id=");
    builder.append(id);
    if (type != null) {
      builder.append(", type=");
      builder.append(type);
    }
    if (colour != null) {
      builder.append(", colour=");
      builder.append(getColour());
    }
    builder.append("]");
    return builder.toString();
  }

  private String discriminator;

  public int hashCode() {
    return new HashCodeBuilder(31, 71).append(id).append(type).toHashCode();
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof ResultBlockImpl)) {
      return false;
    }
    ResultBlockImpl rhs = (ResultBlockImpl) arg0;
    return new EqualsBuilder().append(this.id, rhs.id).append(this.type, rhs.type).isEquals();
  }

  /**
   * Allows subclasses to give another view on the actual result items, e.g. to add generated result items.
   * 
   * @return
   */
  protected Map<String, ResultItem<?>> getResultItemMap() {
    return resultItems;
  }

  @Override
  public ResultBlockImpl clone() throws CloneNotSupportedException {
    ResultBlockImpl clone = (ResultBlockImpl) super.clone();
    clone.attributes = new HashMap<String, Attribute>(attributes.size());
    for (Entry<String, Attribute> entry : attributes.entrySet())
      clone.attributes.put(entry.getKey(), (Attribute) entry.getValue().clone());
    clone.resultItems = new HashMap<String, ResultItem<?>>(resultItems.size());
    for (Entry<String, ResultItem<?>> entry : resultItems.entrySet())
      clone.resultItems.put(entry.getKey(), (ResultItem<?>) entry.getValue().clone());

    return (clone);
  }

}
