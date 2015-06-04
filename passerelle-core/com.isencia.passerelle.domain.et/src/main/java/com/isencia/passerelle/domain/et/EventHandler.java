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

package com.isencia.passerelle.domain.et;

import com.isencia.passerelle.runtime.Event;

/**
 * Contract for concrete event handlers.
 * <p>
 * Event handlers can handle an event with different "consequences". Two aspects may be important :
 * <ul>
 * <li>Is it a problem if the handler does its thing multiple times with a given event, e.g. during a retry-loop or after recovery etc?</li>
 * <li>Is the handler prepared to actually do something with the event (and afterwards, did it actually do it)?</li>
 * </ul>
 * </p>
 * <p>
 * The first aspect is reflected in the <code>HandleType</code> that the handler must return from <code>EventHandler.canHandleAs()</code>.
 * A <code>FUNCTIONAL</code> handling can be considered as repeatable without significant negative consequences.
 * A handling with <code>EFFECT</code> implies that this handler's event processing should be considered as the singular "processing-with-real-impact".
 * This implies that no other handlers-with-EFFECT can be allowed to handle this event anymore, nor can a retry be attempted.
 * Only further <code>FUNCTIONAL</code> event-handling can still be done after an <code>EFFECT</code> handler has actually processed the event.
 * </p>
 * <p>
 * For the second aspect, a combination of the <code>HandleType</code> returned by <code>EventHandler.canHandleAs()</code>, 
 * and the <code>HandleResult</code> returned by <code>EventHandler.handle()</code> (after it has been invoked) must be considered.<br/> 
 * <code>HandleType.SKIP</code> means that the given handler is not interested in the event, or not capable of handling it.
 * </p>
 * <p>
 * By returning SKIP, the handler is guaranteed that the event dispatcher will not invoke <code>EventHandler.handle()</code> for the given event.
 * When the handler has not returned SKIP, its <code>EventHandler.handle()</code> will actually be invoked some time later.
 * </p>
 * <p>
 * When the handling was successful, the handler should return <code>DONE</code>.
 * <br/>
 * It could still happen that the handler is, at the exact time when its <code>handle()</code> is invoked, not able to handle the event. 
 * E.g. because its underlying resource (like an actor) is already occupied. Then the handler should return one of two outcomes :
 * <ul>
 * <li>SKIPPED : indicates that no processing was done, but the handler is OK with this.</li>
 * <li>RETRY : indicates that no processing was done, and the handler would like to get a next shot at it, a bit later.</li>
 * </ul>
 * </p>
 * 
 * @author erwin
 *
 */
public interface EventHandler {
  
  enum HandleType {
    // indicates that the handler does not want to handle the event
    SKIP,
    // indicates that the handler has no (side)-effects, i.e. the event can freely be offered to other handlers after its processing here
    FUNCTIONAL, 
    // indicates that the handler has (side)-effects, and the event should only be offered to remaining FUNCTIONAL handlers
    EFFECT;
  }
  
  enum HandleResult {
    // means the handler has successfully handled the event
    DONE,
    // means that the handler was not able to handle the event for whatever reason, and is OK with this
    SKIPPED,
    // means that the handler was currently not able to handle the event for whatever reason, but would like to retry later
    // a retry loop will only be done when the event was not yet handled by a previous handler with EFFECT type...
    RETRY;
  }
  
  void initialize();
  
  /**
   * 
   * @param event
   * @param isRetry indicates whether the event handling is retried, i.e. it has already been going through the handlers once
   * and one of them has requested to retry again later.
   * @return
   */
  HandleType canHandleAs(Event event, boolean isRetry);
  
  /**
   * 
   * @param event
   * @param isRetry indicates whether the event handling is retried, i.e. it has already been going through the handlers once
   * and one of them has requested to retry again later.
   * @throws Exception
   * @return 
   */
  HandleResult handle(Event event, boolean isRetry) throws Exception;

}
