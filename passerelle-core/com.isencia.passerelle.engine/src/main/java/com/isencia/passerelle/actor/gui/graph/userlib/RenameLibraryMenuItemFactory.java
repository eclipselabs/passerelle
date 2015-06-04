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

import javax.swing.Action;
import javax.swing.JMenuItem;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;


import diva.gui.toolbox.JContextMenu;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.toolbox.MenuItemFactory;

public class RenameLibraryMenuItemFactory implements MenuItemFactory {

	private ModelGraphPanel panel;

	public RenameLibraryMenuItemFactory(ModelGraphPanel panel) {
		this.panel=panel;
	}

	public JMenuItem create(JContextMenu menu, NamedObj target) {
    	if((target instanceof EntityLibrary
    					&& panel.getUserLibrary()!=null && panel.getUserLibrary().deepContains(target))) {
            Action action = new RenameLibraryFolderAction(panel);
            action.putValue("tooltip", "Rename");
            return menu.add(action, (String)action.getValue(Action.NAME));
    	} else {
    		return null;
    	}
	}

}
