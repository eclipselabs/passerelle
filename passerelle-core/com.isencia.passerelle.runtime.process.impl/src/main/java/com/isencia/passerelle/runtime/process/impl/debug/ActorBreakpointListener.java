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
import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

public class ActorBreakpointListener implements DebugListener {
  private final static Logger LOGGER = LoggerFactory.getLogger(ActorBreakpointListener.class);
  private final static FiringEventType BREAKPOINT_EVENT_TYPE = FiringEvent.BEFORE_FIRE;

  private FlowExecutionTask fet;
  private String name;
  
  public ActorBreakpointListener(String name, FlowExecutionTask fet) {
    this.fet = fet;
    this.name = name;
  }

  @Override
  public void event(DebugEvent event) {
    if (event instanceof FiringEvent) {
      FiringEvent fe = (FiringEvent) event;
      if (BREAKPOINT_EVENT_TYPE.equals(fe.getType())) {
        Actor a = fe.getActor();
        LOGGER.info("Suspend on breakpoint {}", name);
        if(fet!=null) {
          fet.addSuspendedElement(name);
        }
        a.getManager().pauseOnBreakpoint(name);
      }
    }
  }

  @Override
  public void message(String message) {
    // ignore in this listener
  }
}
