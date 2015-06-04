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
package com.isencia.passerelle.hmi.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FieldMapping implements Serializable {

  Map fieldMappings = new HashMap();

  /**
   * Apparently XStream is able to create an object where the list is still
   * null! So we need this extra check.
   * 
   * @return
   */
  public Map getFieldMappings() {
    if (fieldMappings == null) {
      fieldMappings = new HashMap();
    }
    return fieldMappings;
  }

  public void addFieldMapping(String keyName, String valueName) {
    if (keyName != null && valueName != null) getFieldMappings().put(keyName, valueName);
  }

  public String getValueForKey(String keyName) {
    if (keyName == null) return null;
    return (String) getFieldMappings().get(keyName);
  }

  public String getKeyForValue(String valueName) {
    if (valueName == null) return null;

    String result = null;
    Iterator entryItr = getFieldMappings().entrySet().iterator();
    while (entryItr.hasNext()) {
      Entry mappingEntry = (Entry) entryItr.next();
      if (mappingEntry.getValue().equals(valueName)) {
        result = (String) mappingEntry.getKey();
        break;
      }
    }
    return result;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[FieldMapping:");
    buffer.append(" fieldMappings: ");
    buffer.append(fieldMappings);
    buffer.append("]");
    return buffer.toString();
  }

  public String getFieldMapping(String string) {
    return getValueForKey(string);
  }
}
