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
package com.isencia.passerelle.actor.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import diva.graph.GraphController;

/**
 * A controller factory that creates a Passerelle custom actor
 * controller that creates a pop-up-menu with following actions:
 * - rename
 * - configure
 * - get docs
 * 
 * @author erwin
 */
public class PasserelleActorControllerFactory extends NodeControllerFactory {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public PasserelleActorControllerFactory(NamedObj container, String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
    }

    /* (non-Javadoc)
     * @see ptolemy.vergil.basic.NodeControllerFactory#create(diva.graph.GraphController)
     */
    public NamedObjController create(GraphController controller) {
        return new PasserelleActorController(controller);
    }
}
