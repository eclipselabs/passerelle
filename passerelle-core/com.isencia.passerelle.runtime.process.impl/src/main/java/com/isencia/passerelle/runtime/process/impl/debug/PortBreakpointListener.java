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
package com.isencia.passerelle.runtime.process.impl.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.runtime.process.impl.executor.FlowExecutionTask;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPortEvent;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

public class PortBreakpointListener implements DebugListener {
  private final static Logger LOGGER = LoggerFactory.getLogger(PortBreakpointListener.class);
  private static final int BREAKPOINT_EVENT_TYPE_INPUT_PORT = IOPortEvent.GET_END;
  private static final int BREAKPOINT_EVENT_TYPE_OUTPUT_PORT = IOPortEvent.SEND;

  private FlowExecutionTask fet;
  private String name;
  
  public PortBreakpointListener(String name, FlowExecutionTask fet) {
    this.fet = fet;
    this.name = name;
  }

  @Override
  public void event(DebugEvent event) {
    if (event instanceof IOPortEvent) {
      IOPortEvent pe = (IOPortEvent) event;
      if ((BREAKPOINT_EVENT_TYPE_INPUT_PORT == pe.getEventType()) 
          || (BREAKPOINT_EVENT_TYPE_OUTPUT_PORT == pe.getEventType())) {
        Port p = pe.getPort();
        LOGGER.info("Suspend on breakpoint {}", p.getFullName());
        if(fet!=null) {
          fet.addSuspendedElement(name);
        }
        ((CompositeActor)p.toplevel()).getManager().pauseOnBreakpoint(p.getFullName());
      }
    }
  }

  @Override
  public void message(String message) {
    // ignore in this listener
  }
}
