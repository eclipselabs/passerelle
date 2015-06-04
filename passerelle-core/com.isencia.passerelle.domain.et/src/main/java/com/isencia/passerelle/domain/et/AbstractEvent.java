/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.runtime.SimpleEvent;

public abstract class AbstractEvent extends SimpleEvent {
  
  private static final long serialVersionUID = 6094980381771564552L;

  protected AbstractEvent(NamedObj subject, String topic, Date creationTS) {
    super(subject, topic, creationTS, 0L);
    setProperty(SUBJECT, subject.getFullName());
  }

  /**
   * 
   * @return a new Event with copied info, but new timestamp
   */
  public abstract Event copy();

  /**
   * 
   * @param dateFormat
   * @return a toString representation of the event, 
   * where dates are formatted with the given dateFormat.
   */
  public abstract String toString(DateFormat dateFormat);

  @Override
  public String toString() {
    return toString(new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS"));
  }
}
