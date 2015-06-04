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
package com.isencia.passerelle.runtime.process.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.runtime.EventListener;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionFuture;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionTask;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutor;

public class FlowProcessingServiceImpl implements FlowProcessingService {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowProcessingServiceImpl.class);

  // the thread pool to launch flow execution tasks
  private ExecutorService flowExecutor;

  // TODO find some method to determine when an entry can be removed here...
  // client code may still like to obtain execution info a while after the execution has already finished
  // so when is a good moment for removal???
  private Map<String, FlowExecutionFuture> flowExecutions = new ConcurrentHashMap<String, FlowExecutionFuture>();
  
  // The set of listeners that are potentially interested in all ProcessEvents.
  // Remark that in an OSGi app, which is the preferred runtime platform, we expect to have one such listener
  // that will be an adapter towards the OSGi EventAdmin service.
  // But in non-OSGi-apps, all listeners may be registered directly here.
  // This is a slowly changing collection, normally only modified at application start-up/shutdown,
  // when such general listers will register/unregister themselves (or via OSGi DS injection etc)
  private Set<EventListener> generalEventListeners = Collections.synchronizedSet(new HashSet<EventListener>());
  
  /**
   * 
   * @param maxConcurrentProcesses
   */
  public FlowProcessingServiceImpl(int maxConcurrentProcesses) {
    LOGGER.info("Creating FlowProcessingService for {} max concurrent processes", maxConcurrentProcesses);
    flowExecutor = new FlowExecutor(maxConcurrentProcesses, maxConcurrentProcesses, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  @Override
  public ProcessHandle start(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, 
      EventListener listener, String... breakpointNames) {
    
    if (processContextId == null || processContextId.trim().length()==0) {
      processContextId = UUID.randomUUID().toString();
    }

    LOGGER.debug("Context {} - Submitting execution of flow {}", processContextId, flowHandle.getCode());

    FlowExecutionTask fet = new FlowExecutionTask(mode, flowHandle, processContextId, parameterOverrides, listener, breakpointNames);
    FlowExecutionFuture fetFuture = (FlowExecutionFuture) flowExecutor.submit(fet);
    ProcessHandle procHandle = new ProcessHandleImpl(fetFuture);

    if(!procHandle.getExecutionStatus().isFinalStatus()) {
      flowExecutions.put(processContextId, fetFuture);
    }

    return procHandle;
  }

  @Override
  public ProcessHandle addBreakpoints(ProcessHandle processHandle, String... extraBreakpoints) {
    // TODO implement signalEvent()
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle removeBreakpoints(ProcessHandle processHandle, String... breakpointsToRemove) {
    // TODO implement removeBreakpoints()
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle getHandle(String processId) {
    FlowExecutionFuture fet = flowExecutions.get(processId);
    return fet!=null ? new ProcessHandleImpl(fet) : null;
  }

  @Override
  public ProcessHandle refresh(ProcessHandle processHandle) {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessId());
    return fet!=null ? new ProcessHandleImpl(fet) : processHandle;
  }

  @Override
  public ProcessHandle waitUntilFinished(ProcessHandle processHandle, long time, TimeUnit unit) throws TimeoutException, InterruptedException, FlowNotExecutingException, ExecutionException {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessId());
    if (fet != null) {
      try {
        fet.get(time, unit);
      } catch (CancellationException e) {
        // ignore, it will be reflected in the status of the handle
      }
      return new ProcessHandleImpl(fet);
    } else {
      throw new FlowNotExecutingException(processHandle.getFlowHandle().getCode());
    }
  }

  /**
   * Does not wait for the execution to have terminated!
   */
  @Override
  public ProcessHandle terminate(ProcessHandle processHandle) throws FlowNotExecutingException {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessId());
    if(fet==null) {
      throw new FlowNotExecutingException(processHandle.getFlowHandle().getCode());
    } else {
      fet.cancel(true);
      return new ProcessHandleImpl(fet);
    }
  }

  @Override
  public ProcessHandle suspend(ProcessHandle processHandle) throws FlowNotExecutingException {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessId());
    if(fet==null) {
      throw new FlowNotExecutingException(processHandle.getFlowHandle().getCode());
    } else {
      // TODO check if we can/need to do something with the boolean result...
      fet.suspend();
      return new ProcessHandleImpl(fet);
    }
  }

  @Override
  public ProcessHandle resume(ProcessHandle processHandle) throws FlowNotExecutingException {
    FlowExecutionFuture fet = flowExecutions.get(processHandle.getProcessId());
    if(fet==null) {
      throw new FlowNotExecutingException(processHandle.getFlowHandle().getCode());
    } else {
      // TODO check if we can/need to do something with the boolean result...
      fet.resume();
      return new ProcessHandleImpl(fet);
    }
  }
  
  /**
   * TODO : implement partial suspensions (cfr doc for <code>start()</code> method).
   * 
   * Until then this method just delegates to the plain <code>resume(processHandle)</code>
   */
  @Override
  public ProcessHandle resume(ProcessHandle processHandle, String suspendedElement) throws FlowNotExecutingException {
    return resume(processHandle);
  }

  @Override
  public ProcessHandle step(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandle signalEvent(ProcessHandle processHandle, Event event) throws FlowNotExecutingException {
    // TODO implement signalEvent()
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(ProcessHandle processHandle, int maxCount) {
    // TODO implement getProcessEvents()
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(String processId, int maxCount) {
    // TODO implement getProcessEvents()
    throw new UnsupportedOperationException();
  }
}
