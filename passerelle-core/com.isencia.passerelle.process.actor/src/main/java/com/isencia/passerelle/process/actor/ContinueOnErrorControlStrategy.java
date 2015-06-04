package com.isencia.passerelle.process.actor;

import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.message.ManagedMessage;

public class ContinueOnErrorControlStrategy extends DefaultActorErrorControlStrategy {

  @Override
  public void handleFireException(Actor actor, ProcessingException processingException) throws IllegalActionException {
   
    Object errorContext = processingException.getMsgContext();
    if (errorContext instanceof ManagedMessage && actor instanceof TaskBasedActor && processingException.getErrorCode().getSeverity() != Severity.FATAL) {
      TaskBasedActor taskBasedActor = (TaskBasedActor)actor;
      try {
        if (taskBasedActor.errorPort != null) {
          taskBasedActor.sendOutputMsg(taskBasedActor.errorPort, (ManagedMessage) errorContext);
        }
      } catch (ProcessingException ex) {
        // should never happen in reality...
        getLoggerForActor(actor).error(actor.getFullName() + " unable to send output for processing exception", ex);
        super.handleFireException(actor, processingException);
      }
    } else {
      super.handleFireException(actor, processingException);
    }

  }
  
}
