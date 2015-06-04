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
package com.isencia.passerelle.actor.gui.graph.userlib;

import java.awt.event.ActionEvent;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;


import ptolemy.kernel.Entity;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.toolbox.FigureAction;

class DeleteFromLibraryAction extends FigureAction {

    private ModelGraphPanel panel;

	public DeleteFromLibraryAction(ModelGraphPanel panel) {
        super("Delete from User Library");
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        // Determine which entity was selected for the delete action.
        super.actionPerformed(event);
        Entity target = (Entity) getTarget();
        if(target.getContainer() instanceof EntityLibrary) {
        	// should always be the case if the menu is correctly set-up...
        	panel.getLibraryManager().deleteEntityFromLibrary((EntityLibrary) target.getContainer(), (Entity) target);
        }
    }
}