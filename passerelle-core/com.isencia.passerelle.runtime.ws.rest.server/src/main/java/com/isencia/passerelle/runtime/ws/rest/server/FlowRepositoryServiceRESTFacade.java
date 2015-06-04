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

import java.io.StringReader;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.repository.VersionSpecification;
import com.isencia.passerelle.runtime.ws.rest.CodeList;
import com.isencia.passerelle.runtime.ws.rest.ErrorCode;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResources;
import com.isencia.passerelle.runtime.ws.rest.InvalidRequestException;
import com.isencia.passerelle.runtime.ws.rest.server.activator.Activator;

/**
 * A REST service provider (or root resource) mapped on the FlowRepositoryService interface.
 * <p>
 * The methods map functionally to the similarly-named methods in the FlowRepositoryService interface,
 * but parameter and return types are adapted for REST-based communication.
 * </p>
 * 
 * @author erwin
 */
@Path("flows")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FlowRepositoryServiceRESTFacade {

  @Context
  UriInfo uriInfo;

  @GET
  public CodeList getAllFlowCodes() {
    return new CodeList(getFlowRepositoryService().getAllFlowCodes());
  }

  @GET
  @Path("{code}")
  public FlowHandle getActiveFlow(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      FlowHandle localHandle = getFlowRepositoryService().getActiveFlow(flowCode);
      return buildRemoteHandle(localHandle, false);
    }
  }

  @GET
  @Path("{code}/{version}")
  public FlowHandle getFlowVersion(@PathParam("code") String flowCode, @PathParam("version") String version) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else if (version == null) {
      return getActiveFlow(flowCode);
    } else {
      VersionSpecification versionSpec = null;
      try {
        versionSpec = VersionSpecification.parse(version);
      } catch (Exception e) {
        throw new InvalidRequestException(ErrorCode.INVALID_PARAM, "version");
      }
      FlowHandle localHandle = null;
      if(versionSpec!=null) {
        localHandle = getFlowRepositoryService().getFlowVersion(flowCode, versionSpec);
      } else {
        localHandle = getFlowRepositoryService().getActiveFlow(flowCode);
      }
      return buildRemoteHandle(localHandle, true);
    }
  }

  @GET
  @Path("{code}/mostRecent")
  public FlowHandle getMostRecentFlow(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      FlowHandle localHandle = getFlowRepositoryService().getMostRecentFlow(flowCode);
      return buildRemoteHandle(localHandle, true);
    }
  }

  @GET
  @Path("{code}/all")
  public FlowHandleResources getAllFlowRevisions(@PathParam("code") String flowCode) throws EntryNotFoundException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      return new FlowHandleResources(uriInfo.getBaseUriBuilder().path(FlowRepositoryServiceRESTFacade.class), getFlowRepositoryService().getAllFlowRevisions(flowCode));
    }
  }

  @POST
  @Path("{code}/activate")
  public FlowHandle activateFlowRevision(FlowHandleResource handle) throws EntryNotFoundException, InvalidRequestException {
    if (handle == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } else {
      FlowHandle localHandle = getFlowRepositoryService().activateFlowRevision(handle);
      return buildRemoteHandle(localHandle, false);
    }
  }

  @POST
  @Path("{code}")
  @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
  public FlowHandle commit(@PathParam("code") String flowCode, String rawFlowDefinition) throws DuplicateEntryException, InvalidRequestException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else if (rawFlowDefinition == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } else {
      Flow flow = null;
      try {
        flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
      } catch (Exception e) {
        throw new InvalidRequestException(ErrorCode.ERROR, "");
      }
      FlowHandle localHandle = getFlowRepositoryService().commit(flowCode, flow);
      return buildRemoteHandle(localHandle, false);
    }
  }

  @DELETE
  @Path("{code}")
  public FlowHandleResources delete(@PathParam("code") String flowCode) throws InvalidRequestException, EntryNotFoundException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else {
      return new FlowHandleResources(uriInfo.getBaseUriBuilder().path(FlowRepositoryServiceRESTFacade.class), getFlowRepositoryService().delete(flowCode));
    }
  }

  @PUT
  @Path("{code}")
  @Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML })
  public FlowHandle update(@PathParam("code") String flowCode, String rawFlowDefinition, @QueryParam("activate") boolean activate) throws InvalidRequestException, EntryNotFoundException {
    if (flowCode == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_PARAM, "code");
    } else if (rawFlowDefinition == null) {
      throw new InvalidRequestException(ErrorCode.MISSING_CONTENT, "flow definition");
    } else {
      FlowHandle handle = getFlowRepositoryService().getActiveFlow(flowCode);
      Flow updatedFlow = null;
      try {
        updatedFlow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
      } catch (Exception e) {
        throw new InvalidRequestException(ErrorCode.ERROR, "");
      }
      FlowHandle localHandle = getFlowRepositoryService().update(handle, updatedFlow, activate);
      return buildRemoteHandle(localHandle, false);
    }
  }

  private FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }

  private FlowHandle buildRemoteHandle(FlowHandle localHandle, boolean specifyVersionInURI) {
    if (uriInfo != null) {
      UriBuilder uriBldr = uriInfo.getBaseUriBuilder().path(FlowRepositoryServiceRESTFacade.class).path("{code}");
      URI resLoc = null;
//      if(specifyVersionInURI) {
//        resLoc = uriBldr.path("{version}").build(localHandle.getCode(), localHandle.getVersion());
//      } else {
        resLoc = uriBldr.build(localHandle.getCode());
//      }
      return new FlowHandleResource(resLoc, localHandle.getCode(), localHandle.getRawFlowDefinition(), localHandle.getVersion());
    } else {
      return FlowHandleResource.buildFlowHandleResource(localHandle);
    }
  }
}
