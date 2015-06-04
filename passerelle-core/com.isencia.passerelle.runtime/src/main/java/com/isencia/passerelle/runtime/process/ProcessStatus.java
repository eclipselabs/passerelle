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
package com.isencia.passerelle.runtime.process;

/**
 * @author erwin
 */
public enum ProcessStatus {

  IDLE, // when the flow is not executing, not even starting to execute
  STARTING, // when the execution has been requested, and the flow is going through it's initialization phases
  ACTIVE, // when the flow is really doing work
  SUSPENDED, // when the flow is suspended; either via a global suspend action, a breakpoint, after finishing a step when running in stepping mode etc
  STOPPING, // when the model has done its work and is going through its wrapup phases
  FINISHED, // when the execution has been completed without technical/runtime errors
  INTERRUPTED, // when the execution was interrupted/aborted before its normal completion, typically by a user action
  ERROR; // when the execution has encountered a technical/runtime error, e.g. flow parsing errors or other dramatic technical errors. 
         // Functional errors for specific actors/tasks do not impact the ExecutionStatus.

  public boolean isFinalStatus() {
    return this.compareTo(FINISHED)>=0;
  }
}
