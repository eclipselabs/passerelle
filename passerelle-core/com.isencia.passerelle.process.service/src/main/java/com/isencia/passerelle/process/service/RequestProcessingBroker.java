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
package com.isencia.passerelle.process.service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.model.Request;

/**
 * 
 * @author erwin
 *
 */
public interface RequestProcessingBroker<R extends Request> {
  
  /**
   * @param request the {@link Request} that must be processed
   * @param timeout the timeout period; null or <=0 values indicate : no timeout should be set.
   * @param unit the {@link TimeUnit} of the timeunit period
   * @return null if this service is unable to process the given task; 
   * a Future to the task after processing is finished.
   * @throws ProcessingException when the delivery has failed of the {@link Request} to a service able to process it.
   * E.g. when no service is found for it.
   */
  Future<R> process(R request, Long timeout, TimeUnit unit) throws ProcessingException;
  
  /**
   * 
   * @param service
   * @return <tt>true</tt> if the new service was successfully registered
   */
  boolean registerService(RequestProcessingService<R> service);
  
  /**
   * 
   * @param service
   * @return <tt>true</tt> if the service was registered and was successfully removed
   */
  boolean removeService(RequestProcessingService<R> service);
  
  /**
   * remove all registered services
   */
  void clearServices();
}
