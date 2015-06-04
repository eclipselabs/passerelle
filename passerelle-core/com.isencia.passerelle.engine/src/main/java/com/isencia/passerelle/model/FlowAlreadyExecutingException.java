/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.model;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

/**
 * Exception thrown when someone tries to execute a given Flow while it is already executing.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class FlowAlreadyExecutingException extends PasserelleException {

  /**
   * @param flow
   */
  public FlowAlreadyExecutingException(NamedObj flow) {
    super(ErrorCode.FLOW_STATE_ERROR, "Flow already executing", flow, null);
  }

  /**
   * @param flowName
   */
  public FlowAlreadyExecutingException(String flowName) {
    super(ErrorCode.FLOW_STATE_ERROR, "Flow already executing " + flowName, null);
  }

  /**
   * @param errorCode
   * @param flow
   */
  public FlowAlreadyExecutingException(ErrorCode errorCode, NamedObj flow) {
    super(errorCode, "Flow already executing", flow, null);
  }

  /**
   * @param errorCode
   * @param flowName
   */
  public FlowAlreadyExecutingException(ErrorCode errorCode, String flowName) {
    super(errorCode, "Flow already executing " + flowName, null);
  }

}
