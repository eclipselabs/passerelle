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

package com.isencia.util.swing.components;

import java.util.SortedSet;
import javax.swing.AbstractListModel;

public class SortedMutableListModel extends AbstractListModel implements MutableListModel {
  private SortedSet data;
  private Object[] temp;

  public SortedMutableListModel(SortedSet newData) throws NullPointerException {
    setData(newData);
  }

  /**
   * To be called when items added or removed to the <code>data</code> or when a
   * new dataset specified.
   */
  public void modelDataChanged() {
    temp = null;
    fireContentsChanged(this, 0, data.size() - 1);
  }

  public void setData(SortedSet newData) throws NullPointerException {
    if (newData == null) {
      throw new NullPointerException();
    }
    data = newData;
    modelDataChanged();
  }

  public SortedSet getData() {
    return data;
  }

  public Object getElementAt(int index) {
    if (temp == null) {
      temp = data.toArray();
    }
    return temp[index];
  }

  public int getSize() {
    return data.size();
  }

  public void addElements(Object[] el) {
    for (int i = 0; i < el.length; i++) {
      data.add(el[i]);
    }
    modelDataChanged();
  }

  public void removeElements(Object[] el) {
    for (int i = 0; i < el.length; i++) {
      data.remove(el[i]);
    }
    modelDataChanged();
  }
}
