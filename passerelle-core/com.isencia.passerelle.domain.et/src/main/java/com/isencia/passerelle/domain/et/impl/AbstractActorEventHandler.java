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
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.EventHandler;
import com.isencia.passerelle.runtime.Event;

/**
 * @author delerw
 */
public abstract class AbstractActorEventHandler implements EventHandler {
  private ETDirector director;

  public AbstractActorEventHandler(ETDirector director) {
    this.director = director;
  }

  public void initialize() {
  }

  public HandleResult handle(Event event, boolean isRetry) throws Exception {
    Actor actor = getDestinationActorFromEvent(event);
    com.isencia.passerelle.actor.Actor passerelleActor = null;
    if(actor instanceof com.isencia.passerelle.actor.Actor) {
      passerelleActor = (com.isencia.passerelle.actor.Actor) actor;
    }
    synchronized (actor) {
      if (director.isActorIterating(actor)) {
        getLogger().debug("Skipping {} - Actor {} is busy.", event, actor.getFullName());
        return HandleResult.RETRY;
      } else if (!director.getAdapter(null).isActorActive(actor)) {
        getLogger().debug("Skipping {} - Actor {} is inactive.", event, actor.getFullName());
        return HandleResult.SKIPPED;
      } else {
        director.notifyActorIteratingForEvent(actor, event);
      }
    }
    try {
      getLogger().debug("Handling {} - iterating Actor {}.", event, actor.getFullName());
      boolean fired=false;
      notifyEvent(passerelleActor, FiringEvent.BEFORE_ITERATE);
      notifyEvent(passerelleActor, FiringEvent.BEFORE_PREFIRE);
      boolean prefireOK = actor.prefire();
      notifyEvent(passerelleActor, FiringEvent.AFTER_PREFIRE);
      if (prefireOK) {
        notifyEvent(passerelleActor, FiringEvent.BEFORE_FIRE);
        actor.fire();
        notifyEvent(passerelleActor, FiringEvent.AFTER_FIRE);
        fired=true;
        notifyEvent(passerelleActor, FiringEvent.BEFORE_POSTFIRE);
        boolean postfireOK = actor.postfire();
        notifyEvent(passerelleActor, FiringEvent.AFTER_POSTFIRE);
        if (!postfireOK) {
          getLogger().debug("Handling {} - postFire() returned false for Actor {}.", event, actor.getFullName());
        }
      } else {
        getLogger().debug("Handling {} - preFire() returned false for Actor {}.", event, actor.getFullName());
      }
      if(!fired) {
        getLogger().error("Did not fire for "+event);
      }
      notifyEvent(passerelleActor, FiringEvent.AFTER_ITERATE);
      return HandleResult.DONE;
    } catch (IllegalActionException e) {
      Manager manager = ((CompositeActor)director.toplevel()).getManager();
      manager.notifyListenersOfException(e);
      throw e;
    } finally {
      director.notifyActorDoneIteratingForEvent(actor, event);
    }
  }
  
  /**
   * If the actor is not null and it is being debugged, send it an event of the given type.
   * @param passerelleActor
   * @param eventType
   * @return true if an event was sent to the actor, false otherwise
   */
  protected boolean notifyEvent(com.isencia.passerelle.actor.Actor passerelleActor, FiringEventType eventType) {
    if(passerelleActor!=null && passerelleActor.isDebugged()) {
      passerelleActor.recordFiring(eventType);
      return true;
    } else {
      return false;
    }
  }

  protected abstract Logger getLogger();
  protected abstract Actor getDestinationActorFromEvent(Event event);
}
