/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class represent states of a Passerelle HMI application. A
 * state is associated with certain actions being possible or not, e.g. some
 * Menu items may be enabled or not etc.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class State {

  private String name;
  private Set allowedActions;
  private static Map states = new HashMap();

  /**
   * @param name
   */
  State(String name) {
    this(name, null);
  }

  /**
   * creates a new state with the given name and the allowed actions. the set of
   * allowed actions is copied, so the state is protected against any chengs in
   * the set that is given here.
   * 
   * @param name
   * @param allowedActions
   */
  State(String name, Set allowedActions) {
    this.name = name;
    if (allowedActions != null)
      this.allowedActions = new HashSet(allowedActions);
    else
      this.allowedActions = new HashSet();
    states.put(name, this);
  }

  void addAllowedAction(String action) {
    allowedActions.add(action);
  }

  void removeAllowedAction(String action) {
    allowedActions.remove(action);
  }

  public String getName() {
    return name;
  }

  public boolean isAllowed(String action) {
    return allowedActions.contains(action);
  }

  public static State getState(String name) {
    return (State) states.get(name);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[State:");
    buffer.append(" name: ");
    buffer.append(name);
    buffer.append(" allowedActions: ");
    buffer.append(allowedActions);
    buffer.append("]");
    return buffer.toString();
  }

}
