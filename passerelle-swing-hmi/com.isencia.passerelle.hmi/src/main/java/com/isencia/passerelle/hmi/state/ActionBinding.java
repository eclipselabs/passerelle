/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.state;

import javax.swing.Action;

class ActionBinding extends ActionEnabler {
  Action action;

  public ActionBinding(String actionName, Action action) {
    super(actionName);
    this.action = action;

  }

  void setEnabled(boolean enable) {
    action.setEnabled(enable);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[ActionBinding:");
    buffer.append(" actionName: ");
    buffer.append(getActionName());
    buffer.append(" action: ");
    buffer.append(action);
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
    hashCode = 31 * hashCode + (action == null ? 0 : action.hashCode());
    return hashCode;
  }

  /**
   * Returns <code>true</code> if this <code>ActionBinding</code> is the same as
   * the o argument.
   * 
   * @return <code>true</code> if this <code>ActionBinding</code> is the same as
   *         the o argument.
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
    ActionBinding castedObj = (ActionBinding) o;
    return ((this.action == null ? castedObj.action == null : this.action.equals(castedObj.action)));
  }
}