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
package com.isencia.passerelle.runtime.process.impl.executor;

import java.util.concurrent.FutureTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.process.ProcessStatus;

/**
 * 
 * @author erwin
 *
 */
public final class FlowExecutionFuture extends FutureTask<ProcessStatus> {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(FlowExecutionFuture.class);
  
  private FlowExecutionTask fet;
  
  public FlowExecutionFuture(FlowExecutionTask callable) {
    super(callable);
    this.fet = callable;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    try {
      fet.cancel();
    } catch (Throwable t) {
      LOGGER.error(ErrorCode.ERROR+" - Failed to cancel a FlowExecutionTask for "+fet.getFlowHandle(),t);
    }
    return super.cancel(mayInterruptIfRunning);
  }
  
  public boolean suspend() {
    return fet.suspend();
  }
  
  public boolean resume() {
    return fet.resume();
  }
  
  public String getProcessContextId() {
    return fet.getProcessContextId();
  }
  
  public ProcessStatus getStatus() {
    return fet.getStatus();
  }
  
  public String[] getSuspendedElements() {
    return fet.getSuspendedElements();
  }

  
  public FlowHandle getFlowHandle() {
    return fet.getFlowHandle();
  }
}