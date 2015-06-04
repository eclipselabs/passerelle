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

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

/**
 * Exception thrown when someone tries to stop the execution of a given Flow while it is not executing.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class FlowNotExecutingException extends PasserelleException {

  /**
   * @param flow
   */
  public FlowNotExecutingException(NamedObj flow) {
    super(ErrorCode.FLOW_STATE_ERROR, "Flow not executing", flow, null);
  }
  /**
   * @param flowName
   */
  public FlowNotExecutingException(String flowName) {
    super(ErrorCode.FLOW_STATE_ERROR, "Flow not executing " + flowName, null);
  }
  /**
   * @param errorCode
   * @param flow
   */
  public FlowNotExecutingException(ErrorCode errorCode, NamedObj flow) {
    super(errorCode, "Flow not executing", flow, null);
  }
  /**
   * @param errorCode
   * @param flowName
   */
  public FlowNotExecutingException(ErrorCode errorCode, String flowName) {
    super(errorCode, "Flow not executing " + flowName, null);
  }
}
