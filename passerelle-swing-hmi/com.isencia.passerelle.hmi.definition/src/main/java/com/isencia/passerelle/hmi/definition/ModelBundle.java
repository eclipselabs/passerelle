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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import com.thoughtworks.xstream.XStream;

/**
 * This class represents a set of configuration info for a HMI tool.
 * <p>
 * For specific customized HMIs, only the models predefined in their bundle will be supported. If a user tries to open another type of model, the tool will give
 * an error pop-up.
 * </p>
 * <p>
 * For the generic HMI, this bundle just contains the recently used models.
 * </p>
 * <p>
 * Both types of entries are typically used in the HMI to populate the Files>Models menu.
 * </p>
 * 
 * @author erwin
 */
public class ModelBundle implements Serializable {
  protected final static XStream xmlStreamer = new XStream();

  /**
   * collection of predefined models, i.e. part of the static configuration of a HMI instance
   */
  private Map<String, Model> predefinedModels = new TreeMap<String, Model>();
  /**
   * collection of recently used models
   */
  private Map<String, Model> recentModels = new TreeMap<String, Model>();
  private List<String> recentModelKeysList = new ArrayList<String>();

  private int recentModelsLimit = 10;

  public int getRecentModelsLimit() {
    return recentModelsLimit;
  }

  public void setRecentModelsLimit(int recentModelsLimit) {
    this.recentModelsLimit = recentModelsLimit;
    while (getRecentModelsList() != null && getRecentModelsList().size() > recentModelsLimit) {
      getRecentModelsList().remove(recentModelsLimit);
    }

  }

  public boolean isEmpty() {
    return getRecentModels().isEmpty() && getPredefinedModels().isEmpty();
  }

  public boolean containsPredefinedModel(URI modelURI) {
    boolean result = false;
    for (Model m : getPredefinedModels().values()) {
      try {
        if (modelURI.equals(m.getMomlPath().toURI())) {
          result = true;
          break;
        }
      } catch (Exception e) {
        // ignore, should never happen
      }
    }
    return result;
  }

  public void setPredefinedModel(String modelKey, Model newModel) {
    getPredefinedModels().put(modelKey, newModel);
  }

  // Apparently XStream is able to create an object where the list is still null! 
  // So we need this extra null check in the following methods.
  public Map<String, Model> getPredefinedModels() {
    if (predefinedModels == null) {
      predefinedModels = new TreeMap<String, Model>();
    }
    return predefinedModels;
  }

  public List<String> getRecentModelsList() {
    if (recentModelKeysList == null) {
      recentModelKeysList = new ArrayList<String>(recentModelsLimit);
    }
    return recentModelKeysList;
  }

  public void setReorderedRecentModelsList(List<String> recentModelList) {
    recentModelKeysList.clear();
    for (String newModelKey : recentModelList) {
      if (recentModels.keySet().contains(newModelKey)) {
        recentModelKeysList.add(newModelKey);
      }
    }
  }

  public Map<String, Model> getRecentModels() {
    if (recentModels == null) {
      recentModels = new TreeMap<String, Model>();
    }
    return recentModels;
  }

  public void addModel(String modelKey, Model newModel) {
    Model prevOne = getRecentModels().get(modelKey);
    if (prevOne != null && prevOne != newModel) {
      // need to generate an indexed name
      int i = 2;
      while (true) {
        String newModelKey = modelKey + "(" + (i++) + ")";
        Model _m = getRecentModels().get(newModelKey);
        if (_m == null || _m.getMomlPath().equals(newModel.getMomlPath())) {
          modelKey = newModelKey;
          break;
        }
      }
    }
    getRecentModels().put(modelKey, newModel);
    getRecentModelsList().remove(modelKey);
    getRecentModelsList().add(0, modelKey);
    while (getRecentModelsList().size() > recentModelsLimit) {
      getRecentModelsList().remove(recentModelsLimit);
    }
  }

  public boolean removeModel(String modelKey) {
    boolean result = getRecentModels().remove(modelKey) != null;
    getRecentModelsList().remove(modelKey);
    return result;
  }

  public boolean removeRecentModel(URI modelURI) {
    String modelKey = getModelKey(modelURI);
    return (modelKey!=null) && removeModel(modelKey);
  }

  public String getModelKey(URI modelURI) {
    String modelKey = null;
    for(Entry<String,Model> entry : getRecentModels().entrySet()) {
      Model m = entry.getValue();
      try {
        if (modelURI.equals(m.getMomlPath().toURI())) {
          modelKey = entry.getKey();
          break;
        }
      } catch (Exception e) {
        // ignore, should never happen
      }
    }
    return modelKey;
  }

  public Model getModel(String modelKey) {
    Model m = getRecentModels().get(modelKey);
    if (m == null) {
      m = getPredefinedModels().get(modelKey);
    }
    return m;
  }

  public static ModelBundle parseModelBundleDefFile(String defPath) {
    ModelBundle result;
    Reader r = null;
    try {
      r = new FileReader(new File(defPath));
      result = (ModelBundle) xmlStreamer.fromXML(r);
    } catch (Throwable t) {
      t.printStackTrace();
      result = new ModelBundle();
    } finally {
      if (r != null)
        try {
          r.close();
        } catch (IOException e) {
        }
    }
    return result;
  }

  public static ModelBundle parseModelBundleDef(String def) {

    ModelBundle result = null;
    try {
      result = (ModelBundle) xmlStreamer.fromXML(def);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return result;
  }

  public final static String generateDef(ModelBundle mb) {
    return xmlStreamer.toXML(mb);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[ModelBundle:");
    buffer.append(" models: ");
    buffer.append(predefinedModels);
    buffer.append("]");
    return buffer.toString();
  }
}
