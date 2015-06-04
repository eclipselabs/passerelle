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

import java.awt.Frame;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;

//////////////////////////////////////////////////////////////////////////
//// RenameDialog

/**
 This class is a modal dialog box for renaming an object.
 The dialog is modal, so the statement that creates the dialog will
 not return until the user dismisses the dialog.

 @author Edward A. Lee
 @version $Id: RenameDialog.java,v 1.20 2005/07/08 19:55:47 cxh Exp $
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class RenameDialog extends ComponentDialog {
    /** Construct a dialog with the specified owner and target.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog, or null if none.
     *  @param target The object being renamed.
     */
    public RenameDialog(Frame owner, Configuration configuration, EntityLibrary target) {
        super(owner, "Rename " + target.getName(),
                new RenameConfigurer(configuration, target), _buttons);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();

        if (!buttonPressed().equals("Cancel")) {
            ((RenameConfigurer) contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Button labels.
    private static String[] _buttons = { "Ok", "Cancel" };
}
