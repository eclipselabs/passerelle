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
import java.net.URL;

public class Model implements Serializable {

  private URL momlPath;

  private FieldMapping fieldMapping;

  public Model(URL path, FieldMapping fMapping) {
    super();
    momlPath = path;
    fieldMapping = fMapping;
  }

  public FieldMapping getFieldMapping() {
    return fieldMapping;
  }

  public void setFieldMapping(FieldMapping fieldMapping) {
    this.fieldMapping = fieldMapping;
  }

  public URL getMomlPath() {
    return momlPath;
  }

  public void setMomlPath(URL momlPath) {
    this.momlPath = momlPath;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[Model:");
    buffer.append(" momlPath: ");
    buffer.append(momlPath);
    buffer.append(" fieldMapping: ");
    buffer.append(fieldMapping);
    buffer.append("]");
    return buffer.toString();
  }
}
