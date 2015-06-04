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
import com.isencia.passerelle.actor.gui.LibraryManager;


import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// RenameConfigurer

/**
 This class is an editor widget to rename an object.

 @see Configurer
 */
public class RenameConfigurer extends Query implements ChangeListener,
        QueryListener {

    // Indicator that the name has changed.
    private boolean _changed = false;

    // The object that this configurer configures.
    private EntityLibrary library;
    
	private Configuration _configuration;
	private LibraryManager libraryManager;

	/** Construct a rename configurer for the specified entity.
     *  @param library The entity to configure.
     */
    public RenameConfigurer(Configuration configuration, EntityLibrary library) {
        super();
        _configuration = configuration;
        libraryManager = new LibraryManager(_configuration);
        this.library = library;
        
        this.addQueryListener(this);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setTextWidth(25);
        addLine("New name", "New name", library.getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the changes by renaming the object.
     */
    public void apply() {
        if (_changed) {
            String newName = getStringValue("New name");
            libraryManager.renameLibrary(library, newName);
        }
    }

    /** React to the fact that the change has been successfully executed
     *  by doing nothing.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Nothing to do.
    }

    /** React to the fact that the change has failed by reporting it.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if ((change != null) && (change.getSource() != this)) {
            return;
        }

        if ((change != null) && !change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Rename failed: ", exception);
        }
    }

    /** Called to notify that one of the entries has changed.
     *  This simply sets a flag that enables application of the change
     *  when the apply() method is called.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        _changed = true;
    }

}
