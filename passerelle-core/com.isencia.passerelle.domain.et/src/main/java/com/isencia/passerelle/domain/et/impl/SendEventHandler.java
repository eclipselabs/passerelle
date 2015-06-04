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

package com.isencia.passerelle.domain.et.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.SendEvent;

/**
 * This handler figures out the Actor for which the SendEvent is meant, and tries out a fire() iteration.
 * <p>
 * The normal Ptolemy/Passerelle iteration semantics are assumed to be valid :
 * <ul>
 * <li>preFire() is invoked. If it returns false, the iteration is not continued. If it returns true, the actor is ready to be fired.</li>
 * <li>fire() is invoked after preFire() returned true.</li>
 * <li>postFire() is invoked. If it returns false, this actor can be wrapped up.</li>
 * </ul>
 * </p>
 * 
 * @author delerw
 */
public class SendEventHandler extends AbstractActorEventHandler {
  private final static Logger LOGGER = LoggerFactory.getLogger(SendEventHandler.class);

  public SendEventHandler(ETDirector director) {
    super(director);
  }

  public HandleType canHandleAs(Event event, boolean isRetry) {
    if (event instanceof SendEvent) {
      return HandleType.EFFECT;
    } else {
      return HandleType.SKIP;
    }
  }

  @Override
  public HandleResult handle(Event event, boolean isRetry) throws Exception {
    if (event instanceof SendEvent) {
      SendEvent _evt = (SendEvent) event;
      if (!_evt.isProcessed()) {
        return super.handle(event, isRetry);
      } else {
        // evt has already been processed
        // this can happen e.g. with stream-handling actors that
        // pop a whole bunch of received pushed messages in one iteration (triggered from a previous Event)
        return HandleResult.DONE;
      }
    } else {
      return HandleResult.SKIPPED;
    }
  }

  protected Actor getDestinationActorFromEvent(Event event) {
    Actor actor = (Actor) ((SendEvent) event).getReceivingPort().getContainer();
    return actor;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
