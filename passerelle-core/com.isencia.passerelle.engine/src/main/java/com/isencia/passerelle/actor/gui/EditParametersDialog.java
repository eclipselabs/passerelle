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

import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// EditParametersDialog
/**
This class is a modal dialog box for editing the parameters of a target
object, which is an instance of NamedObj. All attributes that implement
the Settable interface and have visibility FULL are included in the
dialog. An instance of this class contains an instance of
Configurer, which examines the target for attributes of type
EditorPaneFactory.  Those attributes, if they are present, define
the panels that are used to edit the parameters of the target.
If they are not present, then a default panel is created.
<p>
If the panels returned by EditorPaneFactory implement the
CloseListener interface, then they are notified when this dialog
is closed, and are informed of which button (if any) was used to
close the dialog.
<p>
The dialog is modal, so the statement that creates the dialog will
not return until the user dismisses the dialog.  The method buttonPressed()
can then be called to find out whether the user clicked the Commit button
or the Cancel button (or any other button specified in the constructor).
Then you can access the component to determine what values were set
by the user.

@author Edward A. Lee
@version $Id: EditParametersDialog.java,v 1.3 2005/10/28 14:07:19 erwin Exp $
*/
public class EditParametersDialog extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  A "Commit" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     */
    public EditParametersDialog(Frame owner, NamedObj target) {
        this(owner, target, "Edit parameters for " + target.getName());
    }

    public EditParametersDialog(Frame owner, NamedObj target, String title) {
        super(owner,title,new PasserelleConfigurer(target),_moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _owner = owner;
        _target = target;
	}

	/** Notify the listener that a change has been successfully executed.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change.getSource() != this) return;

        // Open a new dialog.
        new EditParametersDialog(_owner, _target);

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change.getSource() != this) return;

        _target.removeChangeListener(this);

        //String newName = _query.stringValue("name");
/*        ComponentDialog dialog = _openAddDialog(exception.getMessage()
                + "\n\nPlease enter a new default value:",
                newName,
                _query.stringValue("default"),
                _query.stringValue("class"));
        _target.removeChangeListener(this);
        if (!dialog.buttonPressed().equals(_moreButtons[0])) {
            // Remove the parameter, since it seems to be erroneous
            // and the user hit cancel or close.
            String moml = "<deleteProperty name=\"" + newName + "\"/>";
            _target.requestChange(
                    new MoMLChangeRequest(this, _target, moml));
        }*/
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the contents of this dialog implements the CloseListener
     *  interface, then notify it that the window has closed.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals(_moreButtons[0])) {
            // Restore original parameter values.
            ((PasserelleConfigurer)contents).restore();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Button labels.
    private static String[] _moreButtons
            = {"Ok", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The target object whose parameters are being edited.
    private NamedObj _target;
}



