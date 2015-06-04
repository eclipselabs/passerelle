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
package com.isencia.passerelle.process.actor.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.actor.TaskBasedActor;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.event.DoubleValuedEventImpl;
import com.isencia.passerelle.process.model.event.StringValuedEventImpl;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.runtime.Event;

/**
 * This actor provides a bridge from a classic EDM Context/Task-based processing towards the event-based processing "world".
 * <p>
 * It can be used to map a Task in the ongoing processing context to a stream of Events :
 * <ul>
 * <li>The Task's ContextEvents are sent out as is</li>
 * <li>The Task's results (ResultItems) are transformed into value-holding Events and sent out as well</li>
 * </ul>
 * </p>
 * <p>
 * All sent Events have the creation timestamp of their "sources", and will be sent out in the right order. I.e. oldest Events first. But there are no
 * intermediate delays introduced between the sent events, they are all sent out in one rapid sequence.
 * </p>
 * <p>
 * If the received processing Context contains multiple Tasks with the given type, the most recently created one is used.
 * </p>
 * 
 * @author erwin
 */
public abstract class AbstractEventsGenerator extends TaskBasedActor {
  private static final long serialVersionUID = -2237583566697927936L;
  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractEventsGenerator.class);

  private List<Event> events;

  public AbstractEventsGenerator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected boolean doPreFire() throws ProcessingException {
    if (events != null) {
      events.clear();
    } else {
      events = new ArrayList<Event>();
    }
    return super.doPreFire();
  }

  @Override
  protected void postProcess(ManagedMessage message, Task task, ProcessResponse response) throws Exception {
    Collections.sort(events, new EventComparator());
    sendEventBatch(events, response);
    ProcessManager procMgr  = response.getProcessManager();
    procMgr.notifyEvent(task, "EVENTS_SENT", events.toString());
    procMgr.notifyFinished(task);
    processFinished(response.getProcessManager(), response.getRequest(), response);
  }

  private void sendEventBatch(Collection<Event> batch, ProcessResponse response) throws MessageException {
    getLogger().debug("Sending batch {}", batch);
    for (Event event : batch) {
      ManagedMessage message = createMessage(event, ManagedMessage.objectContentType);
      response.addOutputMessageInSequence(output, message);
    }
  }

  protected void addEvents(Collection<? extends Event> otherEvents) {
    events.addAll(otherEvents);
  }

  protected Event createEvent(Date d, String name, String value) {
    Event evt = null;
    try {
      Double dv = Double.parseDouble(value);
      evt = new DoubleValuedEventImpl(name, dv, d, 0L);
    } catch (NumberFormatException e) {
      evt = new StringValuedEventImpl(name, value, d, 0L);
    }
    events.add(evt);
    return evt;
  }

  protected static class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event o1, Event o2) {
      int res = o1.getCreationTS().compareTo(o2.getCreationTS());
      if (res == 0) {
        res = o1.getTopic().compareTo(o2.getTopic());
      }
      if (res == 0 && ((o1 instanceof ResultItem<?>) && (o2 instanceof ResultItem<?>))) {
        res = ((ResultItem<?>) o1).getValueAsString().compareTo(((ResultItem<?>) o2).getValueAsString());
      }
      return res;
    }
  }
}
