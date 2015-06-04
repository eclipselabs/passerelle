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
package com.isencia.passerelle.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.Initializable;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.PausableResumable;

/**
 * Besides the std Ptolemy director stuff, and creation of Passerelle's ProcessThreads, this director adds support for :
 * <ul>
 * <li>error collectors and error notifications
 * <li>ErrorControlStrategies
 * <li>FiringEventListeners
 * </ul>
 * 
 * @author erwin
 */
public abstract class ProcessDirector extends CompositeProcessDirector implements PausableResumable, PasserelleDirector {
  private static Logger logger = LoggerFactory.getLogger(ProcessDirector.class);

  // annoyingly need to maintaina copy here of the activeThreads in the Ptolemy ProcessDirector baseclass,
  // as it is not reachable from subclasses....
  private Collection<Thread> myThreads = new HashSet<Thread>();
  
  /**
	 * 
	 */
  public ProcessDirector() throws IllegalActionException, NameDuplicationException {
    this(null);
  }

  /**
   * @param workspace
   */
  public ProcessDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
    super(workspace);
  }

  /**
   * @param container
   * @param name
   * @throws ptolemy.kernel.util.IllegalActionException
   * @throws ptolemy.kernel.util.NameDuplicationException
   */
  public ProcessDirector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
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
   * Return the configured DirectorAdapter for the given name.
   * If name is null or <code>DirectorAdapter.DEFAULT_ADAPTER_NAME</code>, and no adapter is present yet,
   * the default instance is lazily created and returned.
   * For other names, the specific adapter for that name is searched.
   * If none is found, null is returned.
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
    if (thread instanceof ProcessThread) {
      myThreads.remove((ProcessThread) thread);
    }
    
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
    return new ProcessThread(actor, (ProcessDirector) director);
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
    if (logger.isTraceEnabled())
      logger.trace(getName() + " initialize() - entry");
    getAdapter(null).getExecutionPrePostProcessor().preProcess();
    super.initialize();
    if (logger.isTraceEnabled())
      logger.trace(getName() + " initialize() - exit");
  }

  public void wrapup() throws IllegalActionException {
    if (logger.isTraceEnabled())
      logger.trace(getName() + " wrapup() - entry");
    getAdapter(null).getExecutionPrePostProcessor().postProcess();
    super.wrapup();
    if (logger.isTraceEnabled())
      logger.trace(getName() + " wrapup() - exit");
  }

  public void terminate() {
    if (logger.isTraceEnabled())
      logger.trace(getName() + " terminate() - entry");
    try {
      getAdapter(null).getExecutionPrePostProcessor().postProcess();
    } catch (IllegalActionException e) {
      // should never happen
      logger.error("Internal error - inconsistent attributes for director "+this.getFullName(), e);
    }
    super.terminate();
    if (logger.isTraceEnabled())
      logger.trace(getName() + " terminate() - exit");
  }
}
