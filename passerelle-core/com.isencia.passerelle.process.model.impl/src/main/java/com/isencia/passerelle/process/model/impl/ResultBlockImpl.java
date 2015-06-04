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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_RESULTBLOCK")
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("RESULTBLOCK")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ResultBlockImpl implements ResultBlock {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ID", nullable = false, unique = true, updatable = false)
  @GeneratedValue(generator = "pas_resultblock")
  private Long id;

  @Version
  private Integer version;

  // Remark: need to use the implementation class instead of the interface
  // here to ensure jpa implementations like EclipseLink will generate setter
  // methods
  @ManyToOne(targetEntity = TaskImpl.class, optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "TASK_ID")
  private TaskImpl task;

  @OneToMany(targetEntity = ResultBlockAttributeImpl.class, mappedBy = "resultBlock", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @MapKey(name = "name")
  private Map<String, Attribute> attributes = ProcessUtils.emptyMap();

  @Column(name = "COLOR", nullable = true, unique = false, updatable = true, length = 20)
  private String colour;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
  private Date creationTS;

  @Column(name = "TYPE", nullable = false, unique = false, updatable = false, length = 250)
  private String type;

  @OneToMany(targetEntity = ResultItemImpl.class, mappedBy = "resultBlock", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "RESULTBLOCK_ID")
  @MapKey(name = "name")
  private Map<String, ResultItem<?>> resultItems = ProcessUtils.emptyMap();

  public static final String _ID = "id";
  public static final String _CREATION_TS = "creationTS";
  public static final String _TASK = "task";
  public static final String _TYPE = "type";
  public static final String _RESULT_ITEMS = "resultItems";
  public static final String _ALL_ITEMS = "allItems";
  public static final String _COLOUR = "colour";
  public static final String _ATTRIBUTES = "attributes";
  public static final String _DISCRIMINATOR = "discriminator";
  public static final String _VERSION = "version";

  public ResultBlockImpl() {
  }

  public ResultBlockImpl(Task task, String type, Date creationTS) {
    this.creationTS = creationTS;
    this.task = (TaskImpl) task;
    this.type = type;
    if (task != null) {
      this.task.addResultBlock(this);
    }
  }

  public ResultBlockImpl(Task task, String type) {
    this(task, type, new Date());
  }
  
  @Override
  public ResultBlockImpl clone() throws CloneNotSupportedException {
    ResultBlockImpl clone = (ResultBlockImpl)super.clone();
		
	// clone attributes
    if (ProcessUtils.isInitialized(attributes)) {
	  clone.attributes = new HashMap<String,Attribute>(attributes.size());
	  for (Entry<String,Attribute> entry : attributes.entrySet())
	    clone.attributes.put(entry.getKey(),entry.getValue().clone());
    }
		
	// clone resultItems
    if (ProcessUtils.isInitialized(resultItems)) {
	  clone.resultItems = new HashMap<String,ResultItem<?>>(resultItems.size());
	  for (Entry<String,ResultItem<?>> entry : resultItems.entrySet())
	    clone.resultItems.put(entry.getKey(),entry.getValue().clone());
    }
		
	return(clone);
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
    if (!ProcessUtils.isInitialized(attributes))
      attributes = new HashMap<String, Attribute>();
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

  public String getColour() {
    return colour;
  }

  public void setColour(String colour) {
    this.colour = colour;
  }

  public Date getCreationTS() {
    return creationTS;
  }

  public void setCreationTS(Date creationTS) {
    this.creationTS = creationTS;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ResultItem<?> putItem(ResultItem<?> item) {
    if (!ProcessUtils.isInitialized(resultItems))
      resultItems = new HashMap<String, ResultItem<?>>();
    return resultItems.put(item.getName(), item);
  }

  public Collection<ResultItem<?>> getAllItems() {
    Map<String, ResultItem<?>> map = getResultItemMap();
    if (!ProcessUtils.isInitialized(map)) {
      return ProcessUtils.EMPTY_SET;
    }

    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public Collection<ResultItem<?>> getMatchingItems(Matcher<ResultItem<?>> matcher) {
    Collection<ResultItem<?>> results = new HashSet<ResultItem<?>>();
    for (ResultItem<?> item : getResultItemMap().values()) {
      if (matcher.matches(item))
        results.add(item);
    }
    return results;
  }

  public Set<ResultItem> getResultItems() {
    Map<String, ResultItem<?>> map = getResultItemMap();
    if (!ProcessUtils.isInitialized(map)) {
      return ProcessUtils.EMPTY_SET;
    }

    return new HashSet<ResultItem>(map.values());
  }

  public ResultItem<?> getItemForName(String name) {
    return getResultItemMap().get(name);
  }

  public TaskImpl getTask() {
    return task;
  }
  
  public void setTask(TaskImpl task) {
	this.task = task;
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

  @Column(name = "DTYPE", updatable = false)
  private String discriminator;

  @SuppressWarnings("all")
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

  public void resetItems() {
    resultItems = new HashMap<String, ResultItem<?>>();
  }

  /**
   * Allows subclasses to give another view on the actual result items, e.g. to add generated result items.
   * 
   * @return
   */
  protected Map<String, ResultItem<?>> getResultItemMap() {
    return resultItems;
  }

 
}
