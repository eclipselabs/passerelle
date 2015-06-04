/*
 * (c) Copyright 2005, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package com.isencia.passerelle.hmi.form;

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
