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
import java.io.File;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.util.swing.components.FinderAccessory;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.ExtensionFileFilter;

class ImportClassAction extends FigureAction {
	private final static Logger logger = LoggerFactory.getLogger(ImportClassAction.class);
	
	private ModelGraphPanel panel;
	
    public ImportClassAction(ModelGraphPanel panel) {
        super("Import shared actor");
        this.panel=panel;
    }

    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        NamedObj target = getTarget();
        if(target instanceof EntityLibrary) {
        	EntityLibrary lib = (EntityLibrary) target;
            File actorFile=null;
            
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Import shared actor from...");
           	fileChooser.setCurrentDirectory(EnvironmentUtils.getUserRelevantDirectory());
			fileChooser.setAccessory(new FinderAccessory(fileChooser));
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter(new String[] { "xml", "moml" }, "Passerelle model files"));
			int returnVal = fileChooser.showOpenDialog(panel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				actorFile = fileChooser.getSelectedFile();
				
				if(logger.isInfoEnabled())
					logger.info("Importing actor in Library "+lib.getFullName()+" from "+actorFile.getPath());
				
				EnvironmentUtils.setLastSelectedDirectory(fileChooser.getCurrentDirectory());
				MoMLParser parser = new MoMLParser();
				try {
					Entity e = (Entity) parser.parse(null, actorFile.toURI().toURL());
					panel.getLibraryManager().saveEntityInLibrary(lib, e);
				} catch (Exception e) {
					MessageHandler.error("Failed to load actor", e);
				}

			}
        } else {
        	MessageHandler.error("Selected item is not an actor library");
        }
    }
}