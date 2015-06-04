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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.Receiver;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.domain.et.FlowExecutionEvent.FlowExecutionEventType;
import com.isencia.passerelle.domain.et.impl.ETReceiver;
import com.isencia.passerelle.domain.et.impl.FireEventHandler;
import com.isencia.passerelle.domain.et.impl.FlowEventHandler;
import com.isencia.passerelle.domain.et.impl.SendEventHandler;
import com.isencia.passerelle.domain.et.impl.SimpleEventDispatcher;
import com.isencia.passerelle.domain.et.impl.ThreadPoolEventDispatcher;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.message.MessageQueue;
import com.isencia.passerelle.message.SimpleActorMessageQueue;
import com.isencia.passerelle.runtime.Event;

/**
 * @author delerw
 */
@SuppressWarnings("serial")
public class ETDirector extends Director implements PasserelleDirector {

  public static final String KEEP_EVENT_HISTORY_PARAMNAME = "Keep event history";
  public static final String DISPATCH_TIMEOUT_PARAMNAME = "Dispatch timeout(ms)";
  public static final String NR_OF_DISPATCH_THREADS_PARAMNAME = "Nr of dispatch threads";

  private final static Logger LOGGER = LoggerFactory.getLogger(ETDirector.class);

  // not sure yet if this is a good idea or not,
  // to split-out a separate event dispatcher.
  // maybe easier to put the eventQ and behaviour directly in this director
  // splitting it out may allow to e.g. plugin different impls,
  // like one using OSGi event bus etc...
  private EventDispatcher dispatcher;
  private EventDispatchReporter dispatchReporter;

  private boolean notDone = true;

  // Map maintaining which actors are currently iterating, and for which triggering event
  private Map<Actor, Event> busyIteratingActors = new ConcurrentHashMap<Actor, Event>();

  public Parameter dispatchThreadsParameter;
  public Parameter dispatchTimeoutParameter;

  public Parameter eventHistoryParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ETDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    dispatchThreadsParameter = new Parameter(this, NR_OF_DISPATCH_THREADS_PARAMNAME, new IntToken(1));
    dispatchTimeoutParameter = new Parameter(this, DISPATCH_TIMEOUT_PARAMNAME, new IntToken(100));
    
    eventHistoryParameter = new Parameter(this, KEEP_EVENT_HISTORY_PARAMNAME, BooleanToken.FALSE);
    new CheckBoxStyle(eventHistoryParameter, "check");
    
    // to trigger the creation of our default adapter
    getAdapter(null);
    
    _attachText(
        "_iconDescription",
        "<svg>\n"
          + "<polygon points=\"-20,0 -10,-18 10,-18 20,0 10,18 -10,18\" "
          + "style=\"fill:red;stroke:red\"/>\n"
          + "<line x1=\"-9.5\" y1=\"17\" x2=\"-19\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"-19\" y1=\"0\" x2=\"-9.5\" y2=\"-17\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"-9\" y1=\"-17\" x2=\"9\" y2=\"-17\" "
          + "style=\"stroke-width:1.0;stroke:white\"/>\n"
          + "<line x1=\"10\" y1=\"-17.5\" x2=\"20\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"20\" y1=\"0\" x2=\"10\" y2=\"17.5\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"10\" y1=\"17.5\" x2=\"-10\" y2=\"17.5\" "
          + "style=\"stroke-width:1.0;stroke:black\"/>\n"
          + "<line x1=\"11\" y1=\"-15\" x2=\"19\" y2=\"0\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + "<line x1=\"19\" y1=\"0\" x2=\"11\" y2=\"16\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + "<line x1=\"10\" y1=\"17\" x2=\"-9\" y2=\"17\" "
          + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
          + 

      // director stand
      "<line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-6\" y1=\"10\" x2=\"6\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<polygon points=\"-8,0 -6,-8 8,-8 6,0\" "
        + "style=\"fill:lightgrey\"/>\n"
        + 

      //magic wand
      "<line x1=\"5\" y1=\"-15\" x2=\"15\" y2=\"-5\" "
        + "style=\"stroke-width:2.0;stroke:black\"/>\n"
        + "<line x1=\"5\" y1=\"-15\" x2=\"6\" y2=\"-14\" "
        + "style=\"stroke-width:2.0;stroke:white\"/>\n"
        + 
      // sparkles
      "<circle cx=\"12\" cy=\"-16\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "<circle cx=\"16\" cy=\"-16\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "<circle cx=\"14\" cy=\"-14\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n"
        + "</svg>\n");
  }
  
  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    List<Initializable> oldInitializables = this._initializables;
    this._initializables = null;
    Object clone = super.clone(workspace);
    this._initializables = oldInitializables;
    return clone;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void preinitialize() throws IllegalActionException {
    getAdapter(null).clearExecutionState();
    super.preinitialize();

    int threadCount = ((IntToken) dispatchThreadsParameter.getToken()).intValue();
    boolean needEventLog = ((BooleanToken)eventHistoryParameter.getToken()).booleanValue();

    List<EventHandler> eventHandlers = new ArrayList<EventHandler>();
    eventHandlers.add(new SendEventHandler(this));
    eventHandlers.add(new FireEventHandler(this));
    eventHandlers.add(new FlowEventHandler(this));
    // add any extra configured handlers
    eventHandlers.addAll(attributeList(EventHandler.class));
    
    if (threadCount > 1) {
      dispatcher = new ThreadPoolEventDispatcher(getFullName(), threadCount, eventHandlers.toArray(new EventHandler[eventHandlers.size()]));
    } else {
      dispatcher = new SimpleEventDispatcher(getFullName(), eventHandlers.toArray(new EventHandler[eventHandlers.size()]));
    }
    dispatcher.initialize();
    dispatchReporter = (EventDispatchReporter) dispatcher;
    dispatchReporter.enableEventHistory(needEventLog);
    notDone = true;
  }

  @Override
  public void initialize() throws IllegalActionException {
    getAdapter(null).getExecutionPrePostProcessor().preProcess();
    try {
      enqueueEvent(new FlowExecutionEvent((CompositeActor) getContainer(), FlowExecutionEventType.START));
    } catch (EventRefusedException e) {
      LOGGER.warn("Internal status error - refused flow start event",e);
    }
    super.initialize();
  }

  @Override
  public boolean prefire() throws IllegalActionException {
    return super.prefire();
  }

  @Override
  public synchronized void fire() throws IllegalActionException {
    try {
      int timeout = ((IntToken) dispatchTimeoutParameter.getToken()).intValue();
      // The order is important here, although maybe optimistic to rely on it
      // asynch actors may notify that their work is finished right after the dispatch() returned.
      // In which case they would most probably have added new events in the dispatch Q, but one microsecond too late to be noticed in the dispatch itself.
      // So the final hasWork check should still see these events then.
      notDone = dispatcher.dispatch(timeout) || getAdapter(null).hasBusyTaskActors() || dispatcher.hasWork();
    } catch (Exception e) {
      throw new IllegalActionException(this, e, "Error during dispatching of events");
    }
  }

  @Override
  public boolean postfire() throws IllegalActionException {
    if(!notDone) {
      return false;
    }
    if(!super.postfire()) {
      return false;
    }
    return true;
  }

  @Override
  public void wrapup() throws IllegalActionException {
    try {
      enqueueEvent(new FlowExecutionEvent((CompositeActor) getContainer(), FlowExecutionEventType.FINISH));
      // TODO a bit annoying this explicit dispatch call, but as fire-loop is done we need to repeat it one last time here; isn't there a better way???
      dispatcher.dispatch(10);
    } catch (EventRefusedException e) {
      LOGGER.warn("Internal status error - refused flow finish event",e);
    } catch (InterruptedException e) {
      LOGGER.warn("Internal status error - interrupted flow finish event",e);
    }
    getAdapter(null).getExecutionPrePostProcessor().postProcess();
    super.wrapup();
    boolean needEventLog = ((BooleanToken)eventHistoryParameter.getToken()).booleanValue();

    if(needEventLog) {
      System.err.println(getFullName()+" - Event History");
      for (Event evt : getEventHistory()) {
        System.err.println(evt);
      }
      System.err.println(getFullName()+" - Event Errors");
      for (EventError evtErr : getEventErrors()) {
        System.err.println(evtErr);
      }
      System.err.println(getFullName()+" - Unhandled Events");
      for (Event evt : getUnhandledEvents()) {
        System.err.println(evt);
      }
      System.err.println(getFullName()+" - Pending Events");
      for (Event evt : getPendingEvents()) {
        System.err.println(evt);
      }
    }
    
    dispatcher.shutdown();
  }

  @Override
  public void fireAt(Actor actor, Time time) throws IllegalActionException {
    // TODO add time handling
    try {
      enqueueEvent(new FireEvent(actor));
    } catch (EventRefusedException e) {
      throw new IllegalActionException(actor, e, "Error enqueing fire event");
    }
  }

  @Override
  public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
    try {
      enqueueEvent(new FireEvent(actor));
    } catch (EventRefusedException e) {
      throw new IllegalActionException(actor, e, "Error enqueing fire event");
    }
  }

  @Override
  public Receiver newReceiver() {
    return new ETReceiver(this);
  }
  
  public MessageQueue newMessageQueue(Actor actor) throws InitializationException {
    return new SimpleActorMessageQueue(actor);
  }

  public void enqueueEvent(Event event) throws EventRefusedException {
    if (dispatcher != null)
      dispatcher.accept(event);
  }

  public void notifyActorIteratingForEvent(Actor actor, Event event) {
    Event _e = busyIteratingActors.get(actor);
    if (_e == null) {
      busyIteratingActors.put(actor, event);
    } else if (_e != event) {
      throw new IllegalArgumentException("Actor " + actor.getFullName() + " iterating other event " + _e + " i.o. given event " + event);
    }
  }

  public void notifyActorDoneIteratingForEvent(Actor actor, Event event) {
    Event _e = busyIteratingActors.get(actor);
    if (event == _e) {
      busyIteratingActors.remove(actor);
    } else {
      throw new IllegalArgumentException("Actor " + actor.getFullName() + " iterating other event " + _e + " i.o. given event " + event);
    }
  }

  public boolean isActorIterating(Actor actor) {
    return busyIteratingActors.get(actor) != null;
  }

  public List<Event> getEventHistory() {
    return dispatchReporter.getEventHistory();
  }

  public List<Event> getUnhandledEvents() {
    return dispatchReporter.getUnhandledEvents();
  }

  public List<Event> getPendingEvents() {
    return dispatchReporter.getPendingEvents();
  }

  public List<EventError> getEventErrors() {
    return dispatchReporter.getEventErrors();
  }

  public void clearEvents() {
    dispatchReporter.clearEvents();
  }

  public DirectorAdapter getAdapter(String adapterName) throws IllegalActionException {
    return DirectorUtils.getAdapter(this, adapterName);
  }
}
