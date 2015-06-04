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
package com.isencia.passerelle.runtime.ws.rest;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.ProcessStatus;

@XmlRootElement(name="ProcessHandle")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessHandleResource implements ProcessHandle {
  
  private String processContextId;
  private ProcessStatus status;
  private FlowHandleResource flow;
  private String[] suspendedElements;

  public ProcessHandleResource() {
  }
  
  public ProcessHandleResource(ProcessHandle handle) {
    this(handle.getProcessId(), handle.getExecutionStatus(), handle.getSuspendedElements(), FlowHandleResource.buildCompactFlowHandleResource(handle.getFlowHandle()));
  }
  
  public ProcessHandleResource(String processContextId, ProcessStatus status, String[] suspendedElements, FlowHandleResource flow) {
    this.processContextId = processContextId;
    this.status = status;
    this.suspendedElements = suspendedElements;
    this.flow = flow;
  }

  @Override
  public FlowHandle getFlowHandle() {
    return flow;
  }

  @Override
  public String getProcessId() {
    return processContextId;
  }

  @Override
  public ProcessStatus getExecutionStatus() {
    return status;
  }

  @Override
  public String[] getSuspendedElements() {
    return suspendedElements;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((flow == null) ? 0 : flow.hashCode());
    result = prime * result + ((processContextId == null) ? 0 : processContextId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + Arrays.hashCode(suspendedElements);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProcessHandleResource other = (ProcessHandleResource) obj;
    if (flow == null) {
      if (other.flow != null)
        return false;
    } else if (!flow.equals(other.flow))
      return false;
    if (processContextId == null) {
      if (other.processContextId != null)
        return false;
    } else if (!processContextId.equals(other.processContextId))
      return false;
    if (status != other.status)
      return false;
    if (!Arrays.equals(suspendedElements, other.suspendedElements))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ProcessHandleResource [processContextId=" + processContextId + ", status=" + status + ", suspendedElements=" + Arrays.toString(suspendedElements)
        + ", flow=" + flow.getResourceLocation() + "]";
  }
  
  
}
