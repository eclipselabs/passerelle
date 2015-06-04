/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import javax.swing.Icon;
import org.slf4j.Logger;
import com.isencia.passerelle.hmi.HMIBase;

/**
 * AbstractAction base class for our HMI actions
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public abstract class AbstractAction extends javax.swing.AbstractAction {

  private final HMIBase hmi;

  /**
   * @param name
   * @param icon
   */
  public AbstractAction(final HMIBase hmi, final String name, final Icon icon) {
    super(name, icon);
    this.hmi = hmi;
  }

  protected HMIBase getHMI() {
    return hmi;
  }

  protected abstract Logger getLogger();

}
