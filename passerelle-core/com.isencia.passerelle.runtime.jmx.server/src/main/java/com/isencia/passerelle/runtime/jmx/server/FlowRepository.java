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
package com.isencia.passerelle.runtime.jmx.server;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.jmx.ErrorCode;
import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.jmx.server.activator.Activator;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

/**
 * 
 * @author erwin
 *
 */
public class FlowRepository implements FlowRepositoryMXBean {

  private FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }

  @Override
  public String[] getAllFlowCodes() {
    return getFlowRepositoryService().getAllFlowCodes();
  }

  @Override
  public FlowHandleBean getActiveFlow(String flowCode) throws EntryNotFoundException {
    FlowHandle handle = getFlowRepositoryService().getActiveFlow(flowCode);
    return FlowHandleBean.buildCompactFlowHandleBean(handle);
  }

  @Override
  public FlowHandleBean getFlowVersion(String flowCode, String version) throws EntryNotFoundException {
    VersionSpecification versionSpec = null;
    try {
      versionSpec = VersionSpecification.parse(version);
    } catch (Exception e) {
      throw new EntryNotFoundException(ErrorCode.INVALID_PARAM, "version", null);
    }
    FlowHandle localHandle = null;
    if (versionSpec != null) {
      localHandle = getFlowRepositoryService().getFlowVersion(flowCode, versionSpec);
    } else {
      localHandle = getFlowRepositoryService().getActiveFlow(flowCode);
    }
    return FlowHandleBean.buildFlowHandleBean(localHandle);
  }

  @Override
  public FlowHandleBean getMostRecentFlow(String flowCode) throws EntryNotFoundException {
    FlowHandle handle = getFlowRepositoryService().getMostRecentFlow(flowCode);
    return FlowHandleBean.buildCompactFlowHandleBean(handle);
  }

  @Override
  public FlowHandleBean[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException {
    FlowHandle[] allFlowRevisions = getFlowRepositoryService().getAllFlowRevisions(flowCode);
    return convertToFlowHandleBeans(allFlowRevisions);
  }

  private FlowHandleBean[] convertToFlowHandleBeans(FlowHandle[] allFlowRevisions) {
    Collection<FlowHandleBean> allFHBs = new ArrayList<FlowHandleBean>();
    for (FlowHandle flowHandle : allFlowRevisions) {
      allFHBs.add(FlowHandleBean.buildCompactFlowHandleBean(flowHandle));
    }
    return allFHBs.toArray(new FlowHandleBean[allFHBs.size()]);
  }

  @Override
  public FlowHandleBean activateFlowRevision(FlowHandleBean handle) throws EntryNotFoundException {
    try {
      FlowHandle localHandle = getFlowRepositoryService().activateFlowRevision(FlowHandleImpl.buildFlowHandle(handle));
      return FlowHandleBean.buildCompactFlowHandleBean(localHandle);
    } catch (URISyntaxException e) {
      throw new EntryNotFoundException(ErrorCode.INVALID_PARAM, "Handle has invalid resource location", e);
    }
  }

  @Override
  public FlowHandleBean commit(String flowCode, String rawFlowDefinition) throws DuplicateEntryException {
    Flow flow = null;
    try {
      flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
    } catch (Exception e) {
      e.printStackTrace();
    }
    FlowHandle localHandle = getFlowRepositoryService().commit(flowCode, flow);
    return FlowHandleBean.buildCompactFlowHandleBean(localHandle);
  }

  @Override
  public FlowHandleBean[] delete(String flowCode) throws EntryNotFoundException {
    FlowHandle[] allFlowRevisions = getFlowRepositoryService().delete(flowCode);
    return convertToFlowHandleBeans(allFlowRevisions);
  }

  @Override
  public FlowHandleBean update(String flowCode, String rawFlowDefinition, boolean activate) throws EntryNotFoundException {
    FlowHandle handle = getFlowRepositoryService().getActiveFlow(flowCode);
    Flow updatedFlow = null;
    try {
      updatedFlow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
    } catch (Exception e) {
      // TODO : add JMX-supported exception handling
      e.printStackTrace();
    }
    FlowHandle localHandle = getFlowRepositoryService().update(handle, updatedFlow, activate);
    return FlowHandleBean.buildFlowHandleBean(localHandle);
  }
}
