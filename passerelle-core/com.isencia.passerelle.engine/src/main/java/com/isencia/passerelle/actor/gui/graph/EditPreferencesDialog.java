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
package com.isencia.passerelle.actor.gui.graph;

import java.awt.Frame;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.ComponentDialog;

/**
 This class is a modal dialog box for showing/hiding all port names of the
 actors in a model graph.

 @author erwin
 */
public class EditPreferencesDialog extends ComponentDialog {
	
    /** Construct a dialog with the specified owner and target.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog, or null if none.
     *  @param target The object being renamed.
     */
    public EditPreferencesDialog(Frame owner, Configuration configuration) {
        super(owner, "Edit preferences", new EditPreferencesConfigurer(configuration), _buttons);
    }

	/** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();

        if (!buttonPressed().equals("Cancel")) {
            ((EditPreferencesConfigurer) contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Button labels.
    private static String[] _buttons = { "Ok", "Cancel" };
}
