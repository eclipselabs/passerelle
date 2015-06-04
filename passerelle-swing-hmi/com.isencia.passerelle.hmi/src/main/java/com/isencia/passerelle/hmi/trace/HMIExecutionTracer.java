/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;
import com.isencia.passerelle.util.Level;

/**
 * An ExecutionTracer wrapper that allows to register itself as an attribute on
 * some Passerelle entity (typically a director).
 * 
 * @author erwin.de.ley@isencia.be
 */
public class HMIExecutionTracer implements ExecutionTracer {

  private final ExecutionTracer tracedialog;

  public HMIExecutionTracer(final ExecutionTracer dialog) {
    this.tracedialog = dialog;
  }

  public void trace(final Actor actor, final String message) {
    tracedialog.trace(actor, message);
    try {
      DynamicStepExecutionControlStrategy execCtrl = (DynamicStepExecutionControlStrategy) DirectorUtils.getAdapter(actor.getDirector(), null).getExecutionControlStrategy();

      execCtrl.stopStep();

    } catch (Exception e) {
      // ignore, just means we're certainly not using a step exec ctrl
    }
  }

  public void trace(final Director director, final String message) {
    tracedialog.trace(director, message);
  }

  public void trace(Actor source, String message, Level level) {
    trace(source, message);
  }

  public void trace(Director source, String message, Level level) {
    trace(source, message);
  }
}
