/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.process.impl.executor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RunnableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager.State;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Manager;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.EventListener;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.process.ProcessStatus;
import com.isencia.passerelle.runtime.process.impl.debug.ActorBreakpointListener;
import com.isencia.passerelle.runtime.process.impl.debug.PortBreakpointListener;
import com.isencia.passerelle.runtime.process.impl.event.StatusProcessEvent;
import com.isencia.passerelle.runtime.process.impl.event.TerminateEvent;

/**
 * @author erwin
 */
public class FlowExecutionTask implements CancellableTask<ProcessStatus>, ExecutionListener {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowExecutionTask.class);

  private final static Map<State, ProcessStatus> STATUS_MAPPING = new HashMap<Manager.State, ProcessStatus>();

  private final FlowHandle flowHandle;
  private final StartMode mode;
  private final Map<String, String> parameterOverrides;
  private final Set<String> breakpointNames;
  private final String processContextId;
  private volatile ProcessStatus status;
  private volatile boolean canceled;
  private volatile boolean busy;
  private volatile boolean suspended;
  private volatile Set<String> suspendedElements = new ConcurrentSkipListSet<String>();
  private Manager manager;
  private EventListener listener;

  public FlowExecutionTask(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, EventListener listener,
      String... breakpointNames) {
    this.mode = mode;
    if (flowHandle == null)
      throw new IllegalArgumentException("FlowHandle can not be null");
    this.flowHandle = flowHandle;
    this.processContextId = processContextId;
    status = ProcessStatus.IDLE;
    this.parameterOverrides = (parameterOverrides != null) ? new HashMap<String, String>(parameterOverrides) : null;
    this.breakpointNames = (breakpointNames != null) ? new HashSet<String>(Arrays.asList(breakpointNames)) : null;
    this.listener = listener;
  }

  public RunnableFuture<ProcessStatus> newFutureTask() {
    return new FlowExecutionFuture(this);
  }

  /**
   * @return the flow that is being executed by this task
   */
  public FlowHandle getFlowHandle() {
    return flowHandle;
  }

  /**
   * @return the process context ID for this execution
   */
  public String getProcessContextId() {
    return processContextId;
  }

  /**
   * Performs the real flow execution on the caller's thread.
   * 
   * @return the final status after the model execution has terminated
   */
  @Override
  public ProcessStatus call() throws Exception {
    LOGGER.trace("call() - Context {} - Flow {}", processContextId, flowHandle.getCode());
    try {
      boolean debug = false;
      synchronized (this) {
        Flow flow = (Flow) flowHandle.getFlow().clone(new Workspace());
        applyParameterSettings(flowHandle, flow, parameterOverrides);
        if (StartMode.DEBUG.equals(mode)) {
          debug = setBreakpoints(flowHandle, flow, breakpointNames);
        }
        manager = new Manager(flow.workspace(), processContextId);
        manager.addExecutionListener(this);
        flow.setManager(manager);
        busy = true;
      }
      if (!canceled) {
        if (!debug) {
          LOGGER.info("Context {} - Starting execution of flow {}", processContextId, flowHandle.getCode());
        } else {
          LOGGER.info("Context {} - Starting DEBUG execution of flow {}", processContextId, flowHandle.getCode());
        }
        manager.execute();
        // Just to be sure that for blocking executes,
        // we don't miss the final manager state changes before returning.
        managerStateChanged(manager);
      } else {
        LOGGER.info("Context {} - Canceled execution of flow {} before start", processContextId, flowHandle.getCode());
      }
    } catch (Exception e) {
      executionError(manager, e);
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flowHandle.toString(), e);
      }
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("call() exit - Context {} - Flow {} - Final Status {}", new Object[] { processContextId, flowHandle.getCode(), status });
    }
    return status;
  }

  /**
   * Cancel the flow execution in a clean way.
   */
  public synchronized void cancel() {
    if (!status.isFinalStatus()) {
      LOGGER.trace("cancel() - Context {} - Flow {}", processContextId, flowHandle.getCode());
      canceled = true;
      if (busy) {
        LOGGER.info("Context {} - Canceling execution of flow {}", processContextId, flowHandle.getCode());
        // to ensure that the status is directly returned as stopping,
        // even when the manager.finish() is done asynchronously,
        // we explicitly set the state already here.
        // TODO check if it's not better to override the finish method in our Manager
        // to set the state in there, same as for pause/resume...
        status = ProcessStatus.STOPPING;
        manager.stop();
      } else {
        LOGGER.info("Context {} - Canceling execution of flow {} before it started", processContextId, flowHandle.getCode());
        status = ProcessStatus.INTERRUPTED;
        manager = null;
      }
      if (listener != null) {
        listener.handle(new StatusProcessEvent(processContextId, status, null));
      }
    } else {
      LOGGER.trace("Context {} - Ignoring canceling execution of flow {} that is already done", processContextId, flowHandle.getCode());
    }
  }

  public synchronized boolean suspend() {
    if (!status.isFinalStatus()) {
      LOGGER.trace("cancel() - Context {} - Flow {}", processContextId, flowHandle.getCode());
      suspended = true;
      if (busy) {
        LOGGER.info("Context {} - Suspending execution of flow {}", processContextId, flowHandle.getCode());
        manager.pause();
      } else {
        LOGGER.info("Context {} - Suspending execution of flow {} before it started", processContextId, flowHandle.getCode());
        status = ProcessStatus.SUSPENDED;
      }
      return true;
    } else {
      LOGGER.debug("Context {} - IGNORE suspending execution of flow {}", processContextId, flowHandle.getCode());
      return false;
    }
  }

  public synchronized boolean resume() {
    if (busy && (Manager.PAUSED.equals(manager.getState()) || Manager.PAUSED_ON_BREAKPOINT.equals(manager.getState()))) {
      suspended = false;
      LOGGER.info("Context {} - Resuming execution of flow {}", processContextId, flowHandle.getCode());
      manager.resume();
      return true;
    } else {
      LOGGER.debug("Context {} - IGNORE resuming execution of flow {}", processContextId, flowHandle.getCode());
      return false;
    }
  }

  /**
   * @return the current flow execution status
   */
  public ProcessStatus getStatus() {
    return status;
  }

  public String[] getSuspendedElements() {
    return suspendedElements.toArray(new String[suspendedElements.size()]);
  };

  public boolean addSuspendedElement(String elementName) {
    return suspendedElements.add(elementName);
  }

  public boolean removeSuspendedElement(String elementName) {
    return suspendedElements.remove(elementName);
  }

  /**
   * Updates the flow execution status to <code>ProcessStatus.ERROR</code>
   */
  @Override
  public void executionError(ptolemy.actor.Manager manager, Throwable throwable) {
    LOGGER.warn("Context " + processContextId + " - Execution error of flow " + getFlowHandle().getCode(), throwable);
    status = ProcessStatus.ERROR;
    if (listener != null) {
      listener.handle(new StatusProcessEvent(processContextId, status, throwable));
    }
    busy = false;
  }

  /**
   * Updates the flow execution status to <code>ProcessStatus.FINISHED</code>, or <code>ProcessStatus.INTERRUPTED</code>
   * if the execution finished due to a cancel.
   */
  @Override
  public void executionFinished(ptolemy.actor.Manager manager) {
    if (status == null || !status.isFinalStatus()) {
      if (!canceled) {
        LOGGER.info("Context {} - Execution finished of flow {}", processContextId, getFlowHandle().getCode());
        status = ProcessStatus.FINISHED;
      } else {
        LOGGER.warn("Context {} - Execution interrupted of flow {}", processContextId, getFlowHandle().getCode());
        status = ProcessStatus.INTERRUPTED;
      }
      if (listener != null) {
        listener.handle(new StatusProcessEvent(processContextId, status, null));
      }
      busy = false;
    }
  }

  /**
   * Changes the flow execution status according to the new manager state.
   */
  @Override
  public void managerStateChanged(ptolemy.actor.Manager manager) {
    State state = manager.getState();
    LOGGER.trace("Context {} - Manager state change of flow {} : {}", new Object[] { processContextId, getFlowHandle().getCode(), state });
    if (status == null || !status.isFinalStatus()) {
      ProcessStatus oldStatus = status;
      status = STATUS_MAPPING.get(state);
      if (canceled && ProcessStatus.IDLE.equals(status)) {
        status = ProcessStatus.INTERRUPTED;
      }
      if (oldStatus != status) {
        LOGGER.info("Context {} - Execution state change of flow {} : {}", new Object[] { processContextId, getFlowHandle().getCode(), status });
        // This handles the case where a suspend() call was done right after the (asynch) start,
        // before the actual execution was effectively already started.
        // The behaviour
        if (suspended && ProcessStatus.ACTIVE.equals(status)) {
          LOGGER.info("Context {} - Suspended at startup for Flow {}", processContextId, flowHandle.getCode());
          manager.pause();
        }
      }
      if (status.isFinalStatus()) {
        busy = false;
      }
    }
  }

  protected void applyParameterSettings(FlowHandle flowHandle, Flow flow, Map<String, String> props) throws PasserelleException {
    if (props != null) {
      Iterator<Entry<String, String>> propsItr = props.entrySet().iterator();
      while (propsItr.hasNext()) {
        Entry<String, String> element = propsItr.next();
        String propName = element.getKey();
        String propValue = element.getValue();
        String[] nameParts = propName.split("[\\.]");

        // set model parameters
        if (nameParts.length == 1 && !flow.attributeList().isEmpty()) {
          try {
            final Parameter p = (Parameter) flow.getAttribute(nameParts[0], Parameter.class);
            setParameter(flowHandle, p, propName, propValue);
          } catch (final IllegalActionException e1) {
            throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
          }
        }
        // parts[parts.length-1] is the parameter name
        // all the parts[] before that are part of the nested Parameter name
        Entity e = flow;
        for (int j = 0; j < nameParts.length - 1; j++) {
          if (e instanceof CompositeActor) {
            Entity test = ((CompositeActor) e).getEntity(nameParts[j]);
            if (test == null) {
              try {
                // maybe it is a director
                ptolemy.actor.Director d = ((CompositeActor) e).getDirector();
                if (d != null) {
                  Parameter p = (Parameter) d.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                  setParameter(flowHandle, p, propName, propValue);
                }
              } catch (IllegalActionException e1) {
                throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
              }
            } else {
              e = ((CompositeActor) e).getEntity(nameParts[j]);
              if (e != null) {
                try {
                  Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                  setParameter(flowHandle, p, propName, propValue);
                } catch (IllegalActionException e1) {
                  throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, e1);
                }
              }
            }
          } else {
            break;
          }
        }
      }
    }
  }

  private void setParameter(FlowHandle flowHandle, final Parameter p, String propName, String propValue) throws PasserelleException {
    if (p != null) {
      p.setExpression(propValue);
      p.setPersistent(true);
      LOGGER.info("Context {} - Flow {} - Override parameter {} : {}", new Object[] { processContextId, flowHandle.getCode(), propName, propValue });
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Context {} - Flow {} - Unknown parameter, no override : {} ", new Object[] { processContextId, flowHandle.getCode(), propName });
    }
  }

  protected boolean setBreakpoints(FlowHandle flowHandle, Flow flow, Set<String> breakpointNames) {
    boolean breakpointsDefined = false;
    if (breakpointNames != null) {
      for (String breakpointName : breakpointNames) {
        ComponentEntity entity = flow.getEntity(breakpointName);
        if (entity != null) {
          entity.addDebugListener(new ActorBreakpointListener(breakpointName, this));
          LOGGER.info("Context {} - Flow {} - Set breakpoint {}", new Object[] { processContextId, flowHandle.getCode(), breakpointName });
          breakpointsDefined = true;
        } else {
          Port port = flow.getPort(breakpointName);
          if (port != null) {
            port.addDebugListener(new PortBreakpointListener(breakpointName, this));
            LOGGER.info("Context {} - Flow {} - Set breakpoint {}", new Object[] { processContextId, flowHandle.getCode(), breakpointName });
            breakpointsDefined = true;
          } else {
            LOGGER.warn("Context {} - Flow {} - Breakpoint not found ", new Object[] { processContextId, flowHandle.getCode(), breakpointName });
          }
        }
      }
    }
    return breakpointsDefined;
  }

  static {
    STATUS_MAPPING.put(Manager.IDLE, ProcessStatus.IDLE);
    STATUS_MAPPING.put(Manager.INITIALIZING, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.PREINITIALIZING, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.RESOLVING_TYPES, ProcessStatus.STARTING);
    STATUS_MAPPING.put(Manager.ITERATING, ProcessStatus.ACTIVE);
    STATUS_MAPPING.put(Manager.PAUSED, ProcessStatus.SUSPENDED);
    STATUS_MAPPING.put(Manager.PAUSED_ON_BREAKPOINT, ProcessStatus.SUSPENDED);
    STATUS_MAPPING.put(Manager.WRAPPING_UP, ProcessStatus.STOPPING);
    STATUS_MAPPING.put(Manager.EXITING, ProcessStatus.STOPPING);
    STATUS_MAPPING.put(Manager.CORRUPTED, ProcessStatus.ERROR);
    STATUS_MAPPING.put(Manager.THROWING_A_THROWABLE, ProcessStatus.ERROR);
  }
}
