/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.runtime.process.impl.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author erwin
 *
 */
public class ProcessEvent extends EventObject implements com.isencia.passerelle.runtime.process.ProcessEvent {
  private static final long serialVersionUID = 411059590003641225L;
  
  public final static String BREAKPOINTS = "___breakpoints";
  
  private String processContextId;
  private Date timeStamp;
  private String topic;
  
  private Map<String, String> eventProperties =  new HashMap<String, String>();
  private Kind kind;
  private Detail detail;
  
  public ProcessEvent(String processContextId, Kind kind, Detail detail) {
    super(processContextId);
    this.processContextId = processContextId;
    this.topic = TOPIC_PREFIX + processContextId + "/" + kind.name()+"/"+detail.name();
    this.timeStamp = new Date();
    this.kind = kind;
    this.detail = detail;
  }
  
  @Override
  public String getProcessContextId() {
    return processContextId;
  }

  public String getTopic() {
    return topic;
  }
  
  public Date getCreationTS() {
    return timeStamp;
  }

  /**
   * @return 0L as default duration
   */
  public Long getDuration() {
    return 0L;
  }
  
  public String getProperty(String propName) {
    return eventProperties.get(propName);
  }
  
  public Iterator<String> getPropertyNames() {
    return eventProperties.keySet().iterator();
  }
  
  protected String putProperty(String propName, String propValue) {
    return eventProperties.put(propName, propValue);
  }
  
  protected String removeProperty(String propName) {
    return eventProperties.remove(propName);
  }
  
  @Override
  public String toString() {
    return toString(new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS"));
  }
  
  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Detail getDetail() {
    return detail;
  }

  /**
   * 
   * @param dateFormat
   * @return a toString representation of the event, 
   * where dates are formatted with the given dateFormat.
   */
  public String toString(DateFormat dateFormat) {
    return dateFormat.format(getCreationTS()) + " ProcessEvent [topic=" + getTopic() + "]";
  }
}
