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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.ext.ExecutionControlStrategy;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.NamedObj;


/**
 * Default implementation, supporting just the Passerelle-specific per-actor pause/resume feature.
 * 
 * Moreover it behaves as a "composite" strategy, or more correctly a wrapper, optionally containing
 * one other strategy implementation. 
 * 
 * @author erwin
 *
 */
public class DefaultExecutionControlStrategy implements
		ExecutionControlStrategy {
	
	private Logger logger = LoggerFactory.getLogger(DefaultExecutionControlStrategy.class);
	
	private Map<Actor, Condition> pausedActors = new HashMap<Actor, Condition>();
	private ReentrantLock pauseLock = new ReentrantLock();
	
	private boolean modelFinished = false;
	
	private ExecutionControlStrategy delegate = null;
	
	public void setDelegate(ExecutionControlStrategy delegate) {
		this.delegate = delegate;
	}
	
	public ExecutionControlStrategy getDelegate() {
		return delegate;
	}
	
	/**
	 * Indicate that the execution iteration for the given actor
	 * should be paused.
	 * <br/>
	 * If the actor was already paused, this call has no impact,
	 * but just returns false.
	 * 
	 * @param actor
	 * @return true if actor iteration was not yet paused before this call
	 * false if actor is already paused.
	 */
	public boolean pause(Actor actor) {
		if(actor!=null) {
			try {
				pauseLock.lockInterruptibly();
				if(pausedActors.containsKey(actor)) {
					return false;
				} else {
					pausedActors.put(actor, pauseLock.newCondition());
					return true;
				}
			} catch (InterruptedException e) {
				logger.error("pause failed for actor "+actor.getFullName(),e);
				return false;
			} finally {
				try {pauseLock.unlock(); } catch (Exception e) {}
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Indicate that the execution iteration for the given actor
	 * should be resumed.
	 * <br/>
	 * If the actor was not paused, this call has no impact,
	 * but just returns false.
	 * 
	 * @param actor
	 * @return true if actor iteration was paused before this call
	 * false if actor is not paused.
	 */
	public boolean resume(Actor actor) {
		Condition pauseCondition = pausedActors.remove(actor);
		if(pauseCondition!=null) {
			pauseCondition.signalAll();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Default implementation, returning immediately
	 * for all requests, i.e. no synchronization/stepping/... whatsoever.
	 */
	public IterationPermission requestNextIteration(Actor actor) {
		if(logger.isTraceEnabled())
			logger.trace("requestNextIteration() - entry - actor "+((NamedObj)actor).getName());
		
		try {
			Condition pauseIndicator = pausedActors.get(actor);
			
			if(pauseIndicator!=null) {
				// wait till this actor resumes
				// then...
				try {
					pauseLock.lockInterruptibly();
					pauseIndicator.await(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					
				}
				return null;
			} else if (delegate!=null) {
				return delegate.requestNextIteration(actor);
			} else {
				return null;
			}
		} finally {
			if(logger.isTraceEnabled())
				logger.trace("requestNextIteration() - exit");
		}
	}

	public void iterationFinished(Actor actor, IterationPermission itPerm) {
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - entry - actor "+((NamedObj)actor).getName());
		
		if (delegate!=null) {
			delegate.iterationFinished(actor, itPerm);
		}
			
		if(logger.isTraceEnabled())
			logger.trace("iterationFinished() - exit");
	}
}
