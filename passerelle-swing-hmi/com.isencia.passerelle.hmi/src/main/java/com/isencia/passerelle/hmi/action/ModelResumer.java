/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;

@SuppressWarnings("serial")
public class ModelResumer extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelDebugStepper.class);

  public ModelResumer(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_RESUME), new ImageIcon(HMIBase.class.getResource("resources/resume.gif")));
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public synchronized void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Resume action - entry"); //$NON-NLS-1$
    }

		 getHMI().resumeModel();

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Resume action - exit"); //$NON-NLS-1$
    }
  }
}