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
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;

@SuppressWarnings("serial")
public class ModelDebugStepper extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelDebugStepper.class);

  public ModelDebugStepper(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_DEBUG_STEP), new ImageIcon(HMIBase.class.getResource("resources/step.gif")));
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Debug Step action - entry"); //$NON-NLS-1$
    }

    try {
      ((DynamicStepExecutionControlStrategy) DirectorUtils.getAdapter(getHMI().getDirector(), null).getExecutionControlStrategy()).step();
    } catch (final Exception ex) {
      getLogger().error("Received step event, but model not configured correctly", ex);
    }

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("Model Debug Step action - exit"); //$NON-NLS-1$
    }
  }
}