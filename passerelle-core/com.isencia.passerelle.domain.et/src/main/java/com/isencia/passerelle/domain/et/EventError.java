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
package com.isencia.passerelle.domain.et;

import com.isencia.passerelle.runtime.Event;

/**
 * Simple container class for an event and the exception that happened while
 * trying to process the event.
 * 
 * @author delerw
 *
 */
public class EventError {
  private Event event;
  private Exception errorCause;
  
  public EventError(Event event, Exception errorCause) {
    this.event=event;
    this.errorCause = errorCause;
  }
  public Event getEvent() {
    return event;
  }
  public Exception getErrorCause() {
    return errorCause;
  }
  @Override
  public String toString() {
    return "EventError [event=" + event + ", errorCause=" + errorCause + "]";
  }
}