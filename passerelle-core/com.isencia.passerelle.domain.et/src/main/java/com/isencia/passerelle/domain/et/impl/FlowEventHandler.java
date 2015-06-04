/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.domain.et.impl;

import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.EventHandler;
import com.isencia.passerelle.domain.et.FlowExecutionEvent;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author erwin
 *
 */
public class FlowEventHandler implements EventHandler {
  private ETDirector director;

  /**
   * @param director
   */
  public FlowEventHandler(ETDirector director) {
    this.director=director;
  }

  public HandleType canHandleAs(Event event, boolean isRetry) {
    if (event instanceof FlowExecutionEvent) {
      return HandleType.FUNCTIONAL;
    } else {
      return HandleType.SKIP;
    }
  }

  public void initialize() {
  }

  public HandleResult handle(Event event, boolean isRetry) throws Exception {
    FlowExecutionEvent fee = (FlowExecutionEvent) event;
    ExecutionTracerService.trace(director, fee.getEventType()+" - "+fee.getTarget().getFullName());
    return HandleResult.DONE;
  }

}
