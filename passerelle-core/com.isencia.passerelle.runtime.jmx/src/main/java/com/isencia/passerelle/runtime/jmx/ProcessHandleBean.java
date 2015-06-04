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
package com.isencia.passerelle.runtime.jmx;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessHandleBean {

  private String processContextId;
  private String status;
  private FlowHandleBean flow;
  private String[] suspendedElements;
  
  public final static ProcessHandleBean buildProcessHandleBean(ProcessHandle processHandle) {
    return new ProcessHandleBean(processHandle.getProcessId(), 
        processHandle.getExecutionStatus().name(), 
        FlowHandleBean.buildCompactFlowHandleBean(processHandle.getFlowHandle()), 
        processHandle.getSuspendedElements());
  }
  
  @ConstructorProperties({"processContextId","status","flow","suspendedElements"})
  public ProcessHandleBean(String processContextId, String status, FlowHandleBean flow, String[] suspendedElements) {
    this.processContextId = processContextId;
    this.status = status;
    this.flow = flow;
    this.suspendedElements = suspendedElements;
  }

  public String getProcessContextId() {
    return processContextId;
  }

  public String getStatus() {
    return status;
  }

  public FlowHandleBean getFlow() {
    return flow;
  }

  public String[] getSuspendedElements() {
    return suspendedElements;
  }

  @Override
  public String toString() {
    return "ProcessHandleBean [processContextId=" + processContextId + ", status=" + status + ", flow=" + flow + ", suspendedElements="
        + Arrays.toString(suspendedElements) + "]";
  }
}
