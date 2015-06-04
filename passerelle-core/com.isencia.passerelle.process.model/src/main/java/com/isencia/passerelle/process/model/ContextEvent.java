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
package com.isencia.passerelle.process.model;


/**
 * Interface for <code>Event</code>s associated to <code>Context</code>s.
 * <p>
 * These can correspond to state transitions, or to arbitrary actions for which
 * someone thought it was important to notify listeners and/or store execution traces.
 * </p>
 * 
 * @author delerw
 */
public interface ContextEvent extends com.isencia.passerelle.runtime.Event, Identifiable, Comparable<ContextEvent> {

  /**
   * 
   * @return the associated context
   */
  Context getContext();
  
  /**
   * @return the event message, typically empty except for ERROR events, where error info is then stored in here.
   */
  String getMessage();
}