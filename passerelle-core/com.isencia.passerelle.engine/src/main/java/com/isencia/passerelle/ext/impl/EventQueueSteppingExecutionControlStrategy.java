/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.isencia.passerelle.ext.impl;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.util.BlockingReaderQueue;
import com.isencia.util.EmptyQueueException;
import com.isencia.util.FIFOQueue;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * An execution controller that lets a model execute in stepping mode. Each step allows one actor to do a fire() iteration. A next step is triggered by a
 * stepping request event. Such requests are maintained in an internal queue, from which they are taken for each actor requesting a next iteration. <br>
 * Furthermore it allows to stop the stepping mode and continue in plain running mode.
 * 
 * @author erwin
 */
public class EventQueueSteppingExecutionControlStrategy extends Attribute implements ExecutionControlStrategy {

  private Logger logger = LoggerFactory.getLogger(EventQueueSteppingExecutionControlStrategy.class);

  private BlockingReaderQueue queue;
  private boolean steppingEnabled = true;

  /**
	 * 
	 */
  public EventQueueSteppingExecutionControlStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    DirectorUtils.getAdapter(container, null).setExecutionControlStrategy(this);
    // only for usage inside a IDE or HMI and these will add it everytime it's needed
    setPersistent(false);

    queue = new BlockingReaderQueue(new FIFOQueue(100));
  }

  public synchronized IterationPermission requestNextIteration(Actor actor) {
    if (logger.isTraceEnabled())
      logger.trace("requestNextIteration() - entry - actor " + ((NamedObj) actor).getName());

    try {
      if (isSteppingEnabled())
        queue.get();
      if (logger.isTraceEnabled())
        logger.trace("requestNextIteration() - granted for actor " + ((NamedObj) actor).getName());
    } catch (EmptyQueueException e) {
      // TODO investigate what's best behaviour now
    }

    return null;
  }

  public void iterationFinished(Actor actor, IterationPermission itPerm) {
    if (logger.isTraceEnabled())
      logger.trace("iterationFinished() - entry - actor " + ((NamedObj) actor).getName());

    if (logger.isTraceEnabled())
      logger.trace("iterationFinished() - exit");
  }

  /**
   * Request a next step in the execution
   */
  public void step() {
    queue.put(new Date());
  }

  public void resume() {
    setSteppingEnabled(false);
  }

  public void setSteppingEnabled(boolean enabled) {
    steppingEnabled = enabled;
    if (!enabled) {
      // to ensure that the thing immediately continues...
      step();
    }
  }

  public boolean isSteppingEnabled() {
    return steppingEnabled;
  }
}
