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

import java.util.HashSet;
import java.util.Set;
import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;
import com.isencia.passerelle.ext.FiringEventListener;

/**
 * An implementation that does nothing.
 * Useful as "NullObject" when no real adapter impl is present,
 * e.g. when Passerelle actors are used without a Passerelle director.
 * 
 * @author erwin
 *
 */
public class NullDirectorAdapter implements DirectorAdapter {
  
  private final static Parameter[] EMPTY_PARAMETER_ARRAY = new Parameter[0];
  private final static Set<Actor> EMPTY_ACTIVE_ACTORS = new HashSet<Actor>();

  private DefaultExecutionControlStrategy execCtrlStrategy = new DefaultExecutionControlStrategy();
  private ExecutionPrePostProcessor execPrePostProcessor = new DefaultExecutionPrePostProcessor();

  private ErrorControlStrategy errorCtrlStrategy = new DefaultActorErrorControlStrategy();
  
  private final static NullDirectorAdapter instance = new NullDirectorAdapter();
  
  public final static NullDirectorAdapter getInstance() {
    return instance;
  }
  
  private NullDirectorAdapter() {
  }

  public void addErrorCollector(ErrorCollector errCollector) {
  }

  public boolean removeErrorCollector(ErrorCollector errCollector) {
    return false;
  }

  public void removeAllErrorCollectors() {
  }

  public void reportError(NamedObj modelElement, PasserelleException e) {
  }

  public ErrorControlStrategy getErrorControlStrategy() {
    return errorCtrlStrategy;
  }

  public void setErrorControlStrategy(ErrorControlStrategy errorCtrlStrategy, boolean enforceThisOne) {
  }

  public ExecutionControlStrategy getExecutionControlStrategy() {
    return execCtrlStrategy;
  }

  public void setExecutionControlStrategy(ExecutionControlStrategy execCtrlStrategy) {
  }

  public void setExecutionPrePostProcessor(ExecutionPrePostProcessor execPrePostProcessor) {
  }

  public ExecutionPrePostProcessor getExecutionPrePostProcessor() {
    return execPrePostProcessor;
  }

  public boolean isStopForUnhandledError() {
    return false;
  }
  public boolean isMockMode() {
    return false;
  }

  public boolean isExpertMode() {
    return false;
  }

  public boolean mustValidateInitialization() {
    return false;
  }

  public boolean mustValidateIteration() {
    return false;
  }

  public Parameter[] getConfigurableParameters() {
    return EMPTY_PARAMETER_ARRAY;
  }

  public void registerConfigurableParameter(Parameter newParameter) {
  }

  public void registerFiringEventListener(FiringEventListener listener) {
  }

  public boolean removeFiringEventListener(FiringEventListener listener) {
    return false;
  }

  public boolean hasFiringEventListeners() {
    return false;
  }

  public void notifyFiringEventListeners(FiringEvent event) {
  }

  public void notifyActorStartedTask(Actor actor, Object task) {
  }

  public void notifyActorFinishedTask(Actor actor, Object task) throws IllegalArgumentException {
  }

  public void clearBusyTaskActors() {
  }

  public boolean hasBusyTaskActors() {
    return false;
  }
  
  public boolean isActorBusy(Actor actor) {
    return false;
  }

  public void notifyActorActive(Actor actor) {
  }

  public void notifyActorInactive(Actor actor) {
  }

  public boolean isActorActive(Actor actor) {
    return false;
  }

  public Set<Actor> getActiveActors() {
    return EMPTY_ACTIVE_ACTORS;
  }

  public void clearExecutionState() {
  }
}
