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

import java.util.List;
import com.isencia.passerelle.runtime.Event;

/**
 * Contract for obtaining a report on the execution of a model
 * that was processed in the ET domain.
 * <p>
 * Via this interface it is possible to obtain the history of processed events,
 * a list of unhandled events and a list of events that failed being processed, 
 * with the associated error cause. 
 * </p>
 * 
 * @author delerw
 *
 */
public interface EventDispatchReporter {
  
  /**
   * 
   * @param enable true if the reporter should maintain a full event history.
   */
  void enableEventHistory(boolean enable);
  
  
  /**
   * @return the list of all events that are still pending for execution
   */
  List<Event> getPendingEvents();
  
  /**
   * @return a list of all events, most recent first.
   */
  List<Event> getEventHistory();
  
  /**
   * Return the events for which no handler was found. 
   * <p>
   * Unhandled events are not necessarily a problem. 
   * There could be "information-only" events that are raised during a model execution
   * in case someone is interested to see them afterwards in the report.
   * </p> 
   * You should expect that all unhandled events are also present in the result of <code>getEventHistory()</code>
   * and that all events in the result of <code>getEventErrors()</code> are contained in the
   * list of unhandled events as well.
   * @return a list of unhandled events, most recent first.
   */
  List<Event> getUnhandledEvents();
  
  /**
   * Returns a list of events for which processing failedm, with the accompanying root cause info.
   * Remark that processing of events may be done through several <code>EventHandler</code>s, depending
   * on concrete domain implementations. In such cases, a same event could lead to several processing errors,
   * and you could find multiple entries in the resulting list.
   * 
   * @return events for which processing failed
   */
  List<EventError> getEventErrors();
  
  /**
   * Clear all event entries stored in this reporter.
   */
  void clearEvents();
}
