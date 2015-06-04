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
package com.isencia.passerelle.hmi.generic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import com.isencia.passerelle.hmi.definition.ModelBundle;

/**
 * Swing list model supporting the panel where the recent models can be managed for the Generic HMI.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class RecentModelsListModel extends AbstractListModel {
  private final List<String> recentModelKeys = new ArrayList<String>();
  private final List<String> removedModelKeys = new ArrayList<String>();
  private final ModelBundle hmiDef;

  public RecentModelsListModel(final ModelBundle hmiDef) {
    this.hmiDef = hmiDef;
    recentModelKeys.addAll(hmiDef.getRecentModelsList());
  }

  public int getSize() {
    return recentModelKeys.size();
  }

  /*
   * Return what should be shown in the List view
   * @see javax.swing.ListModel#getElementAt(int)
   */
  public String getElementAt(final int index) {
    return recentModelKeys.get(index);
  }

  public void clear() {
    recentModelKeys.clear();
    fireContentsChanged(this, 0, getSize());
  }

  public boolean isEmpty() {
    return recentModelKeys.isEmpty();
  }

  public String get(final int i) {
    return recentModelKeys.get(i);
  }

  public boolean remove(final int index) {
    final String removed = recentModelKeys.remove(index);
    if (removed!=null) {
      removedModelKeys.add(removed);
      fireContentsChanged(this, 0, getSize());
      return true;
    } else {
    return false;
    }
  };

  public boolean contains(final String o) {
    return recentModelKeys.contains(o);
  }

  public List<String> getRemovedModelKeys() {
    return removedModelKeys;
  }
  
  public List<String> getRecentModelList() {
    return new ArrayList<String>(recentModelKeys);
  }

  public void swap(final int index1, final int index2) {
    final String temp = recentModelKeys.get(index1);
    recentModelKeys.set(index1, recentModelKeys.get(index2));
    recentModelKeys.set(index2, temp);
  }
}
