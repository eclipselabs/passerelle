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

package com.isencia.passerelle.ext.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.ext.ConfigurationExtender;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.ErrorHandler;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;
import com.isencia.passerelle.ext.FiringEventListener;

/**
 * @author erwin
 */
public class DefaultDirectorAdapter extends Attribute implements DirectorAdapter, ConfigurationExtender {
  private static final long serialVersionUID = 1L;

  private Logger LOGGER;

  /**
   * The collection of parameters that are meant to be available to a model configurer tool. The actor's parameters that are not in this collection are not
   * meant to be configurable, but are only meant to be used during model assembly (in addition to the public ones).
   */
  private Collection<Parameter> configurableParameters;

  /**
   * The collection of listeners for FiringEvents. If the collection is empty, no events are generated. If non-empty, inside the ProcessThread.run(), lots of
   * events are generated for each transition in the iteration of an actor.
   */
  private Collection<FiringEventListener> firingEventListeners;

  /**
   * The collection of error collectors, to which the Director forwards any reported errors. If the collection is empty, reported errors are logged.
   */
  private Collection<ErrorCollector> errorCollectors;

  private DefaultExecutionControlStrategy execCtrlStrategy = new DefaultExecutionControlStrategy();
  private ExecutionPrePostProcessor execPrePostProcessor = new DefaultExecutionPrePostProcessor();

  private ErrorControlStrategy errorCtrlStrategy = new DefaultActorErrorControlStrategy();
  private boolean enforcedErrorCtrlStrategy;

  private Parameter stopForUnhandledErrorParam = null;
  private Parameter mockModeParam = null;
  private Parameter expertModeParam = null;
  private Parameter validateInitializationParam = null;
  private Parameter validateIterationParam = null;

  // Need some collection to maintain info about busy tasks
  // i.e. for slow actions done by actors.
  // Seems interesting to store a tuple {actor, taskHandle, startTime}.
  // Probably through a dedicated Event type?
  // In models with loops or request-trains (e.g. DARE), a same actor could
  // be handling several tasks concurrently.
  // The taskHandle could be used to link to any domain-specific task entity.
  // The startTime could be used for internal CEP, timeout mgmt etc.
  private ConcurrentMap<Object, Actor> busyTaskActors;

  // maintains which actors have already indicated that they're no longer
  // participating in the current model execution
  private ConcurrentLinkedQueue<Actor> activeActors;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DefaultDirectorAdapter(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    LOGGER = LoggerFactory.getLogger(container.getClass().getName() + "." + this.getClass().getName());

    init();
    
    if (container.getAttribute(STOP_FOR_UNHANDLED_ERROR_PARAM) != null) {
      stopForUnhandledErrorParam = (Parameter) container.getAttribute(STOP_FOR_UNHANDLED_ERROR_PARAM);
    } else {
      stopForUnhandledErrorParam = new Parameter(container, STOP_FOR_UNHANDLED_ERROR_PARAM, new BooleanToken(false));
      stopForUnhandledErrorParam.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(stopForUnhandledErrorParam, "style");
      registerConfigurableParameter(stopForUnhandledErrorParam);
    }

    if (container.getAttribute(MOCKMODE_PARAM) != null) {
      mockModeParam = (Parameter) container.getAttribute(MOCKMODE_PARAM);
    } else {
      mockModeParam = new Parameter(container, MOCKMODE_PARAM, new BooleanToken(false));
      mockModeParam.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(mockModeParam, "style");
      registerConfigurableParameter(mockModeParam);
    }

    if (container.getAttribute(EXPERTMODE_PARAM) != null) {
      expertModeParam = (Parameter) container.getAttribute(EXPERTMODE_PARAM);
    } else {
      expertModeParam = new Parameter(container, EXPERTMODE_PARAM, new BooleanToken(false));
      expertModeParam.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(expertModeParam, "style");
      registerConfigurableParameter(expertModeParam);
    }

    if (container.getAttribute(VALIDATE_INITIALIZATION_PARAM) != null) {
      validateInitializationParam = (Parameter) container.getAttribute(VALIDATE_INITIALIZATION_PARAM);
    } else {
      validateInitializationParam = new Parameter(container, VALIDATE_INITIALIZATION_PARAM, new BooleanToken(true));
      validateInitializationParam.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(validateInitializationParam, "style");
      registerConfigurableParameter(validateInitializationParam);
    }

    if (container.getAttribute(VALIDATE_ITERATION_PARAM) != null) {
      validateIterationParam = (Parameter) container.getAttribute(VALIDATE_ITERATION_PARAM);
    } else {
      validateIterationParam = new Parameter(container, VALIDATE_ITERATION_PARAM, new BooleanToken(false));
      validateIterationParam.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(validateIterationParam, "style");
      registerConfigurableParameter(validateIterationParam);
    }
  }
  
  protected void init() {
    configurableParameters = new HashSet<Parameter>();
    firingEventListeners = Collections.synchronizedSet(new HashSet<FiringEventListener>());
    errorCollectors = Collections.synchronizedSet(new HashSet<ErrorCollector>());
    busyTaskActors = new ConcurrentHashMap<Object, Actor>();
    activeActors = new ConcurrentLinkedQueue<Actor>();
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    DefaultDirectorAdapter clonedAdapter = (DefaultDirectorAdapter)  super.clone(workspace);
    clonedAdapter.init();
    return clonedAdapter;
  }

  public void addErrorCollector(ErrorCollector errCollector) {
    if (errCollector != null) {
      errorCollectors.add(errCollector);
    }
  }

  public boolean removeErrorCollector(ErrorCollector errCollector) {
    boolean res = false;
    if (errCollector != null) {
      res = errorCollectors.remove(errCollector);
    }
    return res;
  }

  public void removeAllErrorCollectors() {
    errorCollectors.clear();
  }

  public void reportError(NamedObj modelElement, PasserelleException e) {
    boolean isHandled = handleError(modelElement, e);
    if (!isHandled) {
      synchronized (errorCollectors) {
        if (!errorCollectors.isEmpty()) {
          for (Iterator<ErrorCollector> errCollItr = errorCollectors.iterator(); errCollItr.hasNext();) {
            ErrorCollector element = errCollItr.next();
            element.acceptError(e);
          }
        } else {
          LOGGER.error("reportError() - no errorCollectors but received exception", e);
          Manager manager = ((CompositeActor) toplevel()).getManager();
          manager.notifyListenersOfException(e);
          if (isStopForUnhandledError()) {
            manager.finish();
          }
        }
      }
    }
  }

  private boolean handleError(NamedObj errorSource, PasserelleException error) {
    boolean result = false;
    NamedObj container = errorSource;
    while (!(container instanceof CompositeEntity) && (container != null)) {
      container = container.getContainer();
    }
    while (!result && (container != null)) {
      result = handleErrorOnOneLevel((CompositeEntity) container, errorSource, error);
      container = container.getContainer();
    }
    return result;
  }

  private boolean handleErrorOnOneLevel(CompositeEntity parentComposite, NamedObj errorSource, PasserelleException error) {
    boolean result = false;
    List<ErrorHandler> errHandlerList = parentComposite.entityList(ErrorHandler.class);
    for (ErrorHandler errorHandler : errHandlerList) {
      result = errorHandler.handleError(errorSource, error) || result;
    }
    return result;
  }

  public ErrorControlStrategy getErrorControlStrategy() {
    return errorCtrlStrategy;
  }

  public void setErrorControlStrategy(ErrorControlStrategy errorCtrlStrategy, boolean enforceThisOne) {
    if (enforceThisOne || !this.enforcedErrorCtrlStrategy) {
      this.errorCtrlStrategy = errorCtrlStrategy;
      this.enforcedErrorCtrlStrategy = enforceThisOne;
    }
  }

  public ExecutionControlStrategy getExecutionControlStrategy() {
    return execCtrlStrategy.getDelegate();
  }

  public void setExecutionControlStrategy(ExecutionControlStrategy execCtrlStrategy) {
    this.execCtrlStrategy.setDelegate(execCtrlStrategy);
  }

  public void setExecutionPrePostProcessor(ExecutionPrePostProcessor execPrePostProcessor) {
    this.execPrePostProcessor = execPrePostProcessor;
  }

  public ExecutionPrePostProcessor getExecutionPrePostProcessor() {
    return execPrePostProcessor;
  }

  public boolean isStopForUnhandledError() {
    try {
      return ((BooleanToken) stopForUnhandledErrorParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean isMockMode() {
    try {
      return ((BooleanToken) mockModeParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean isExpertMode() {
    try {
      return ((BooleanToken) expertModeParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean mustValidateInitialization() {
    try {
      return ((BooleanToken) validateInitializationParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public boolean mustValidateIteration() {
    try {
      return ((BooleanToken) validateIterationParam.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      return false;
    }
  }

  public Parameter[] getConfigurableParameters() {
    return (Parameter[]) configurableParameters.toArray(new Parameter[0]);
  }

  public void registerConfigurableParameter(Parameter newParameter) {
    if (newParameter != null && !configurableParameters.contains(newParameter) && newParameter.getContainer().equals(this)) {
      configurableParameters.add(newParameter);
    }
  }

  public void registerFiringEventListener(FiringEventListener listener) {
    if (listener != null) {
      firingEventListeners.add(listener);
    }
  }

  public boolean removeFiringEventListener(FiringEventListener listener) {
    return firingEventListeners.remove(listener);
  }

  public boolean hasFiringEventListeners() {
    return !firingEventListeners.isEmpty();
  }

  public void notifyFiringEventListeners(FiringEvent event) {
    if (event != null && event.getDirector().equals(getContainer())) {
      synchronized (firingEventListeners) {
        for (Iterator<FiringEventListener> listenerItr = firingEventListeners.iterator(); listenerItr.hasNext();) {
          FiringEventListener listener = listenerItr.next();
          listener.onEvent(event);
        }
      }
    }
  }

  public void notifyActorActive(Actor actor) {
    LOGGER.debug("notifyActorActive() - Marking actor {} as active.", actor.getFullName());
    synchronized (activeActors) {
      activeActors.add(actor);
    }
  }

  public void notifyActorInactive(Actor actor) {
    LOGGER.debug("notifyActorInactive() - Marking actor {} as inactive.", actor.getFullName());
    if (!activeActors.remove(actor)) {
      LOGGER.warn("notifyActorInactive() - Actor {} was not marked as active", actor.getFullName());
    }
  }

  public boolean isActorActive(Actor actor) {
    return activeActors.contains(actor);
  }

  public Collection<Actor> getActiveActors() {
    return activeActors;
  }

  public void clearExecutionState() {
    LOGGER.debug("clearExecutionState() - {}", getFullName());
    busyTaskActors.clear();
    activeActors.clear();
  }

  public boolean hasBusyTaskActors() {
    return busyTaskActors.size() > 0;
  }

  public boolean isActorBusy(Actor actor) {
    return busyTaskActors.containsValue(actor);
  }

  public void notifyActorStartedTask(Actor actor, Object task) {
    if (isActorBusy(actor)) {
      LOGGER.info("notifyActorStartedTask() - Extra task {} passed for busy actor {}", task, actor.getFullName());
    }
    if (task == null) {
      LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode() + " - notifyActorStartedTask() - Null task passed for actor {}", actor.getFullName());
      // no task differentiation possible, so just use the actor itself as key
      task = actor;
    }
    Actor actor2 = busyTaskActors.putIfAbsent(task, actor);
    if (actor2 != null) {
      LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode()
          + " - notifyActorStartedTask() - Task {} passed for actor {}, but already linked with actor {}",
          new Object[] { task, actor.getFullName(), actor2.getFullName() });
    } else {
      LOGGER.debug("notifyActorStartedTask() - Task {} started for actor {}", task, actor.getFullName());
    }
    // TODO : could be interesting to generate events for this?
    // enqueueEvent(new TaskStartedEvent(task, actor));
  }

  public void notifyActorFinishedTask(Actor actor, Object task) throws IllegalArgumentException {
    if (!isActorBusy(actor)) {
      LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode() + " - notifyActorFinishedTask() - Task {} passed for non-busy actor {}", task,
          actor.getFullName());
    }
    if (task == null) {
      LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode() + " - notifyActorFinishedTask() - Null task passed for actor {}", actor.getFullName());
      // no task differentiation possible, so just use the actor itself as key
      task = actor;
    }
    if (actor == busyTaskActors.get(task)) {
      boolean removed = busyTaskActors.remove(task, actor);
      if (removed) {
        LOGGER.debug("notifyActorFinishedTask() - Task {} finished for actor {}", task, actor.getFullName());
      } else {
        LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode() + " - notifyActorFinishedTask() - Removal failed for task {} and actor {}", task,
            actor.getFullName());
      }
    } else {
      LOGGER.error(ErrorCode.FLOW_STATE_ERROR.getFormattedCode() + " - notifyActorFinishedTask() - Task {} was not linked with actor {}", task,
          actor.getFullName());
    }
    // TODO : could be interesting to generate events for this?
    // enqueueEvent(new TaskFinishedEvent(task, actor));
  }
}
