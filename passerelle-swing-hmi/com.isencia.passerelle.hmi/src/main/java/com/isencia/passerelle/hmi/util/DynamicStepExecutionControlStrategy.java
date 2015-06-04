/*
 * (c) Copyright 2008, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */

package com.isencia.passerelle.hmi.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ExecutionControlStrategy;

/**
 * An execution controller that lets a model execute in stepping mode. Each step
 * allows one actor to do a fire() iteration. A next step is triggered by a
 * stepping request event. Such requests are maintained in an internal queue,
 * from which they are taken for each actor requesting a next iteration. <br>
 * Furthermore it allows to stop the stepping mode and continue in plain running
 * mode.
 * 
 * @author erwin
 */
public class DynamicStepExecutionControlStrategy extends Attribute implements ExecutionControlStrategy {

  private final Logger logger = LoggerFactory.getLogger(DynamicStepExecutionControlStrategy.class);

  /**
   * flag indicating whether we should enforce stepping mode or not
   */
  private final AtomicBoolean steppingEnabled = new AtomicBoolean(true);

  private final Semaphore stepEventCount;
  private final Semaphore busyActorCount;

  public DynamicStepExecutionControlStrategy(final Director container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    DirectorUtils.getAdapter(container, null).setExecutionControlStrategy(this);
    // this kind of attribute is only for usage inside a IDE or HMI and
    // these will add it everytime it's needed
    // so we must make sure it's not saved into the model's moml
    // inadvertently...
    setPersistent(false);

    busyActorCount = new Semaphore(0);
    stepEventCount = new Semaphore(0);
  }

  public IterationPermission requestNextIteration(final Actor actor) {
    if (logger.isTraceEnabled()) {
      logger.trace("requestNextIteration() - entry - actor " + ((NamedObj) actor).getName());
    }

    try {
      if (isSteppingEnabled()) {
        // block until a step event is available
        stepEventCount.acquire();
        stepEventCount.release();
      } else {
        // don't block iterations
      }
      // increase busy count
      busyActorCount.release();
    } catch (final InterruptedException e) {
      // TODO investigate what's best behaviour now
    }

    if (logger.isTraceEnabled()) {
      logger.trace("requestNextIteration() - granted for actor " + ((NamedObj) actor).getName());
    }

    return null;
  }

  public void iterationFinished(final Actor actor, final IterationPermission itPerm) {
    if (logger.isTraceEnabled()) {
      logger.trace("iterationFinished() - entry - actor " + ((NamedObj) actor).getName());
    }

    // decrease busy count
    busyActorCount.tryAcquire();

    if (logger.isTraceEnabled()) {
      logger.trace("iterationFinished() - exit");
    }
  }

  /**
   * Request a next step in the execution. If a step is ongoing, ignore this
   * invocation, i.e. a next step request is only taken into account after the
   * previous one has finished.
   */
  public void step() {
    if (logger.isTraceEnabled()) {
      logger.trace("step() - entry");
    }
    stepEventCount.release();
    if (logger.isTraceEnabled()) {
      logger.trace("step() - exit");
    }
  }

  /**
   * Indicate that the end of a step has been reached.
   */
  public void stopStep() {
    if (logger.isTraceEnabled()) {
      logger.trace("stopStep() - entry");
    }
    stepEventCount.tryAcquire();
    if (logger.isTraceEnabled()) {
      logger.trace("stopStep() - exit");
    }
  }

  public void resume() {
    if (logger.isTraceEnabled()) {
      logger.trace("resume() - entry");
    }
    setSteppingEnabled(false);
    if (logger.isTraceEnabled()) {
      logger.trace("resume() - exit");
    }
  }

  public void setSteppingEnabled(final boolean enabled) {
    if (logger.isTraceEnabled()) {
      logger.trace("setSteppingEnabled() - entry : " + enabled);
    }
    steppingEnabled.set(enabled);
    if (!enabled) {
      // make sure any blocked actors can do their thing now
      step();
    } else {
      // make sure to remove any lingering step events
      // from before becoming stepping-enabled (again)
      stepEventCount.drainPermits();
    }
    if (logger.isTraceEnabled()) {
      logger.trace("setSteppingEnabled() - exit");
    }
  }

  public boolean isSteppingEnabled() {
    return steppingEnabled.get();
  }
}
