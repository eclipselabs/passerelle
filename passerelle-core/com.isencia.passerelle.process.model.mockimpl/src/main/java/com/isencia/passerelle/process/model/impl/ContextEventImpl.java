/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;

/**
 * @author "puidir"
 *
 */
public class ContextEventImpl implements ContextEvent {

	private static final long serialVersionUID = 1L;
	
	private Long id;

	private String topic;
	
	private String message;
	
	private Date creationTS;

	private ContextImpl context;

	public ContextEventImpl() {
	}
	
	public ContextEventImpl(Context context, String topic) {
		this.creationTS = new Date();
		this.context = (ContextImpl)context;
		this.topic = topic;

		this.context.addEvent(this);
	}
	
	public ContextEventImpl(Context context, String topic, String message) {
		this(context, topic);
		this.message = message;
	}
	
	public Long getId() {
		return id;
	}

	public String getTopic() {
		return topic;
	}

	public String getMessage() {
		return message;
	}

	public Date getCreationTS() {
		return creationTS;
	}

	public Long getDuration() {
		// Irrelevant
		return 0L;
	}

	public Context getContext() {
		return context;
	}

	public int compareTo(ContextEvent rhs) {
		ContextEventImpl rhsImpl = (ContextEventImpl)rhs;
		return new CompareToBuilder()
			.append(creationTS, rhsImpl.creationTS)
			.append(topic, rhsImpl.topic).toComparison();
	}

  public String getProperty(String propName) {
    Serializable entryValue = getContext().getEntryValue(propName);
    if(entryValue==null) {
      return null;
    } else if (entryValue instanceof String) {
      return (String) entryValue;
    } else {
      return entryValue.toString();
    }
  }

  public Iterator<String> getPropertyNames() {
    return getContext().getEntryNames();
  }
}
