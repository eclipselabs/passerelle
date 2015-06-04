/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */

package com.isencia.passerelle.hmi.action;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;

/**
 * Default implementation of an error control strategy for an actor:
 * <ul>
 * <li>Check if the exception is FATAL or NON_FATAL
 * <li>FATALs and RuntimeExceptions are escalated as IllegalActionExceptions
 * <li>NON_FATALs are sent via the error port, if it is connected
 * <li>if the error port is not connected, the error is reported to the director
 * </ul>
 * 
 * @author erwin
 */
public class AskTheUserErrorControlStrategy extends DefaultActorErrorControlStrategy {

  private HMIBase hmiBase;

  public AskTheUserErrorControlStrategy(HMIBase hmiBase, ModelExecutor executor) {
    this.hmiBase = hmiBase;
  }

  public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
    Icon icon = new ImageIcon(getClass().getResource("/com/isencia/passerelle/hmi/resources/ide32.gif"));
    int choice = PopupUtil.showOptionDialog(hmiBase.getDialogHookComponent(), 
    	HMIMessages.getString("error.execution.error") + e.getMessage(), 
    	HMIMessages.getString("error"), 
    	JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, 
    	new String[] { "continue", "stop model", "refire actor" }, "continue");
    switch (choice) {
    case 0:
      if (e.getSeverity() != PasserelleException.Severity.FATAL) {
        a.sendErrorMessage(e);
      } else {
        getLoggerForActor(a).error(a.getInfo() + " fire() - generated exception", e);
        throw new IllegalActionException(a, e, "");
      }
      break;
    case 1:
      hmiBase.stopModel();
      break;
    case 2:
      a.fire();
      break;
    default:
      break;
    }
  }
}
