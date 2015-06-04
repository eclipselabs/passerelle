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

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.ValidationException;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A Passerelle actor can have at most 1 ErrorControlStrategy. 
 * This one can determine what must be done in case of an exception
 * during a model execution.
 * <br>
 * Examples of options are:
 * <ul>
 * <li>just log the exception
 * <li>terminate the model execution
 * <li>retry the actor.fire() of the actor where the exception was generated
 * <li>ask a user to decide
 * <li>... 
 * </ul>
 * ErrorControlStrategies may be registered per individual actor. If an actor
 * has no dedicated strategy, it will delegate to the Director's one.
 * <p>
 * RuntimeExceptions in the initialization and termination phases cannot be handled
 * in custom ways. Passerelle will always stop the model execution in such cases.
 * During the fire-cycle however, custom ErrorControlStrategies may react in any
 * desired/mysterious way. 
 * 
 * @author erwin
 *
 */
public interface ErrorControlStrategy {
	/**
	 * 
	 * @param a the actor where the InitializationException was generated
	 * @param e
	 */
	void handleInitializationException(Actor a, InitializationException e) throws IllegalActionException;
	/**
	 * 
	 * @param actor the actor where the ValidationException was generated
	 * @param e
	 * @throws IllegalActionException
	 */
	void handleInitializationValidationException(Actor actor, ValidationException e) throws IllegalActionException;
	/**
	 * 
	 * @param actor the actor where the ValidationException was generated
	 * @param e
	 * @throws IllegalActionException
	 */
	void handleIterationValidationException(Actor actor, ValidationException e) throws IllegalActionException;
	/**
	 * 
	 * @param a the actor where the ProcessingException was generated
	 * @param e
	 */
	void handlePreFireException(Actor a, ProcessingException e) throws IllegalActionException;
	void handleFireException(Actor a, ProcessingException e) throws IllegalActionException;
	void handlePostFireException(Actor a, ProcessingException e) throws IllegalActionException;
	/**
	 * 
	 * @param a the actor where the TerminationException was generated
	 * @param e
	 */
	void handleTerminationException(Actor a, TerminationException e) throws IllegalActionException;
	/**
	 * 
	 * @param a the actor where the RuntimeException was generated
	 * @param e
	 */
	void handlePreFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException;
	/**
	 * 
	 * @param a the actor where the RuntimeException was generated
	 * @param e
	 */
	void handleFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException;
	/**
	 * 
	 * @param a the actor where the RuntimeException was generated
	 * @param e
	 */
	void handlePostFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException;
}
