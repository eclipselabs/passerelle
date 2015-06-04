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
package com.isencia.passerelle.ext;

/**
 * Contract for components that need to do some preparation work before a model starts its execution, 
 * and some cleanup after the model execution has finished. 
 * <p>
 * A pre/post processor can be configured as a kind of interceptor for a model execution, 
 * providing custom logic that should be executed at the start/finish of a model run. 
 * It is typically used in situations where common non-functional logic is needed 
 * with which a model designer should not be bothered each time.
 * </p>
 * 
 * @author erwin
 * 
 * @see com.isencia.passerelle.ext.DirectorAdapter
 */
public interface ExecutionPrePostProcessor {
  /**
   * Method that will be invoked once, before the model starts its processing.
   * The call is done after the pre-initialization of all actors is done and before their initialization.
   */
  void preProcess();
  /**
   * Method that will be called once during the wrapup of a model execution.
   */
  void postProcess();
}
