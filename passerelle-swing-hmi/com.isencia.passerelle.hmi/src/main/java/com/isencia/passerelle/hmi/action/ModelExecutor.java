/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.impl.SuspendResumeExecutionControlStrategy;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.State;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public class ModelExecutor extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(ModelExecutor.class);

  public ModelExecutor(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_EXECUTE), new ImageIcon(HMIBase.class.getResource("resources/run.gif")));
  }

  protected ModelExecutor(final HMIBase hmi, final String name, final Icon icon) {
    super(hmi, name, icon);
  }

  public void actionPerformed(final ActionEvent e) {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("actionPerformed() - entry"); //$NON-NLS-1$
    }
    getHMI().launchModel(this);
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("actionPerformed() - exit"); //$NON-NLS-1$
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  /**
   * @param director
   * @param name
   * @return
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ExecutionControlStrategy createExecutionControlStrategy(final Director director, final String name) throws IllegalActionException,
      NameDuplicationException {
    return new SuspendResumeExecutionControlStrategy(director, name);
  }

  public State getSuccessState() {
    return StateMachine.MODEL_EXECUTING;
  }

  public State getErrorState() {
    return StateMachine.MODEL_OPEN;
  }

}