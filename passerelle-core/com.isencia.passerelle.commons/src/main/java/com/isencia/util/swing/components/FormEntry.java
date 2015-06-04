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

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * @author erwin
 */
public class FormEntry extends Box {

  private static final long serialVersionUID = 8917245049504603775L;

  JLabel label;
  Component field;

  public FormEntry(JLabel label, Component field) {
    super(BoxLayout.X_AXIS);
    this.label = label;
    this.field = field;
    add(createGlue());
    add(this.label);
    add(createHorizontalStrut(10));
    add(this.field);
    add(createGlue());
  }

  public Component getField() {
    return field;
  }

  public JLabel getLabel() {
    return label;
  }
}
