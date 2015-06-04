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

import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;

/**
 * MXbean interface for the JMX server-side facade for a FlowRepositoryService.
 * <p>
 * The methods map functionally to the similarly-named methods in the FlowRepositoryService interface,
 * but parameter and return types are adapted for JMX-based remote monitoring and management.
 * </p>
 * @author erwin
 *
 */
public interface FlowRepositoryMXBean {
  
  String[] getAllFlowCodes();
  
  FlowHandleBean getActiveFlow(String flowCode) throws EntryNotFoundException;

  FlowHandleBean getFlowVersion(String flowCode, String version) throws EntryNotFoundException;
  
  FlowHandleBean getMostRecentFlow(String flowCode) throws EntryNotFoundException;

  FlowHandleBean[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException;
  
  FlowHandleBean activateFlowRevision(FlowHandleBean handle) throws EntryNotFoundException;
  
  FlowHandleBean commit(String flowCode, String rawFlowDefinition) throws DuplicateEntryException;

  FlowHandleBean[] delete(String flowCode) throws EntryNotFoundException;

  FlowHandleBean update(String flowCode, String rawFlowDefinition, boolean activate) throws EntryNotFoundException;

}
