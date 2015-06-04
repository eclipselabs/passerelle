/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.state;

import java.awt.Component;

class ActionComponentBinding extends ActionEnabler {
  Component component;

  public ActionComponentBinding(String action, Component component) {
    super(action);
    this.component = component;

  }

  void setEnabled(boolean enable) {
    component.setEnabled(enable);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[ActionComponentBinding:");
    buffer.append(" action: ");
    buffer.append(getActionName());
    buffer.append(" component: ");
    buffer.append(component);
    buffer.append("]");
    return buffer.toString();
  }

  /**
   * Override hashCode.
   * 
   * @return the Objects hashcode.
   */
  public int hashCode() {
    int hashCode = super.hashCode();
    hashCode = 31 * hashCode + (component == null ? 0 : component.hashCode());
    return hashCode;
  }

  /**
   * Returns <code>true</code> if this <code>ActionComponentBinding</code> is
   * the same as the o argument.
   * 
   * @return <code>true</code> if this <code>ActionComponentBinding</code> is
   *         the same as the o argument.
   */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!super.equals(o)) {
      return false;
    }
    if (o == null) {
      return false;
    }
    if (o.getClass() != getClass()) {
      return false;
    }
    ActionComponentBinding castedObj = (ActionComponentBinding) o;
    return ((this.component == null ? castedObj.component == null : this.component.equals(castedObj.component)));
  }

}