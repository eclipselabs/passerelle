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
package com.isencia.passerelle.runtime.repository;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.model.Flow;

/**
 * @author erwin
 *
 */
public class DuplicateEntryException extends RepositoryException {

  private static final long serialVersionUID = 7349254541339728923L;

  /**
   * @param flowCode
   */
  public DuplicateEntryException(String flowCode) {
    super(ErrorCode.FLOW_SAVING_ERROR_FUNC, "Flow "+flowCode+" already exists in the repository", null);
  }
  /**
   * @param flowCode
   * @param flow
   */
  public DuplicateEntryException(String flowCode, Flow flow) {
    super(ErrorCode.FLOW_SAVING_ERROR_FUNC, "Flow "+flowCode+" already exists in the repository", flow, null);
  }
}
