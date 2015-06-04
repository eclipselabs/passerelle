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
package com.isencia.passerelle.domain.cap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.Initializable;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.message.MessageQueue;

/**
 * A new version of the Passerelle process-domain director, directly extending from Ptolemy's PNDirector, mainly to try to reuse Ptolemy's receiver queue
 * handling as much as possible and at the same time to start reducing the volume of Ptolemy customizations in Passerelle.
 * 
 * @author erwin
 */
public class CapDirector extends PNDirector implements PasserelleDirector {
  private static final long serialVersionUID = 5892449989000600722L;

  private static Logger LOGGER = LoggerFactory.getLogger(CapDirector.class);

  // annoyingly need to maintain copy here of the activeThreads in the Ptolemy ProcessDirector baseclass,
  // as it is not reachable from subclasses....
  private Collection<Thread> myThreads = new HashSet<Thread>();

  /**
   * Construct a director in the default workspace with an empty string as its name. The director is added to the list of objects in the workspace. Increment
   * the version number of the workspace. Create a director parameter "Initial_queue_capacity" with the default value 1. This sets the initial capacities of the
   * queues in all the receivers created in the PN domain.
   */
  public CapDirector() throws IllegalActionException, NameDuplicationException {
    this(null);
  }

  /**
   * Construct a director in the workspace with an empty name. The director is added to the list of objects in the workspace. Increment the version number of
   * the workspace. Create a director parameter "Initial_queue_capacity" with the default value 1. This sets the initial capacities of the queues in all the
   * receivers created in the PN domain.
   * 
   * @param workspace
   *          The workspace of this object.
   */
  public CapDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
    super(workspace);
    // to trigger the creation of our default adapter
    getAdapter(null);
  }

  /**
   * Construct a director in the given container with the given name. If the container argument must not be null, or a NullPointerException will be thrown. If
   * the name argument is null, then the name is set to the empty string. Increment the version number of the workspace. Create a director parameter
   * "Initial_queue_capacity" with the default value 1. This sets the initial capacities of the queues in all the receivers created in the PN domain.
   * 
   * @param container
   *          Container of the director.
   * @param name
   *          Name of this director.
   * @exception IllegalActionException
   *              If the director is not compatible with the specified container. Thrown in derived classes.
   * @exception NameDuplicationException
   *              If the container not a CompositeActor and the name collides with an entity in the container.
   */
  public CapDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // to trigger the creation of our default adapter
    getAdapter(null);

    _attachText("_iconDescription", "<svg>\n" + "<polygon points=\"-20,0 -10,-18 10,-18 20,0 10,18 -10,18\" " + "style=\"fill:red;stroke:red\"/>\n"
        + "<line x1=\"-9.5\" y1=\"17\" x2=\"-19\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"0\" x2=\"-9.5\" y2=\"-17\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-9\" y1=\"-17\" x2=\"9\" y2=\"-17\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"10\" y1=\"-17.5\" x2=\"20\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"20\" y1=\"0\" x2=\"10\" y2=\"17.5\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"10\" y1=\"17.5\" x2=\"-10\" y2=\"17.5\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"11\" y1=\"-15\" x2=\"19\" y2=\"0\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<line x1=\"19\" y1=\"0\" x2=\"11\" y2=\"16\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"10\" y1=\"17\" x2=\"-9\" y2=\"17\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        +

        // director stand
        "<line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-6\" y1=\"10\" x2=\"6\" y2=\"10\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<polygon points=\"-8,0 -6,-8 8,-8 6,0\" "
        + "style=\"fill:lightgrey\"/>\n"
        +

        // magic wand
        "<line x1=\"5\" y1=\"-15\" x2=\"15\" y2=\"-5\" " + "style=\"stroke-width:2.0;stroke:black\"/>\n" + "<line x1=\"5\" y1=\"-15\" x2=\"6\" y2=\"-14\" "
        + "style=\"stroke-width:2.0;stroke:white\"/>\n"
        +
        // sparkles
        "<circle cx=\"12\" cy=\"-16\" r=\"1\"" + "style=\"fill:black;stroke:white\"/>\n" + "<circle cx=\"16\" cy=\"-16\" r=\"1\""
        + "style=\"fill:black;stroke:white\"/>\n" + "<circle cx=\"14\" cy=\"-14\" r=\"1\"" + "style=\"fill:black;stroke:white\"/>\n" + "</svg>\n");
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    List<Initializable> oldInitializables = this._initializables;
    this._initializables = null;
    Object clone = super.clone(workspace);
    this._initializables = oldInitializables;
    return clone;
  }

  /**
   * Return the configured DirectorAdapter for the given name. If name is null or <code>DirectorAdapter.DEFAULT_ADAPTER_NAME</code>, and no adapter is present
   * yet, the default instance is lazily created and returned. For other names, the specific adapter for that name is searched. If none is found, null is
   * returned.
   * 
   * @param adapterName
   * @return
   * @throws IllegalActionException
   */
  public DirectorAdapter getAdapter(String adapterName) throws IllegalActionException {
    return DirectorUtils.getAdapter(this, adapterName);
  }

  @Override
  public synchronized void addThread(Thread thread) {
    super.addThread(thread);
    if (thread instanceof ProcessThread) {
      myThreads.add((ProcessThread) thread);
    }
  }

  @Override
  public void preinitialize() throws IllegalActionException {
    getAdapter(null).clearExecutionState();
    myThreads.clear();
    super.preinitialize();
  }

  @Override
  public synchronized void removeThread(Thread thread) {
    super.removeThread(thread);
    myThreads.remove(thread);

    Set<Actor> activeActorsWithoutInputs = DirectorUtils.getRootActorsForActiveBranchesAndLoops(this);
    boolean areAllDaemon = true;
    for (Actor actor : activeActorsWithoutInputs) {
      if (!(actor instanceof com.isencia.passerelle.actor.Actor) || !((com.isencia.passerelle.actor.Actor)actor).isDaemon()) {
        areAllDaemon = false;
        break;
      }
    }
    if (areAllDaemon) {
      for (Actor actor : activeActorsWithoutInputs) {
        ((com.isencia.passerelle.actor.Actor)actor).requestFinish();
      }
    }
  }

  public Collection<Thread> getThreads() {
    return myThreads;
  }

  @Override
  protected ptolemy.actor.process.ProcessThread _newProcessThread(Actor actor, ptolemy.actor.process.ProcessDirector director) throws IllegalActionException {
    return new ProcessThread(actor, (CapDirector) director);
  }

  @Override
  public Receiver newReceiver() {
    CapReceiver receiver = new CapReceiver();
    try {
      int capacity = ((IntToken) initialQueueCapacity.getToken()).intValue();
      receiver.setCapacity(capacity);
    } catch (IllegalActionException e) {
      throw new InternalErrorException(e);
    }
    return receiver;
  }
  
  @Override
  public MessageQueue newMessageQueue(Actor actor) throws InitializationException {
    try {
      return new CapActorMessageQueue(actor, ((IntToken) maximumQueueCapacity.getToken()).intValue());
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.ERROR, "Unable to create actor message queue", actor, e);
    }
  }

  /**
   * just an alias for stopFire()...
   */
  public void pauseAllActors() {
    stopFire();
  }

  public void resumeAllActors() {
    Iterator<Thread> threads = myThreads.iterator();

    while (threads.hasNext()) {
      ProcessThread thread = (ProcessThread) threads.next();

      if (thread.getActor() instanceof com.isencia.passerelle.actor.Actor) {
        ((com.isencia.passerelle.actor.Actor) thread.getActor()).resumeFire();
      }
    }

    _stopFireRequested = false;
  }

  public void initialize() throws IllegalActionException {
    LOGGER.trace("{} - initialize() - entry", getFullName());
    getAdapter(null).getExecutionPrePostProcessor().preProcess();
    super.initialize();
    LOGGER.trace("{} - initialize() - exit", getFullName());
  }

  @Override
  public boolean postfire() throws IllegalActionException {
    LOGGER.trace("{} - postfire() - entry", getFullName());
    boolean result = super.postfire();
    if(!result && !_notDone) {
      // need to add our Passerelle busy-actor-management to include support for actors doing their processing in a background thread
      boolean hasBusyActors = getAdapter(null).hasBusyTaskActors();
      LOGGER.debug("{} - postfire() - _notDone false ; busy actors {}", getFullName(), hasBusyActors);
      result = _notDone = hasBusyActors;
    }
    LOGGER.trace("{} - postfire() - exit : {}", getFullName(), result);
    return result;
  }

  public void wrapup() throws IllegalActionException {
    LOGGER.trace("{} - wrapup() - entry", getFullName());
    getAdapter(null).getExecutionPrePostProcessor().postProcess();
    super.wrapup();
    LOGGER.trace("{} - wrapup() - exit", getFullName());
  }

  public void terminate() {
    LOGGER.trace("{} - terminate() - entry", getFullName());
    try {
      getAdapter(null).getExecutionPrePostProcessor().postProcess();
    } catch (IllegalActionException e) {
      // should never happen
      LOGGER.error("Internal error - inconsistent attributes for director " + this.getFullName(), e);
    }
    super.terminate();
    LOGGER.trace("{} - terminate() - exit", getFullName());
  }
}