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

import ptolemy.kernel.Entity;
import ptolemy.util.MessageHandler;

/** An action to save this actor in a file.
 */
public class SaveInFileAction extends LibraryManagementAction {
	/** Create a new action to save a model in a file.
     */
    public SaveInFileAction() {
        super("Export to File", "Exports the model to a file, so it can be exchanged with other users.");
    }

    /** Save the target object in a file.
     *  @param event The action event.
     */
    protected void actionPerformedForEntity(Entity entity) {
        try {
        	getLibraryManager().exportEntityToClassFile(entity);
        } catch (Exception e) {
            MessageHandler.error("Save failed.", e);
        }
    }
}