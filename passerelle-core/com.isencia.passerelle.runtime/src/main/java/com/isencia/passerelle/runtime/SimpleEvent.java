package com.isencia.passerelle.runtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleEvent extends EventObject implements Event, Comparable<Event> {
	private static final long serialVersionUID = 6899799531537110832L;
	
  private static volatile AtomicLong idCounter = new AtomicLong(0);

  private long id;
	private String topic;
	private Date creationTS;
	private Long duration;
  
  private Map<String, String> eventProperties =  new HashMap<String, String>();

  /**
   * Sets current datetime as creationTS, duration at 0 and
   * topic as its EventObject.subject.
   * 
   * @param topic
   */
  public SimpleEvent(String topic) {
    this(topic, new Date(), 0L);
  }

  /**
   * Sets current datetime as creationTS and
   * topic as its EventObject.subject.
   * 
   * @param topic
   * @param duration
   */
  public SimpleEvent(String topic, Long duration) {
		this(topic, new Date(), duration);
	}

  /**
   * Sets topic as its EventObject.subject.
   * 
   * @param topic
   * @param creationTS
   * @param duration
   */
  public SimpleEvent(String topic, Date creationTS, Long duration) {
    this(topic, topic, creationTS, duration);
	}
  
  /**
   * 
   * @param subject cfr EventObject.subject
   * @param topic
   * @param creationTS
   * @param duration
   */
  public SimpleEvent(Object subject, String topic, Date creationTS, Long duration) {
    super(subject);
    this.id = idCounter.incrementAndGet();
    this.topic = topic;
    this.creationTS = creationTS;
    this.duration = duration;
  }
  
  public Long getId() {
    return id;
  }

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public Date getCreationTS() {
		return creationTS;
	}

	@Override
	public Long getDuration() {
		return duration;
	}
	
	public String setProperty(String propName, String propValue) {
	  return eventProperties.put(propName, propValue);
	}
	
  public String getProperty(String propName) {
    return eventProperties.get(propName);
  }
  
  public Iterator<String> getPropertyNames() {
    return eventProperties.keySet().iterator();
  }

	public Event createDerivedEvent(String namePrefix) {
	  return new SimpleEvent(namePrefix + "//" + getTopic(), new Date(), getDuration());
  }
	
	@Override
	public int compareTo(Event o) {
	  int result = 0;
    if (creationTS == null) {
      if (o.getCreationTS() != null) {
        result = -1;
      }
    } else {
      result = creationTS.compareTo(o.getCreationTS());
    }
    if(result==0) {
      if(topic==null) {
        if(o.getTopic() != null) {
          result = -1;
        }
      } else {
        result = topic.compareTo(o.getTopic());
      }
    }
	  return result;
	}

//	 public int compareTo(Event rhs) {
//	    return new CompareToBuilder().append(creationTS, rhs.getCreationTS()).append(topic, rhs.getTopic()).toComparison();
//	  }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creationTS == null) ? 0 : creationTS.hashCode());
		result = prime * result
				+ ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEvent other = (SimpleEvent) obj;
		if (creationTS == null) {
			if (other.creationTS != null)
				return false;
		} else if (!creationTS.equals(other.creationTS))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleEventImpl [topic=" + topic + ", creationTS=" + creationTS
				+ ", duration=" + duration + "]";
	}

	protected String getFormattedCreationTS() {
    if (creationTS == null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    sdf.setLenient(true);
    return sdf.format(creationTS);
  }
}
