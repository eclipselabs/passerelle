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
 * An execution controller that allows a model execution until a suspend signal
 * is received. After that it keeps the execution waiting until a resume signal
 * is received. The suspend/resume actions can be repeated (in the right order).
 * 
 * @author erwin
 *
 */
public class SuspendResumeExecutionControlStrategy extends Attribute implements
		ExecutionControlStrategy {

	private static Logger logger = LoggerFactory.getLogger(SuspendResumeExecutionControlStrategy.class);
	
	private boolean suspended = false;

	/**
	 * 
	 */
	public SuspendResumeExecutionControlStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
		DirectorUtils.getAdapter(container, null).setExecutionControlStrategy(this);
		// only for usage inside a IDE or HMI and these will add it everytime it's needed
		setPersistent(false);
	}
	
	public synchronized boolean isSuspended() {
		return suspended;
	}

	public synchronized void suspend() {
		this.suspended = true;
		notifyAll();
	}
	
	public synchronized void resume() {
		this.suspended = false;
		notifyAll();
	}

	public synchronized IterationPermission requestNextIteration(Actor actor) {
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - entry - actor "+((NamedObj)actor).getName());
		
		while(isSuspended()) {
			try {
				wait();
			} catch (InterruptedException e) {
				break;
			}
		}
		
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - exit");
		
		return null;
	}
	
	public void iterationFinished(Actor actor, IterationPermission itPerm) {
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - entry - actor "+((NamedObj)actor).getName());
		
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - exit");
	}

}
