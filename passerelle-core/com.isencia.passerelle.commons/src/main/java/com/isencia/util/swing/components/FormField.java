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

import java.awt.Dimension;
import java.util.StringTokenizer;
import javax.swing.JTextField;

/**
 * a JTextField with some default settings
 * <ul>
 * <li>16 columns
 * <li>read-only
 * <ul>
 * 
 * @author erwin
 */
public class FormField extends JTextField {

  private static final long serialVersionUID = -4038890446795681431L;
  private boolean multiLine;

  public FormField() {
    this(0, false);
  }

  public FormField(int columns, boolean multiLine) {
    super(columns);
    setEditable(true);
    setPreferredSize(new Dimension(100, 20));
    this.multiLine = multiLine;
  }

  public void setText(String t) {

    if (multiLine) {
      StringTokenizer tokenizer = new StringTokenizer(t);
      StringBuffer buffer = new StringBuffer();
      int charCount = 0;
      int rows = 1;
      while (tokenizer.hasMoreTokens()) {
        String next = tokenizer.nextToken();
        charCount += next.length();
        if (charCount > 20) {
          buffer.append("\n");
          charCount = next.length();
          rows++;
        }
        buffer.append(next + " ");
      }

      setPreferredSize(new Dimension(100, 20 * rows));
      super.setText(buffer.toString());
    } else {
      super.setText(t);
    }

    setCaretPosition(0);
  }
}
