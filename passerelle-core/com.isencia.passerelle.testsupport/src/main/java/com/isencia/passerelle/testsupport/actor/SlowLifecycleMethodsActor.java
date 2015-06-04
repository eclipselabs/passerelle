package com.isencia.passerelle.testsupport.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;

/**
 * An actor that can be configured with delays for executing each of its lifecycle methods.
 * This can be useful for testing concurrency issues.
 * (delays are in ms) 
 * @author erwindl
 *
 */
public class SlowLifecycleMethodsActor extends Forwarder {
  private static final long serialVersionUID = -4585285885377977433L;
  private static final Logger LOGGER = LoggerFactory.getLogger(SlowLifecycleMethodsActor.class);
  private boolean fireInterrupted;
  private boolean flowExecutionStopped;

  public Parameter initDelayParameter;
  public Parameter postFireDelayParameter;
  public Parameter preFireDelayParameter;
  public Parameter preInitDelayParameter;
  public Parameter processDelayParameter;
  public Parameter wrapupDelayParameter;
  
  public SlowLifecycleMethodsActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    initDelayParameter = new Parameter(this, "init delay", new IntToken(0));
    postFireDelayParameter = new Parameter(this, "postFire delay", new IntToken(0));
    preFireDelayParameter = new Parameter(this, "preFire delay", new IntToken(0));
    preInitDelayParameter = new Parameter(this, "preInit delay", new IntToken(0));
    processDelayParameter = new Parameter(this, "process delay", new IntToken(0));
    wrapupDelayParameter = new Parameter(this, "wrapup delay", new IntToken(1000));
  }

  
  @Override
  protected void doInitialize() throws InitializationException {
    fireInterrupted = false;
    flowExecutionStopped = false;
    waitForGivenTime(initDelayParameter);
    super.doInitialize();
  }
  
  @Override
  protected boolean doPostFire() throws ProcessingException {
    waitForGivenTime(postFireDelayParameter);
    return super.doPostFire();
  }
  @Override
  protected boolean doPreFire() throws ProcessingException {
    waitForGivenTime(preFireDelayParameter);
    return super.doPreFire();
  }
  @Override
  protected void doPreInitialize() throws InitializationException {
    waitForGivenTime(preInitDelayParameter);
    super.doPreInitialize();
  }
  @Override
  protected void doWrapUp() throws TerminationException {
    waitForGivenTime(wrapupDelayParameter);
    super.doWrapUp();
  }
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    waitForGivenTime(processDelayParameter);
    super.process(ctxt, request, response);
  }


  protected void waitForGivenTime(Parameter timeParameter) {
    fireInterrupted = false;
    try {
      int time = ((IntToken)timeParameter.getToken()).intValue();
      if (time > 0) {
        int count = time/100;
        for (int i = 0; i < count; ++i) {
          Thread.sleep(100);
          if (isFinishRequested() || fireInterrupted || flowExecutionStopped) {
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      // do nothing, means someone wants us to stop
    } catch (Exception e) {
      LOGGER.error("", new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error in delay processing", this, e));
    }
  }

  @Override
  protected void doStopFire() {
    super.doStopFire();
    fireInterrupted = true;
  }
  
  @Override
  protected void doStop() {
    super.doStop();
    flowExecutionStopped = true;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
  
}
