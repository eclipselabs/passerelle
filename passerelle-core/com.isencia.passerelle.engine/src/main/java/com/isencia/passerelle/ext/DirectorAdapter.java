/* Copyright 2012 - iSencia Belgium NV

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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.PasserelleException;

/**
 * An interface grouping all extension features that Passerelle assigns to a Director, to centralize them in one spot in a model.
 * 
 * @author erwin
 */
public interface DirectorAdapter {

  public static final String STOP_FOR_UNHANDLED_ERROR_PARAM = "Stop for unhandled Error";
  public static final String MOCKMODE_PARAM = "Mock Mode";
  public static final String EXPERTMODE_PARAM = "Expert Modeler";
  public static final String VALIDATE_INITIALIZATION_PARAM = "Validate Initialization";
  public static final String VALIDATE_ITERATION_PARAM = "Validate Iteration";
  String DEFAULT_ADAPTER_NAME = "__directorAdapter";

  /**
   * Register the given instance as an overall error collector.
   * <p>
   * Since Passerelle v8.3, the usage of these collectors has changed a bit.
   * <br/>
   * Before they were typically implemented by an actor that was added at the top level of a model,
   * receiving all errors that were not handled via actor error ports.
   * <br/>
   * Now those actors receive the errors per model-level, and are registered on the respective CompositeEntity.
   * Any ErrorCollectors registered via this method will now typically serve as "out-of-model" error handlers.
   * </p>
   * @param errCollector
   */
  void addErrorCollector(ErrorCollector errCollector);

  /**
   * 
   * @param errCollector the one to remove
   * @return true if the collector was previously registered and is now successfully removed
   */
  boolean removeErrorCollector(ErrorCollector errCollector);

  /**
   * remove all registered error collectors
   */
  void removeAllErrorCollectors();

  /**
   * @param modelElement
   *          the model element that reports the error
   * @param e
   */
  void reportError(NamedObj modelElement, PasserelleException e);

  /**
   * @return the error control strategy that was configured on this director
   */
  ErrorControlStrategy getErrorControlStrategy();

  /**
   * Set the error control strategy for this director.
   * 
   * @param errorCtrlStrategy
   * @param enforceThisOne
   *          if true, overwrite any previously set errorCtrlStrategy. If false, any previous one will remain active.
   */
  void setErrorControlStrategy(ErrorControlStrategy errorCtrlStrategy, boolean enforceThisOne);

  /**
   * Execution control strategies can be used to plugin an external control on a model's execution, adapting/overriding default execution mechanisms as
   * determined by the selected director domain.
   * 
   * @return the configured execution control strategy
   */
  ExecutionControlStrategy getExecutionControlStrategy();

  /**
   * Execution control strategies can be used to plugin an external control on a model's execution, adapting/overriding default execution mechanisms as
   * determined by the selected director domain.
   * 
   * @param execCtrlStrategy
   */
  void setExecutionControlStrategy(ExecutionControlStrategy execCtrlStrategy);

  /**
   * A pre/post processor can be configured as a kind of interceptor for a model execution, providing custom logic that should be executed at the start/finish
   * of a model run. It is typically used in situations where common non-functional logic is needed with which a model designer should not be bothered each
   * time.
   * 
   * @param execPrePostProcessor
   */
  void setExecutionPrePostProcessor(ExecutionPrePostProcessor execPrePostProcessor);

  /**
   * @return the configured pre/post execution processor
   */
  ExecutionPrePostProcessor getExecutionPrePostProcessor();

  /**
   * @return whether a flow execution should be stopped when an actor error is not handled
   * in the model or through a registered ErrorHandler or ErrorCollector.
   */
  boolean isStopForUnhandledError();
  
  /**
   * @return Returns the mockMode.
   */
  boolean isMockMode();

  /**
   * @return Returns the expertMode.
   */
  boolean isExpertMode();

  /**
   * @return whether each actor should do a validation of its initialization
   */
  boolean mustValidateInitialization();

  /**
   * @return whether each iteration of each actor should do a validation
   */
  boolean mustValidateIteration();

  /**
   * @return all configurable parameters
   */
  Parameter[] getConfigurableParameters();

  /**
   * Register a director parameter as configurable. Such parameters will be available in the Passerelle model configuration tools. All other actor parameters
   * are only available in model assembly tools.
   * 
   * @param newParameter
   */
  void registerConfigurableParameter(Parameter newParameter);

  /**
   * Notify the director that the actor is initialized and ready to start working.
   * @param actor
   */
  void notifyActorActive(Actor actor);
  
  /**
   * Notify the director that the actor is no longer active in the model execution,
   * i.e. it has indicated its postFire=false.
   * @param actor
   */
  void notifyActorInactive(Actor actor);
  
  /**
   * @param actor
   * @return true if the actor is active for work in the current execution.
   */
  boolean isActorActive(Actor actor);
  
  /**
   * Remark : in multi-threaded execution models, actor state may be reported
   * from different threads. This implies that code iterating over the returned set
   * should be prepared to handle {@link ConcurrentModificationException}s.
   * 
   * @return an unmodifiable view on the set of all active actors
   */
  Collection<Actor> getActiveActors();
  
  /**
   * remove all actor state indicators
   */
  void clearExecutionState();

  /**
   * @return true if any actor in the model is currently busy processing a task
   */
  boolean hasBusyTaskActors();

  /**
   * @param actor
   * @return true if the actor is part of the director's executing model and is currently busy processing a task
   */
  boolean isActorBusy(Actor actor);

  /**
   * Actors should call this method in the beginning of their actual fire/process work, (optionally) passing some task object that can serve as key to identify
   * the unit-of-work that they're executing. If task is null here, it should also be passed as null in <code>notifyActorFinishedTask</code>.
   * 
   * @param actor
   * @param task
   */
  void notifyActorStartedTask(Actor actor, Object task);

  /**
   * @param actor
   * @param task
   * @throws IllegalArgumentException
   *           when the given task is not registered as busy for the given actor.
   */
  void notifyActorFinishedTask(Actor actor, Object task) throws IllegalArgumentException;

  /**
   * Register a listener that will be notified of ALL actor iteration transitions.
   * 
   * @see ptolemy.actor.FiringEvent
   * @param listener
   */
  void registerFiringEventListener(FiringEventListener listener);

  /**
   * @param listener
   * @return true if the listener was registered (and is now removed)
   */
  boolean removeFiringEventListener(FiringEventListener listener);

  /**
   * @return true if at least 1 listener is registered
   */
  boolean hasFiringEventListeners();

  /**
   * Forward the event to all registered listeners, iff the event is not-null and its director is me. The listener-related methods are NOT synchronized, to
   * ensure that the model execution does not block completely because of a blocking/long action of a listener... So there's no guarantee against race
   * conditions when someone starts modifying the listener set during model execution!
   * 
   * @param event
   */
  void notifyFiringEventListeners(FiringEvent event);
}