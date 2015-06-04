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
package com.isencia.passerelle.process.actor;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * Minimal contract for process-context-aware actors.
 * 
 * @author erwin
 *
 */
public interface ProcessActor {
  
  /**
   * Method to indicate whether the given request will be processed synchronously or asynchronously. 
   * <p>
   * Actors that have asynchronous processing, should combine returning <code>ProcessingMode.ASYNCHRONOUS</code> here, with invoking
   * <code>processFinished(ActorContext ctxt, ProcessRequest request, ProcessResponse response)</code> when the work is done for a given request.
   * </p>
   * 
   * @param ctxt
   * @param request
   * @return whether the given request will be processed synchronously or asynchronously.
   */
  ProcessingMode getProcessingMode(ProcessRequest request);

  /**
   * The actual method where the functional processing of the actor must be implemented.
   * <p>
   * Remark that it is not advisable to put complex logic within the actor implementation itself,
   * but to put this in a service layer, and to have actors and services communicating via <code>Task</code>s.
   * </p>
   * 
   * @param processManager
   *          the manager for the request processing lifecycle
   * @param request
   *          the request that must be processed
   * @param response
   *          after processing this should contain the output messages that the actor should send, or a ProcessingException if some error was encountered during
   *          processing. (However, normally, for synchronous processing, exceptions will just be thrown.)
   * @throws ProcessingException
   */
  void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response) throws ProcessingException;

  /**
   * @param processManager
   *          the manager for the request processing lifecycle
   * @param request
   *          the request that was processed
   * @param response
   *          contains the output messages that the actor should send, or a ProcessingException if some error was encountered during processing.
   */
  void processFinished(ProcessManager processManager, ProcessRequest request, ProcessResponse response);

}
