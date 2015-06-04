/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public class ModelPrinter extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelPrinter.class);

  public ModelPrinter(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_PRINT), new ImageIcon(HMIBase.class.getResource("resources/print_edit.gif")));
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_CLOSE, this);
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));

    // Set an accelerator key; this value is used by menu items
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
  }

  public void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("actionPerformed() - entry"); //$NON-NLS-1$
    }

    getHMI().print();

    if (getLogger().isTraceEnabled()) {
      getLogger().trace("actionPerformed() - exit"); //$NON-NLS-1$
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

}