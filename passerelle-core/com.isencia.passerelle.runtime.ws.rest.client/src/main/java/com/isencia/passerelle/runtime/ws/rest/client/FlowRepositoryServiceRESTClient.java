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
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.repository.VersionSpecification;
import com.isencia.passerelle.runtime.ws.rest.CodeList;
import com.isencia.passerelle.runtime.ws.rest.ErrorInfo;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 * @author erwin
 */
public class FlowRepositoryServiceRESTClient implements FlowRepositoryService {

  private final static Logger LOGGER = LoggerFactory.getLogger(FlowRepositoryServiceRESTClient.class);

  private boolean configured = false;

  private Client restClient;
  private WebResource flowReposResource;

  public void init(Dictionary<String, String> configuration) {
    try {
      String debugStr = configuration.get("debug");
      String resourceURLStr = configuration.get("resourceURL");
      boolean debug = Boolean.parseBoolean(debugStr);
      restClient = Client.create();
      if (debug) {
        restClient.addFilter(new LoggingFilter());
      }
      flowReposResource = restClient.resource(resourceURLStr);
      configured = true;
    } catch (Exception e) {
      configured = false;
      LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR.getFormattedCode() + " - Error configuring REST client", e);
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  @Override
  public FlowHandle commit(Flow flow) throws DuplicateEntryException {
    return commit(flow.getName(), flow);
  }

  @Override
  public FlowHandle commit(String flowCode, Flow flow) throws DuplicateEntryException {
    try {
      return flowReposResource.path(flowCode).type(MediaType.APPLICATION_XML).post(FlowHandleResource.class, flow.exportMoML());
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new DuplicateEntryException(flowCode);
    }
  }

  @Override
  public FlowHandle[] delete(String flowCode) throws EntryNotFoundException {
    try {
      FlowHandleResources handleResources = flowReposResource.path(flowCode).delete(FlowHandleResources.class);
      return handleResources.getFlowHandles().toArray(new FlowHandle[0]);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(flowCode);
    }
  }

  @Override
  public FlowHandle update(FlowHandle handle, Flow updatedFlow, boolean activate) throws EntryNotFoundException {
    try {
      return flowReposResource.path(handle.getCode()).queryParam("activate", Boolean.toString(activate)).type(MediaType.APPLICATION_XML)
          .put(FlowHandleResource.class, updatedFlow.exportMoML());
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(handle.getCode());
    }
  }

  @Override
  public FlowHandle getActiveFlow(String flowCode) throws EntryNotFoundException {
    try {
      return flowReposResource.path(flowCode).get(FlowHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(flowCode);
    }
  }

  @Override
  public FlowHandle getMostRecentFlow(String flowCode) throws EntryNotFoundException {
    try {
      return flowReposResource.path(flowCode).path("mostRecent").get(FlowHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(flowCode);
    }
  }
  
  @Override
  public FlowHandle getFlowVersion(String flowCode, VersionSpecification version) throws EntryNotFoundException {
    try {
      return flowReposResource.path(flowCode).path(version.toString()).get(FlowHandleResource.class);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(flowCode);
    }
  }
  
  @Override
  public FlowHandle loadFlowHandleWithContent(FlowHandle handle) throws EntryNotFoundException {
    return getFlowVersion(handle.getCode(), handle.getVersion());
  }

  @Override
  public String[] getAllFlowCodes() {
    CodeList codeList = flowReposResource.get(CodeList.class);
    if (codeList != null && codeList.getCodes() != null) {
      return codeList.getCodes().toArray(new String[0]);
    } else {
      return new String[0];
    }
  }

  @Override
  public FlowHandle[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException {
    try {
      FlowHandleResources handleResources = flowReposResource.path(flowCode).get(FlowHandleResources.class);
      return handleResources.getFlowHandles().toArray(new FlowHandle[0]);
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(flowCode);
    }
  }

  @Override
  public FlowHandle activateFlowRevision(FlowHandle handle) throws EntryNotFoundException {
    try {
      return flowReposResource.path(handle.getCode()).path("activate").post(FlowHandleResource.class, FlowHandleResource.buildFlowHandleResource(handle));
    } catch (UniformInterfaceException e) {
      LOGGER.error("REST call exception", e);
      ErrorInfo errorInfo = e.getResponse().getEntity(ErrorInfo.class);
      LOGGER.error(errorInfo.toString());
      throw new EntryNotFoundException(handle.getCode());
    }
  }

}
