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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For the generic HMI, this ModelBundle specialization contains extra layout
 * customization info for some models, as previously defined by the user.
 * 
 * @author erwin
 */
public class HMIDefinition extends ModelBundle {

  public static class LayoutPreferences {
    public LayoutPreferences(List actorNames) {
      super();
      this.actorNames = actorNames;
    }

    private int nrColumns;
    private List actorNames = new ArrayList();

    public LayoutPreferences(int columns) {
      nrColumns = columns;
    }

    public List getActorNames() {
      if (actorNames == null) actorNames = new ArrayList();
      return actorNames;
    }

    public int getNrColumns() {
      return nrColumns;
    }

    public void setNrColumns(int nrColumns) {
      this.nrColumns = nrColumns;
    }

  }

  private Map modelLayoutPreferences;

  /**
   * Apparently XStream is able to create an object where the list is still
   * null! So we need this extra check.
   * 
   * @return
   */
  public Map getModelLayoutPrefs() {
    if (modelLayoutPreferences == null) {
      modelLayoutPreferences = new HashMap();
    }
    return modelLayoutPreferences;
  }

  public void addModelLayout(String modelKey, LayoutPreferences prefs) {
    getModelLayoutPrefs().put(modelKey, prefs);
  }

  public LayoutPreferences getLayoutPrefs(String modelKey) {
    return (LayoutPreferences) getModelLayoutPrefs().get(modelKey);
  }

  public static HMIDefinition parseHMIDefFile(String defPath) {
    HMIDefinition result = null;
    Reader r = null;
    try {
      r = new FileReader(defPath);
      result = (HMIDefinition) xmlStreamer.fromXML(r);
    } catch (Throwable t) {
      // just return an empty one
      result = new HMIDefinition();
    } finally {
      if (r != null) try {
        r.close();
      } catch (IOException e) {
      }
    }
    return result;
  }

  public static HMIDefinition parseHMIDef(String def) {

    HMIDefinition result = null;
    try {
      result = (HMIDefinition) xmlStreamer.fromXML(def);
    } catch (Throwable t) {
      // ignore, just return null
    }
    return result;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[HMIDefinition:");
    buffer.append(" modelLayoutPreferences: ");
    buffer.append(modelLayoutPreferences);
    buffer.append("]");
    return buffer.toString();
  }
}
