package com.isencia.passerelle.actor.advanced;

import java.util.Timer;
import java.util.TimerTask;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An actor that buffers incoming messages for a configurable delay time, and then lets them continue on their way.
 * 
 * @author erwin
 * 
 */
public class DelayActor extends Actor {
  private static final long serialVersionUID = 1L;
  private Timer delayTimer;
  public Port input;
  public Port output;
  public Parameter delayParameter;
  private int pendingTasks = 0;

  public DelayActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    delayParameter = new Parameter(this, "Delay [s]", new IntToken(3));
  }
  
  @Override
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.ASYNCHRONOUS;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    pendingTasks = 0;
    delayTimer = new Timer("Timer for " + getName());
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage msg = request.getMessage(input);
    int delay = getDelay(msg, Integer.parseInt(delayParameter.getExpression()));
    if (delay > 0) {
      delayTimer.schedule(new DelayTask(response), delay * 1000);
    } else {
      response.addOutputMessage(output, msg);
    }
  }

  /**
   * The actual processing, triggered from the delayed task.
   * This just forwards the received message.
   * 
   * @param ctxt
   * @param request
   * @param response
   * @throws ProcessingException
   */
  protected void _process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    response.addOutputMessage(output, request.getMessage(input));
  }
  
  @Override
  protected boolean doPostFire() throws ProcessingException {
    boolean result = super.doPostFire();
    if (!result) {
      while (pendingTasks > 0) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          break;
        }
      }
      return false;
    }
    return true;
  }
  
  @Override
  protected void doWrapUp() throws TerminationException {
    if(delayTimer!=null) {
      delayTimer.cancel();
    }
    super.doWrapUp();
  }

  /**
   * Overridable method to be able to change the delay, according to specific needs, in subclasses.
   * 
   * @param msg
   * @param defaultDelay
   * @return the delay for the message
   */
  protected int getDelay(ManagedMessage msg, int defaultDelay) {
    return defaultDelay;
  }
  
  public class DelayTask extends TimerTask {
    private ProcessResponse response;

    public DelayTask(ProcessResponse response) {
      this.response = response;
      DelayActor.this.pendingTasks++;
    }

    @Override
    public void run() {
      try {
        DelayActor.this._process(response.getContext(), response.getRequest(), response);
      } catch (ProcessingException e) {
        response.setException(e);
      } finally {
        DelayActor.this.processFinished(response.getContext(), response.getRequest(), response);
        DelayActor.this.pendingTasks--;
      }
    }
  }
}
