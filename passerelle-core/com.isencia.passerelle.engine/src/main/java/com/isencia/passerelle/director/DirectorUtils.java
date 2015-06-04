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

package com.isencia.passerelle.director;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.Actor;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.impl.DefaultDirectorAdapter;
import com.isencia.passerelle.ext.impl.NullDirectorAdapter;

/**
 * @author erwin
 */
public class DirectorUtils {
  private static Logger LOGGER = LoggerFactory.getLogger(DirectorUtils.class);

  /**
   * Tries to obtain the configured adapter with the given name, on the given director. If the name is null, it is treated as the default name
   * <code>DirectorAdapter.DEFAULT_ADAPTER_NAME</code>.
   * <p>
   * For the default name, if no adapter was present yet, a default adapter is set. For other names, null is returned in that case.
   * </p>
   * <p>
   * I.e. there is theoretical support to optionally attach multiple adapters to a same director, for very specific cases. But a default adapter is presumed
   * present.
   * </p>
   * 
   * @param director
   *          not null!
   * @param adapterName
   * @return
   */
  public static DirectorAdapter getAdapter(Director director, String adapterName) {
    try {
      DirectorAdapter adapter = null;
      if (adapterName == null || DirectorAdapter.DEFAULT_ADAPTER_NAME.equals(adapterName)) {
        adapter = (DirectorAdapter) director.getAttribute(DirectorAdapter.DEFAULT_ADAPTER_NAME, DirectorAdapter.class);
        if (adapter == null) {
          adapter = new DefaultDirectorAdapter(director, DirectorAdapter.DEFAULT_ADAPTER_NAME);
        }
        return adapter;
      } else {
        return (DirectorAdapter) director.getAttribute(adapterName, DirectorAdapter.class);
      }
    } catch (IllegalActionException e) {
      LOGGER.error("Internal error - failed to get DirectorAdapter", e);
      return NullDirectorAdapter.getInstance();
    } catch (NameDuplicationException e) {
      LOGGER.error("Internal error - failed to create DirectorAdapter", e);
      return NullDirectorAdapter.getInstance();
    }
  }

  /**
   * Finds the actors that are still active in the flow execution and that are potential sources for new work in the remaining active model branches or loops.
   * In models with loops, at least one actor in the loop will be present, potentially more/all of them.
   * <p>
   * Actors that are still actively iterating but without active input ports are considered as potential roots for the branches of actors that are connected to
   * their output ports. Active actors with active input ports are not considered, unless they are in a loop.
   * </p>
   * 
   * @param director
   * @return
   */
  public static Set<Actor> getRootActorsForActiveBranchesAndLoops(Director director) {
    Set<Actor> checkedActors = new HashSet<Actor>();
    Set<Actor> result = new HashSet<Actor>();

    DirectorAdapter adapter = DirectorUtils.getAdapter(director, null);
    Collection<Actor> activeActors = adapter.getActiveActors();
    for (Actor actor : activeActors) {
      LOGGER.debug("{} : Active actor", actor.getFullName());
      if (result.contains(actor) || checkedActors.contains(actor)) {
        continue;
      } else {
        checkBranchRootAndLoop((Actor) actor, result, checkedActors);
      }
    }
    LOGGER.debug("Found root/loop actors {}", result);
    return result;
  }

  /**
   * 
   * @param fromActor the actor from which we start looking backwards in the model to determine its branch's root actor
   * or whether the actor is in a loop.
   * @param result the set of all branch roots and loop actors found till now
   * @param checkedActors the set of all checked actors in the model
   * @return true if a root or loop actor was found and added in the result set
   */
  protected static boolean checkBranchRootAndLoop(Actor fromActor, Set<Actor> result, Set<Actor> checkedActors) {
    LOGGER.trace("isActorBranchRootOrInLoop() - entry : {}", fromActor);
    boolean addedOne = false;
    if (checkedActors.contains(fromActor)) {
      // actor is in a loop as it was checked already
      LOGGER.debug("{} : Actor is in a loop", fromActor.getFullName());
      addedOne = result.add(fromActor);
    } else {
      checkedActors.add(fromActor);
      boolean addThisOne = true;
      @SuppressWarnings("unchecked")
      List<Port> portList = fromActor.inputPortList();
      for (Port port : portList) {
        if (port instanceof com.isencia.passerelle.core.Port) {
          com.isencia.passerelle.core.Port p = (com.isencia.passerelle.core.Port) port;
          Set<Entity> activeSources = p.getActiveSources();
          if (!activeSources.isEmpty()) {
            // now find roots of this actor's branch or check if we're in a loop and may still need to include this port's actor
            for (Entity srcEntity : activeSources) {
              if (srcEntity instanceof Actor) {
                Actor srcActor = (Actor) srcEntity;
                if (result.contains(srcEntity)) {
                  // we don't need to include this srcEntity anymore as another actor before this one, in a same branch/loop, is already included
                  addThisOne = false;
                  break;
                } else if (checkBranchRootAndLoop(srcActor, result, checkedActors)) {
                  // we've found and added a preceeding actor in this branch, so no need to add this one anymore
                  addedOne = true;
                  addThisOne = false;
                  break;
                }
              }
            }
          }
          if (!addThisOne) {
            break;
          }
        }
      }
      if(addThisOne) {
        LOGGER.debug("{} : Found root/loop actor", fromActor.getFullName());
        addedOne = result.add(fromActor);
      }
    }
    LOGGER.trace("isActorBranchRootOrInLoop() - exit : {} - added a result : {}", fromActor, addedOne);
    return addedOne;
  }
}
