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
package com.isencia.passerelle.runtime;

import java.io.Serializable;

import com.isencia.passerelle.runtime.process.ProcessStatus;



/**
 * A light-weight handle on a Flow-based process execution.
 * <p>
 * </p>
 * 
 * @author erwin
 *
 */
public interface ProcessHandle extends Serializable {
  
  /**
   * 
   * @return the flowHandle of the flow that is running the process
   */
  FlowHandle getFlowHandle();
  
  /**
   * For context-aware executions, this can be used to retrieve 
   * the <code>ProcessManager</code> from the <code>ProcessManagerService</code> if needed.
   * <b>Remark that such retrieval can be a heavy operation and should only be attempted when really necessary.</b> 
   * <br/>
   * For the rare process executions without assigned <code>Context</code>s, this returns an id that can be used to
   * uniquely identify the execution in any related actions, e.g. to obtain execution logs, pause/resume it etc.
   * 
   * @return the UUID of the process execution;
   * 
   */
  String getProcessId();
  
  /**
   * 
   * @return the current execution status
   */
  ProcessStatus getExecutionStatus();
  
  /**
   * Suspensions can  be caused by breakpoints and/or the end of a step execution.
   * @return the names of the currently suspended Flow elements (typically actors)
   */
  String[] getSuspendedElements();
}
