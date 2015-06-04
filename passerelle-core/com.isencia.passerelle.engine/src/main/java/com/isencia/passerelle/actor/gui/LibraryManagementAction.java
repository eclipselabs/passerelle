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

import java.awt.event.ActionEvent;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;

/** 
 * An action that works on the library or actors.
 */
public abstract class LibraryManagementAction extends FigureAction {

    private LibraryManager libraryManager;
    private Configuration configuration;
    
	/** Create a new action to save a model in a file.
     */
    public LibraryManagementAction(String name, String tooltip) {
        super(name);
        putValue("tooltip", tooltip);
        this.libraryManager = new LibraryManager(null);
    }

    /** Save the target object in a file.
     *  @param event The action event.
     */
    public final void actionPerformed(ActionEvent event) {
        // Find the target.
        super.actionPerformed(event);
        NamedObj object = getTarget();
        if (object instanceof Entity) {
            Entity entity = (Entity)object;

            actionPerformedForEntity(entity);
        }
    }

	protected abstract void actionPerformedForEntity(Entity entity);

	
	protected final LibraryManager getLibraryManager() {
		return libraryManager;
	}

	public final Configuration getConfiguration() {
		return configuration;
	}

	public final void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
        libraryManager.refreshManagerCache(configuration);
	}
}