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

import javax.swing.BoxLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.gui.LibraryManager;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.Query;
import ptolemy.moml.EntityLibrary;

/**
 * Panel that allows to pick a library name from a drop-down list,
 * and save a composite actor into it.
 * 
 * @author erwin
 */
public class AddFolderToLibraryConfigurer extends Query {
	private static final String FOLDER_NAME = "Folder name";
	
	private final static Logger logger = LoggerFactory.getLogger(AddFolderToLibraryConfigurer.class);
	
	private EntityLibrary _parentLibrary;
	private Configuration _configuration;
	private LibraryManager libraryManager;

	/**
	 * 
	 * @param configuration
	 * @param actor
	 */
	public AddFolderToLibraryConfigurer(Configuration configuration, EntityLibrary parentLibrary) {
		super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(25);
        _parentLibrary = parentLibrary;
        _configuration = configuration;
        libraryManager = new LibraryManager(_configuration);
        addLine(FOLDER_NAME, FOLDER_NAME, "new folder");
	}

	/**
	 * @param configuredEntities
	 */

	/**
	 * @throws Exception 
	 * 
	 */
    public void save() throws Exception {
    	String folderName = getStringValue(FOLDER_NAME);
    	libraryManager.addSubLibrary(_parentLibrary,folderName);
    }
}
