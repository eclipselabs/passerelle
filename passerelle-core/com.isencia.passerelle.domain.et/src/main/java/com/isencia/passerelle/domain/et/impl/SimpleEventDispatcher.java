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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.domain.et.AbstractEvent;
import com.isencia.passerelle.domain.et.EventDispatchReporter;
import com.isencia.passerelle.domain.et.EventDispatcher;
import com.isencia.passerelle.domain.et.EventError;
import com.isencia.passerelle.domain.et.EventHandler;
import com.isencia.passerelle.domain.et.EventHandler.HandleResult;
import com.isencia.passerelle.domain.et.EventHandler.HandleType;
import com.isencia.passerelle.domain.et.EventRefusedException;

/**
 * A basic implementation of an event dispatcher, based on a BlockingQueue and a method for dispatching the oldest queued event. The method must be invoked by
 * some external component (e.g. the model director).
 * 
 * @author delerw
 */
public class SimpleEventDispatcher implements EventDispatcher, EventDispatchReporter {

  private final static Logger LOGGER = LoggerFactory.getLogger(SimpleEventDispatcher.class);

  private String name;

  private static class EventEntry {
    Event event;
    boolean retry;
    public EventEntry(Event event) {
      this.event = event;
      this.retry = false;
    }
  }
  
  private BlockingQueue<EventEntry> eventQ = new LinkedBlockingQueue<EventEntry>();
  private List<Event> eventHistory = new LinkedList<Event>();
  private List<Event> unhandledEvents = new LinkedList<Event>();
  private List<EventError> eventErrors = new LinkedList<EventError>();

  private EventHandler eventHandlers[];

  // Flag to indicate whether the dispatcher should maintain a full history of all processed events.
  // Remark that this is only relevant for the "normal" history.
  // Unhandled events and errors are always kept around for consultation.
  private boolean keepHistory;
  
  // state variables for managing the shutdown sequence
  private volatile boolean active = true;
  private volatile boolean forcedShutdown = false;

  public SimpleEventDispatcher(String name, EventHandler... handlers) {
    this.name = name;
    eventHandlers = handlers;

    getLogger().debug("Created {}", this);
  }

  public void enableEventHistory(boolean enable) {
    this.keepHistory = enable;
  }
  
  public String getName() {
    return name;
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  public void initialize() {
    clearEvents();
    active = true;
    forcedShutdown = false;
    for (EventHandler evtHandler : eventHandlers) {
      evtHandler.initialize();
    }
    getLogger().debug("Initialized {}", this);
  }

  protected int getPendingEventCount() {
    return eventQ.size();
  }

  public void accept(Event e) throws EventRefusedException {
    if (!active) {
      throw new EventRefusedException(e, ErrorCode.FLOW_STATE_ERROR, "Dispatcher inactive " + getName(), new IllegalStateException());
    }
    try {
      eventQ.put(new EventEntry(e));
    } catch (Exception e1) {
      throw new EventRefusedException(e, ErrorCode.FLOW_EXECUTION_ERROR, "Error accepting event " + getName(), e1);
    }
  }

  public boolean hasWork() {
    return !eventQ.isEmpty();
  }
  
  public boolean dispatch(long timeOut) throws InterruptedException {
    if (forcedShutdown) {
      throw new IllegalStateException("Dispatcher forced to shutdown");
    }
    boolean possiblyMoreWork = false;
    boolean eventHandled = false;
    EventEntry eventEntry = null;
    Event event = null;
    try {
      eventEntry = eventQ.poll(timeOut, TimeUnit.MILLISECONDS);
      if (eventEntry != null) {
        event = eventEntry.event;
        boolean eventEffected = false;
        for (EventHandler evtHandler : eventHandlers) {
          try {
            HandleType handleType = evtHandler.canHandleAs(event, eventEntry.retry);
            if (HandleType.SKIP.equals(handleType)
                || (eventEffected && HandleType.EFFECT.equals(handleType)) ) {
              continue;
            } else {
              HandleResult result = evtHandler.handle(event, eventEntry.retry);
              if (HandleResult.DONE.equals(result)) {
                eventHandled = true;
                possiblyMoreWork = true;
                eventEffected = HandleType.EFFECT.equals(handleType);
              } else if (!eventEffected && HandleResult.RETRY.equals(result)){
                // interrupt current handling loop and add event to Q again for later retry
                possiblyMoreWork = true;
                EventEntry newEventEntry = new EventEntry(((AbstractEvent)event).copy());
                newEventEntry.retry=true;
                eventQ.add(newEventEntry);
                break;
              }
            }
          } catch (Exception e) {
            eventErrors.add(0, new EventError(event, e));
          }
        }
      }
    } finally {
      if (event != null) {
        if (eventHandled) {
          if(keepHistory) {
            eventHistory.add(0, event);
          }
        } else {
          unhandledEvents.add(0, event);
        }
      }
    }
    return possiblyMoreWork;
  }

  public List<Event> getEventHistory() {
    return eventHistory;
  }

  public List<Event> getUnhandledEvents() {
    return unhandledEvents;
  }
  
  public List<Event> getPendingEvents() {
    List<Event> result = new ArrayList<Event>();
    for(EventEntry ee : eventQ) {
      result.add(ee.event);
    }
    return result;
  }

  public List<EventError> getEventErrors() {
    return eventErrors;
  }

  public void clearEvents() {
    eventHistory.clear();
    unhandledEvents.clear();
    eventErrors.clear();
  }

  public void shutdown() {
    active = false;
    getLogger().debug("Shutdown {}", this);
  }

  public List<Event> shutdownNow() {
    getLogger().debug("shutdownNow {}", this);
    shutdown();
    forcedShutdown = true;
    List<EventEntry> pendingEventEntries = new ArrayList<EventEntry>(eventQ);
    getLogger().info("shutdownNow {} found {} pending events", this, pendingEventEntries.size());
    List<Event> pendingEvents = new ArrayList<Event>();
    for (EventEntry eventEntry : pendingEventEntries) {
      pendingEvents.add(eventEntry.event);
    }
    return pendingEvents;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (active)
      return false;
    // TODO do something here to block till all events have been processed
    return false;
  }

  @Override
  public String toString() {
    return getClass().getName() + " [name=" + name + ", active=" + active + "]";
  }
}
