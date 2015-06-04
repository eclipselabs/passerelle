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
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.ExecutionControlStrategy;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * An execution controller that blocks a model execution until a start signal
 * is received, and stops the execution when a stop signal is received.
 * 
 * @author erwin
 *
 */
public class StartStopExecutionControlStrategy extends Attribute implements
		ExecutionControlStrategy {

	private Logger logger = LoggerFactory.getLogger(StartStopExecutionControlStrategy.class);
	
	private boolean started, stopped = false;

	/**
	 * 
	 */
	public StartStopExecutionControlStrategy(Director container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
		DirectorUtils.getAdapter(container, null).setExecutionControlStrategy(this);
	}
	
	public synchronized boolean isStarted() {
		return started;
	}

	public synchronized boolean isStopped() {
		return stopped;
	}

	public synchronized void start() {
		this.started = true;
		notifyAll();
	}
	
	public synchronized void stop() {
		this.stopped = true;
		notifyAll();
	}

	public synchronized IterationPermission requestNextIteration(Actor actor) {
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - entry - actor "+((NamedObj)actor).getName());
		
		while(!isStarted() && ! isStopped()) {
			try {
				wait();
			} catch (InterruptedException e) {
				break;
			}
		}
		
		if(isStopped()) {
			((Director)getContainer()).stop();
		}
		
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - exit");
		
		return null;
	}
	
	public synchronized void iterationFinished(Actor actor, IterationPermission itPerm) {
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - entry - actor "+((NamedObj)actor).getName());
		
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - exit");
	}
}
