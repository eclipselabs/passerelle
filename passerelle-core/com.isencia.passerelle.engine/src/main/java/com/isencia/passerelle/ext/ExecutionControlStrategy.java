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

package com.isencia.passerelle.ext;

import ptolemy.actor.Actor;


/**
 * Contract for components that can determine execution
 * timing, synchronization etc. for actor fire-iterations.
 * 
 * E.g. currently used for a "stepping" mode, where 1 actor at a time can
 * perform a fire-iteration.
 * 
 * Implementations of this strategy are typically seen as Director extensions/attributes.
 * Check the Passerelle developer guides for general guidelines on implementing
 * and configuring extension components.
 * 
 * @author erwin
 *
 */
public interface ExecutionControlStrategy {
	
	/**
	 * Marker interface for objects identifying an iteration "permission"
	 * obtained by an actor's iteration thread.
	 * 
	 * @author erwin
	 *
	 */
	interface IterationPermission {
	}

	/**
	 * Implementations of this method should block until the given actor
	 * is allowed to do a next iteration.
	 * 
	 * @param actor
	 * @return an object identifying the current permission for the actor
	 * to do 1 iteration. May be null for strategy implementations that 
	 * don't need to care about such things...
	 */
	IterationPermission requestNextIteration(Actor actor);
	
	/**
	 * When an actor's iteration is done, it should notify the ExecutionControlStrategy
	 * about this, returning the IterationPermission that it received for this iteration.
	 * 
	 * @param actor
	 * @param itPerm
	 */
	void iterationFinished(Actor actor, IterationPermission itPerm);
}
