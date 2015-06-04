/*
 * (c) Copyright 2002, Tuple NV Belgium
 * All Rights Reserved.
 */
package com.isencia.passerelle.process.actor.trial;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * Dump a Context in an execution trace message
 * 
 * @author erwin
 */
public class ContextTracerConsole extends Sink {
  private static final long serialVersionUID = 1L;

  /**
   * @param container
   *          The container.
   * @param name
   *          The name of this actor.
   * @exception IllegalActionException
   *              If the entity cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the container already has an actor with this name.
   */
  public ContextTracerConsole(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }

  protected void sendMessage(ManagedMessage message) throws ProcessingException {
    if (message != null) {
      ExecutionTracerService.trace(this, message.toString());
    }
  }
}