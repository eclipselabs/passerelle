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
package com.isencia.passerelle.runtime.process;

import com.isencia.passerelle.runtime.Event;

/**
 * An event interface as a basis for all specific events related to flow process executions.
 * It reuses some concepts from eclipse's DebugEvent, e.g. the <i>kind</i> and <i>detail</i>.
 * <p>
 * Topics of ProcessEvents are of the format <code>com/isencia/passerelle/process/[PROCESS ID]/[KIND]/[DETAIL]/...</code>.
 * </p>
 * 
 * @author erwin
 * @see org.eclipse.debug.core.DebugEvent about event kind & detail
 */
public interface ProcessEvent extends Event {

  String TOPIC_PREFIX = "com/isencia/passerelle/process/";
  
  enum Kind {
    /**
     * Indicates a process was resumed. 
     * The detail will indicate in which way it was resumed (STEP or CLIENT_REQUEST).
     */
    RESUME(0x0001), 
    /**
     * Indicates a process was suspended. 
     * The detail will indicate in which way it was resumed (STEP_END, BREAKPOINT or CLIENT_REQUEST).
     */
    SUSPEND(0x0002), 
    /**
     * Indicates a process or a child(i.e. an actor iteration cycle) was created/started
     */
    CREATE(0x0004), 
    /**
     * Indicates a process or a child(i.e. an actor iteration cycle) was terminated on CLIENT_REQUEST (via a specific terminate() call) or naturally (with UNSPECIFIED detail)
     */
    TERMINATE(0x0008), 
    UNSPECIFIED(0);
    
    int kind;
    Kind(int kind) {
      this.kind = kind;
    }
    int getKind() {
      return kind;
    }
  }

  enum Detail {
    /**
     * Indicates a process was resumed by a step action.
     * <p>
     * In eclipse's DebugEvent, this value is used for STEP_OVER.
     * This one corresponds more-or-less to the step-"level" we have in Passerelle flow executions, 
     * if we map each actor iteration to the level of a method-call in a Java program.
     * <br/>
     * In the far future we might want to differentiate stepping <i>over</i> versus <i>into</i> sub-models, 
     * but for the moment the execution model is considered "flat", i.e. we just consider each actor's iterations,
     * irrespective of the root/sub-model level where the actor is located.
     * </p>
     */
    STEP(0x0002),
    /**
     * Indicates a process was suspended due to the completion of a step action.
     */
    STEP_END(0x0008),
    /**
     * Indicates a process was suspended by a breakpoint.
     */
    BREAKPOINT(0x0010),
    /**
     * Indicates a process was suspended, resumed or terminated due to a client request, i.e. an explicit suspend(), resume() or terminate() call.
     */
    CLIENT_REQUEST(0x0020),
    UNSPECIFIED(0);
    int detail;
    Detail(int detail) {
      this.detail = detail;
    }
    int getDetail() {
      return detail;
    }
  }

  /**
   * @return the kind of the event
   * @see org.eclipse.debug.core.DebugEvent.getKind()
   */
  Kind getKind();

  /**
   * @return the detail of the event
   * @see org.eclipse.debug.core.DebugEvent.getDetail()
   */
  Detail getDetail();
  
  /**
   * @return the id of the process to which this event is related
   */
  String getProcessContextId();
}
