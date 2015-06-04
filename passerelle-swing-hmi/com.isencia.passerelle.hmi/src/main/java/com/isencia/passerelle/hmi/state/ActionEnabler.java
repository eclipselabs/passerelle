/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.state;

abstract class ActionEnabler {
  String actionName;

  /**
   * @param name
   */
  public ActionEnabler(String name) {
    actionName = name;
  }

  /**
   * @return Returns the actionName.
   */
  public synchronized String getActionName() {
    return actionName;
  }

  abstract void setEnabled(boolean enable);

  /**
   * Override hashCode.
   * 
   * @return the Objects hashcode.
   */
  public int hashCode() {
    int hashCode = 1;
    hashCode = 31 * hashCode + (actionName == null ? 0 : actionName.hashCode());
    return hashCode;
  }

  /**
   * Returns <code>true</code> if this <code>ActionEnabler</code> is the same as
   * the o argument.
   * 
   * @return <code>true</code> if this <code>ActionEnabler</code> is the same as
   *         the o argument.
   */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o.getClass() != getClass()) {
      return false;
    }
    ActionEnabler castedObj = (ActionEnabler) o;
    return ((this.actionName == null ? castedObj.actionName == null : this.actionName.equals(castedObj.actionName)));
  }

}