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

import java.awt.Frame;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.Entity;
import ptolemy.util.MessageHandler;

/**
 * SaveInLibraryDialog
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class SaveInLibraryDialog extends ComponentDialog {

	/**
	 * @param owner
	 * @param title
	 * @param component
	 */
	public SaveInLibraryDialog(Frame owner, Configuration configuration, Entity target) {
        super(owner,
                "Save " + target.getName(),
                new SaveInLibraryConfigurer(configuration, target),
                _buttons);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals("Cancel")) {
            try {
				((SaveInLibraryConfigurer)contents).save();
			} catch (Exception e) {
				MessageHandler.error("Failed to save in library", e);
			}
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Button labels.
    private static String[] _buttons = {"Save", "Cancel"};

}
