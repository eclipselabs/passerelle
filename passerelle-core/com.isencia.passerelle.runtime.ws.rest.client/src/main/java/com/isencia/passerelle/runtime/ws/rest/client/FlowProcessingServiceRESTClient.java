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
package com.isencia.passerelle.runtime.ws.rest.client;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.runtime.EventListener;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.ws.rest.ErrorInfo;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.ProcessHandleResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * @author erwin
 */
public class FlowProcessingServiceRESTClient implements FlowProcessingService {

  private static final String BREAKPOINTS = "___breakpoints";

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowProcessingServiceRESTClient.class);

  private boolean configured = false;

  private Client restClient;
  private WebResource flowProcResource;
  
  private ScheduledExecutorService pollingService = Executors.newSingleThreadScheduledExecutor();

  public void init(Dictionary<String, String> configuration) {
    try {
      String debugStr = configuration.get("debug");
      String resourceURLStr = configuration.get("resourceURL");
      boolean debug = Boolean.parseBoolean(debugStr);
      restClient = Client.create();
      if (debug) {
        restClient.addFilter(new LoggingFilter());
      }
      flowProcResource = restClient.resource(resourceURLStr);
      configured = true;
    } catch (Exception e) {
      configured = false;
      LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR.getFormattedCode() + " - Error configuring REST client", e);
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  // TODO finish this : handle all arguments ; better error handling
  @Override
  public ProcessHandle start(StartMode mode, FlowHandle flowHandle, String processContextId, Map<String, String> parameterOverrides, EventListener listener,
      String... breakpointNames) {
    LOGGER.info("Context {} - Submitting start request for flow {}", processContextId, flowHandle.getCode());
    try {
      WebResource pathPart = flowProcResource.path(mode.name());
      if(parameterOverrides!=null && !parameterOverrides.isEmpty()) {
        for (Entry<String, String> parameterOverride : parameterOverrides.entrySet()) {
          pathPart = pathPart.queryParam(parameterOverride.getKey(), parameterOverride.getValue());
        }
      }
      if (StartMode.DEBUG.equals(mode) && breakpointNames!=null) {
        StringBuilder breakPointStr = new StringBuilder();
        for (String breakPoint : breakpointNames) {
          if(breakPointStr.length()>0) {
            breakPointStr.append(",");
          }
          breakPointStr.append(breakPoint);
        }
        return pathPart.queryParam(BREAKPOINTS, breakPointStr.toString()).type(MediaType.APPLICATION_XML).post(ProcessHandleResource.class, FlowHandleResource.buildFlowHandleResource(flowHandle));
      } else {
        return pathPart.type(MediaType.APPLICATION_XML).post(ProcessHandleResource.class, FlowHandleResource.buildFlowHandleResource(flowHandle));
      }
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new IllegalArgumentException();
    }
  }
  
  @Override
  public ProcessHandle waitUntilFinished(final ProcessHandle processHandle, final long time, final TimeUnit unit) throws FlowNotExecutingException, TimeoutException, InterruptedException, ExecutionException {
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("Context {} - waitUntilFinished {} {}", new Object[]{processHandle.getProcessId(), time, unit});
    }
    ProcessHandle ph = refresh(processHandle);
    if(ph.getExecutionStatus().isFinalStatus()) {
      return ph;
    } else if(time>0) {
      // stupid polling system as a first basic implementation
      final long pollingDelayTime = time/2 + 1;
      final long nextWaitTime = time - pollingDelayTime;
      ScheduledFuture<ProcessHandle> schedule = pollingService.schedule(new Callable<ProcessHandle>() {
        @Override
        public ProcessHandle call() throws Exception {
          return waitUntilFinished(processHandle, nextWaitTime , unit);
        }
      }, pollingDelayTime, unit);
      // Add an extra wait unit to avoid issues for calls where time==1 
      // (as pollingDelayTime also becomes 1 then, and then timeouts become very probable on the schedule.get())
      try {
        return schedule.get(time+1, unit);
      } catch (ExecutionException e) {
        if(e.getCause() instanceof FlowNotExecutingException) {
          throw (FlowNotExecutingException) e.getCause();
        } else if(e.getCause() instanceof InterruptedException) {
          throw (InterruptedException) e.getCause();
        } else if(e.getCause() instanceof TimeoutException) {
          throw (TimeoutException) e.getCause();
        } else if(e.getCause() instanceof ExecutionException) {
          throw (ExecutionException) e.getCause();
        } else {
          throw e;
        }
      }
    } else {
        throw new TimeoutException("Timeout waitUntilFinished "+processHandle.getProcessId());
    }
  }
  
  // TODO finish this : better error handling
  @Override
  public ProcessHandle getHandle(String processId) {
    try {
      return flowProcResource.path(processId).type(MediaType.APPLICATION_XML).get(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new IllegalArgumentException();
    }
  }
  
  @Override
  public ProcessHandle refresh(ProcessHandle processHandle) {
    LOGGER.info("Context {} - Refreshing execution status of flow {}", processHandle.getProcessId(), processHandle.getFlowHandle().getCode());
    ProcessHandle ph = getHandle(processHandle.getProcessId());
    LOGGER.debug("Context {} - Execution status : {}", ph.getProcessId(), ph.getExecutionStatus());
    return ph;
  }

  @Override
  public ProcessHandle terminate(ProcessHandle processHandle) throws FlowNotExecutingException {
    LOGGER.info("Context {} - Terminating execution of flow {}", processHandle.getProcessId(), processHandle.getFlowHandle().getCode());
    try {
      return flowProcResource.path(processHandle.getProcessId()).delete(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessId());
    }
  }

  @Override
  public ProcessHandle suspend(ProcessHandle processHandle) throws FlowNotExecutingException {
    LOGGER.info("Context {} - Suspending execution of flow {}", processHandle.getProcessId(), processHandle.getFlowHandle().getCode());
    try {
      return flowProcResource.path(processHandle.getProcessId()).path("suspend").post(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessId());
    }
  };
  
  @Override
  public ProcessHandle resume(ProcessHandle processHandle) throws FlowNotExecutingException {
    LOGGER.info("Context {} - Resuming execution of flow {}", processHandle.getProcessId(), processHandle.getFlowHandle().getCode());
    try {
      return flowProcResource.path(processHandle.getProcessId()).path("resume").post(ProcessHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new FlowNotExecutingException(processHandle.getProcessId());
    }
  }

  // TODO implement local resume, once this is supported
  @Override
  public ProcessHandle resume(ProcessHandle processHandle, String suspendedElement) throws FlowNotExecutingException {
    return resume(processHandle);
  }

  @Override
  public ProcessHandle step(ProcessHandle processHandle) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle addBreakpoints(ProcessHandle processHandle, String... extraBreakpoints) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle removeBreakpoints(ProcessHandle processHandle, String... breakpointsToRemove) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcessHandle signalEvent(ProcessHandle processHandle, Event event) throws FlowNotExecutingException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(ProcessHandle processHandle, int maxCount) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event> getProcessEvents(String processId, int maxCount) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  };
}
