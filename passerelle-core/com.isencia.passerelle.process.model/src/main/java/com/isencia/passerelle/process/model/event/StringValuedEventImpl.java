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
package com.isencia.passerelle.process.model.event;

import java.util.Date;
import com.isencia.passerelle.process.model.DataTypes;
import com.isencia.passerelle.runtime.Event;

public class StringValuedEventImpl extends AbstractResultItemEventImpl<String> {

  private static final long serialVersionUID = 7401438663940242342L;
  
  public StringValuedEventImpl(String topic, String value, Date creationTS, Long duration) {
    super(topic,value,creationTS,duration);
  }

  public StringValuedEventImpl(String topic, String value) {
    this(topic, value, new Date(), 0L);
  }
  
  @Override
  public String getDataType() {
    return DataTypes.STRING;
  }
  
  @Override
  public Event createDerivedEvent(String namePrefix) {
    return new StringValuedEventImpl(namePrefix + "//" + getName()+"//" + "(" + getFormattedCreationTS() + ")", getValue());
  }
}
