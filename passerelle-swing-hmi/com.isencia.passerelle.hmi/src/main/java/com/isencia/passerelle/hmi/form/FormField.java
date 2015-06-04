/*
 * (c) Copyright 2005, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package com.isencia.passerelle.hmi.form;

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
