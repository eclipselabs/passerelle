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
package com.isencia.passerelle.runtime.ws.rest.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.ErrorCode;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.InvalidRequestException;
import com.isencia.passerelle.runtime.ws.rest.ProcessHandleResource;
import com.isencia.passerelle.runtime.ws.rest.server.activator.Activator;

/**
 * A REST service provider (or root resource) mapped on the FlowProcessingService interface.
 * <p>
 * </p>
 * 
 * @author erwin
 */
@Path("processes")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FlowProcessingServiceRESTFacade {
  private static final String PROCESS_CONTEXT_ID = "___processContextId";

  private static final String BREAKPOINTS = "___breakpoints";

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowProcessingServiceRESTFacade.class);

  @Context
  UriInfo uriInfo;

  /**
   * 
   * @param mode
   * @param flowHandle
   * @param processContextId
   * @param breakPointStr comma-separated concatenated string of actor/port names for breakpoints
   * @return
   * @throws EntryNotFoundException
   * @throws InvalidRequestException
   */
  @POST
  @Path("{mode}")
  public ProcessHandle start(@PathParam("mode") String mode, FlowHandleResource flowHandle, 
      @QueryParam(PROCESS_CONTEXT_ID) String processContextId, @QueryParam(BREAKPOINTS) String breakPointStr) throws EntryNotFoundException, InvalidRequestException {
    if (flowHandle == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } if (mode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "mode");
    } else {
      if(LOGGER.isInfoEnabled()) {
        LOGGER.info("Context {} - Submitting {} request for flow {}", new Object[]{processContextId, mode, flowHandle.getCode()});
      }
      try {
        // (re)load the flowhandle contents based on code and version
        // this allows to send compact handles around, without the complete raw flow definition
        // and we only load the content (which could be a large MOML/XML) at the moment it's really needed.
        FlowHandle handle = getFlowRepositoryService().loadFlowHandleWithContent(flowHandle);
        StartMode _mode = StartMode.valueOf(mode);
        String[] breakpointNames = breakPointStr!=null ? breakPointStr.split(",") : null;
        
        Map<String, String> parameterOverrides = new HashMap<String, String>();
        MultivaluedMap<String,String> queryParameters = uriInfo.getQueryParameters();
        for(Entry<String,List<String>> qP : queryParameters.entrySet()) {
          String paramName = qP.getKey();
          List<String> paramValueList = qP.getValue();
          String paramValue = ((paramValueList!=null) && (paramValueList.size()>0)) ? paramValueList.get(0) : null;
          if(!PROCESS_CONTEXT_ID.equals(paramName)
              && !BREAKPOINTS.equals(paramName)
              && paramValue!=null) {
            parameterOverrides.put(paramName, paramValue);
          }
        }
        ProcessHandle localHandle = getFlowProcessingService().start(_mode, handle, processContextId, parameterOverrides, null, breakpointNames);
        return buildRemoteHandle(localHandle);
      } catch (Exception e) {
        throw new InvalidRequestException(ErrorCode.INVALID_PARAM, "mode");
      }
    }
  }

  @GET
  @Path("{processContextId}")
  public ProcessHandle getHandle(@PathParam("processContextId") String processContextId) throws FlowNotExecutingException, InvalidRequestException {
    if(processContextId==null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "processContextId");
    } else {
      LOGGER.info("Context {} - Getting execution handle", processContextId);
      ProcessHandle localHandle = getFlowProcessingService().getHandle(processContextId);
      if(localHandle!=null) {
        return buildRemoteHandle(localHandle);
      } else {
        return null;
      }
    }
  }

  @DELETE
  @Path("{processContextId}")
  public ProcessHandle terminate(@PathParam("processContextId") String processContextId) throws FlowNotExecutingException, InvalidRequestException {
    if(processContextId==null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "processContextId");
    } else {
      ProcessHandle localHandle = getFlowProcessingService().getHandle(processContextId);
      if(localHandle!=null) {
        LOGGER.info("Context {} - Terminating execution of flow {}", localHandle.getProcessId(), localHandle.getFlowHandle().getCode());
        localHandle = getFlowProcessingService().terminate(localHandle);
        return buildRemoteHandle(localHandle);
      } else {
        throw new FlowNotExecutingException(processContextId);
      }
    }
  }

  @POST
  @Path("{processContextId}/suspend")
  public ProcessHandle suspend(@PathParam("processContextId") String processContextId) throws FlowNotExecutingException, InvalidRequestException {
    if(processContextId==null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "processContextId");
    } else {
      ProcessHandle localHandle = getFlowProcessingService().getHandle(processContextId);
      if(localHandle!=null) {
        LOGGER.info("Context {} - Suspending execution of flow {}", localHandle.getProcessId(), localHandle.getFlowHandle().getCode());
        localHandle = getFlowProcessingService().suspend(localHandle);
        return buildRemoteHandle(localHandle);
      } else {
        throw new FlowNotExecutingException(processContextId);
      }
    }
  }
  
  @POST
  @Path("{processContextId}/resume")
  public ProcessHandle resume(@PathParam("processContextId") String processContextId) throws FlowNotExecutingException, InvalidRequestException {
    if(processContextId==null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "processContextId");
    } else {
      ProcessHandle localHandle = getFlowProcessingService().getHandle(processContextId);
      if(localHandle!=null) {
        LOGGER.info("Context {} - Resuming execution of flow {}", localHandle.getProcessId(), localHandle.getFlowHandle().getCode());
        localHandle = getFlowProcessingService().resume(localHandle);
        return buildRemoteHandle(localHandle);
      } else {
        throw new FlowNotExecutingException(processContextId);
      }
    }
  }
  
  private ProcessHandle buildRemoteHandle(ProcessHandle localHandle) {
    return new ProcessHandleResource(localHandle);
  }
  private FlowProcessingService getFlowProcessingService() {
    return Activator.getInstance().getFlowProcessingSvc();
  }
  private FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }
}
