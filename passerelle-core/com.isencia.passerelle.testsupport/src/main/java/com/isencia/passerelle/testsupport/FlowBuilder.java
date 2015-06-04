/* Copyright 2014 - iSencia Belgium NV

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
package com.isencia.passerelle.testsupport;

import com.isencia.passerelle.model.Flow;

/**
 * Interface for builders that can be passed into FlowExecutionTester's multi-run test executions.
 * 
 * @author erwindl
 *
 */
public interface FlowBuilder {
  
  /**
   * Build a flow instance with the given name.
   * 
   * @param name
   * @return a newly constructed Flow with the given name.
   * @throws Exception
   */
  Flow buildFlow(String name) throws Exception;
}
