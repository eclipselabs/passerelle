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
import java.util.concurrent.TimeUnit;
import com.isencia.passerelle.runtime.Event;

/**
 * @author delerw
 */
public interface EventDispatcher {

  /**
   * 
   */
  void initialize();

  /**
   * Accept a new event, buffer it if needed, and dispatch it to the right handler.
   * 
   * @param e
   * @throws EventRefusedException
   */
  void accept(Event e) throws EventRefusedException;
  
  /**
   * 
   * @return true if the dispatcher has pending events that still need to be dispatched. false otherwise.
   */
  boolean hasWork();

  /**
   * @param timeOut (ms)
   * @return true if an event was dispatched or must be retried, false if no event was pending during the given timeout
   * @throws InterruptedException
   */
  boolean dispatch(long timeOut) throws InterruptedException;

  /**
   * Initiate a nice shutdown, i.e. allow all pending events to be processed first, but do not accept extra events in the meantime. <br>
   * Remark that this method returns immediately, without blocking till all events have been processed.
   * 
   * @see awaitTermination()
   */
  void shutdown();

  /**
   * Initiate an immediate shutdown and return all pending events, i.e. the events that were not yet delivered to their handlers. <br>
   * Remark that this method returns immediately, without blocking till all events have been processed.
   * 
   * @see awaitTermination()
   * @return list of pending requests
   */
  List<Event> shutdownNow();

  /**
   * Block until the shutdown() sequence has terminated, or the given timeout has expired. <br>
   * If no shutdown() is ongoing, returns immediately.
   * 
   * @param timeout
   * @param unit
   * @return true if a real shutdown was terminated, false if no shutdown ongoing, or timed out
   * @throws InterruptedException
   */
  boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
