/*
 * (c) Copyright 2005, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package com.isencia.passerelle.hmi.form;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A combination of a checkbox and another component when the checkbox is on,
 * the other component is shown when it's off the other component disappears An
 * optional subject can be registered as well, so we can easily link the check
 * status with another arbitrary object.
 * 
 * @author erwin
 */
public class CheckableComponent extends JPanel {

  private class ComponentVisualizer implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      checkedComponent.setEnabled(isChecked());
      getParent().validate();
    }
  }

  private JCheckBox checkBox;
  private Component checkedComponent;
  private Object subject;

  public CheckableComponent(Component c, Object subject, boolean isChecked) {
    super(new FormLayout("pref,10px,pref", "pref"));
    this.checkedComponent = c;
    this.checkBox = new JCheckBox();
    this.subject = subject;
    checkBox.setSelected(isChecked);
    checkBox.addActionListener(new ComponentVisualizer());

    // for some reason, if we put it as not visible here
    // the component can not be set visible later on
    // when we're checking the checkbox...
    checkedComponent.setEnabled(isChecked);
    CellConstraints cc = new CellConstraints();
    add(this.checkBox, cc.xy(1, 1));
    add(this.checkedComponent, cc.xy(3, 1));
  }

  public Component getCheckedComponent() {
    return checkedComponent;
  }

  public boolean isChecked() {
    return checkBox.isSelected();
  }

  public Object getSubject() {
    return subject;
  }

  public void setChecked(boolean checked) {
    checkBox.setSelected(checked);
    checkedComponent.setEnabled(checked);
  }
}
