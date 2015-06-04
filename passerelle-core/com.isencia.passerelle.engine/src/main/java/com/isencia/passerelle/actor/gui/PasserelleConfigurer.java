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

import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.domain.cap.Director;

import ptolemy.actor.gui.EditorPaneFactory;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**
 * Overrides the check on parameter visibility.
 * We also support a model-wide expert mode on the Director.
 * 
 * @author erwin
 */
public class PasserelleConfigurer extends JPanel implements CloseListener {

	/**
	 * @param object
	 */
	public PasserelleConfigurer(NamedObj object) {
        
        if(object!=null && Actor.class.isInstance(object)) {
            // Passerelle Actor's can add more dynamic parameter configuration
            // e.g. using OptionFactories
            // We give the Actor the opportunity here to prepare parameter settings...
            configureParameters((Actor) object);
        }
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _object = object;
        // Record the original values so a restore can happen later.
        _originalValues = new HashMap();
        Iterator parameters = _object.attributeList(Settable.class).iterator();
        while (parameters.hasNext()) {
            Settable parameter = (Settable)parameters.next();
            if (isVisible(_object, parameter)) {
                _originalValues.put(parameter, parameter.getExpression());
            }
        }
        boolean foundOne = false;
        Iterator editors
            = object.attributeList(EditorPaneFactory.class).iterator();
        while (editors.hasNext()) {
            foundOne = true;
            EditorPaneFactory editor = (EditorPaneFactory)editors.next();
            Component pane = editor.createEditorPane();
            add(pane);
            // Inherit the background color from the container.
            pane.setBackground(null);
            if (pane instanceof CloseListener) {
                _closeListeners.add(pane);
            }
        }
        if (!foundOne) {
            // There is no attribute of class EditorPaneFactory.
            // We cannot create one because that would have to be done
            // as a mutation, something that is very difficult to do
            // while constructing a modal dialog.  Synchronized interactions
            // between the thread in which the manager performs mutations
            // and the event dispatch thread prove to be very tricky,
            // and likely lead to deadlock.  Hence, instead, we use
            // the static method of EditorPaneFactory.
            Component pane = EditorPaneFactory.createEditorPane(object);
            add(pane);
            // Inherit the background color from the container.
            pane.setBackground(null);
            if (pane instanceof CloseListener) {
                _closeListeners.add(pane);
            }
        }
	}
	
    /**
     * We don't put this as a method on Actor,
     * as this kind of parameter configuration
     * is only useful in the context of the UI
     * parameter pop-ups...
     */
    private void configureParameters(Actor actor) {
        if(actor.getOptionsFactory()!=null) {
            List parameters = actor.attributeList(Parameter.class);
            for (Iterator iter = parameters.iterator(); iter.hasNext();) {
                Parameter p = (Parameter) iter.next();
                actor.getOptionsFactory().setOptionsForParameter(p);
            }
        }
    }
    


	    /** Request restoration of the user settable attribute values to what they
	     *  were when this object was created.  The actual restoration
	     *  occurs later, in the UI thread, in order to allow all pending
	     *  changes to the attribute values to be processed first. If the original
	     *  values match the current values, then nothing is done.
	     */
	    public void restore() {
	        // This is done in the UI thread in order to
	        // ensure that all pending UI events have been
	        // processed.  In particular, some of these events
	        // may trigger notification of new attribute values,
	        // which must not be allowed to occur after this
	        // restore is done.  In particular, the default
	        // attribute editor has lines where notification
	        // of updates occurs when the line loses focus.
	        // That notification occurs some time after the
	        // window is destroyed.
	        // FIXME: Unfortunately, this gets
	        // invoked before that notification occurs if the
	        // "X" is used to close the window.  Swing bug?
	        SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    // First check for changes.
	                    Iterator parameters = _object.attributeList(Settable.class).iterator();
	                    boolean hasChanges = false;
	                    StringBuffer buffer = new StringBuffer("<group>\n");
	                    while (parameters.hasNext()) {
	                        Settable parameter = (Settable)parameters.next();
	                        if (isVisible(_object, parameter)) {
	                            String newValue = parameter.getExpression();
	                            String oldValue = (String)_originalValues.get(parameter);
	                            if (!newValue.equals(oldValue)) {
	                                hasChanges = true;
	                                buffer.append("<property name=\"");
	                                buffer.append(((NamedObj)parameter).getName(_object));
	                                buffer.append("\" value=\"");
	                                buffer.append(StringUtilities.escapeForXML(oldValue));
	                                buffer.append("\"/>\n");
	                            }
	                        }
	                    }
	                    buffer.append("</group>\n");

	                    // If there are changes, then issue a change request.
	                    // Use a MoMLChangeRequest so undo works... I.e., you can undo a cancel
	                    // of a previous change.
	                    if (hasChanges) {
	                        MoMLChangeRequest request = new MoMLChangeRequest(
	                                this,              // originator
	                                _object,           // context
	                                buffer.toString(), // MoML code
	                                null);             // base
	                        _object.requestChange(request);
	                    }
	                }
	            });
	    }

	    /** Restore parameter values to their defaults.
	     */
	    public void restoreToDefaults() {
	        // This is done in the UI thread in order to
	        // ensure that all pending UI events have been
	        // processed.  In particular, some of these events
	        // may trigger notification of new attribute values,
	        // which must not be allowed to occur after this
	        // restore is done.  In particular, the default
	        // attribute editor has lines where notification
	        // of updates occurs when the line loses focus.
	        // That notification occurs some time after the
	        // window is destroyed.
	        SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    Iterator parameters = _object.attributeList(Settable.class).iterator();
	                    StringBuffer buffer = new StringBuffer("<group>\n");
	                    final List parametersReset = new LinkedList();
	                    while (parameters.hasNext()) {
	                        Settable parameter = (Settable)parameters.next();
	                        if (isVisible(_object, parameter)) {
	                            String newValue = parameter.getExpression();
	                            String defaultValue = parameter.getDefaultExpression();
	                            if (defaultValue != null && !newValue.equals(defaultValue)) {
	                                buffer.append("<property name=\"");
	                                buffer.append(((NamedObj)parameter).getName(_object));
	                                buffer.append("\" value=\"");
	                                buffer.append(StringUtilities.escapeForXML(defaultValue));
	                                buffer.append("\"/>\n");
	                                parametersReset.add(parameter);
	                            }
	                        }
	                    }
	                    buffer.append("</group>\n");

	                    // If there are changes, then issue a change request.
	                    // Use a MoMLChangeRequest so undo works... I.e., you can undo a cancel
	                    // of a previous change.
	                    if (parametersReset.size() > 0) {
	                        MoMLChangeRequest request = new MoMLChangeRequest(
	                                this,              // originator
	                                _object,           // context
	                                buffer.toString(), // MoML code
	                                null) {            // base
	                            protected void _execute() throws Exception {
	                                super._execute();
	                                // Reset the derived level, which has the side
	                                // effect of marking the object not overridden.
	                                Iterator parameters = parametersReset.iterator();
	                                while (parameters.hasNext()) {
	                                    Settable parameter = (Settable)parameters.next();
	                                    if (isVisible(_object, parameter)) {
	                                        int derivedLevel
	                                                = ((NamedObj)parameter).getDerivedLevel();
	                                        ((NamedObj)parameter).setDerivedLevel(derivedLevel);
	                                    }
	                                }
	                            }
	                        };
	                        _object.requestChange(request);
	                    }
	                }
	            });
	    }


    /** Return true if the given settable should be visible in a
     *  configurer panel for the specified target. Any settable with
     *  visibility FULL or NOT_EDITABLE will be visible.  If the target
     *  contains an attribute named "_expertMode", then any
     *  attribute with visibility EXPERT will also be visible.
     *  @param target The object to be configured.
     *  @param settable The object whose visibility is returned.
     */
    public static boolean isVisible(NamedObj target, Settable settable) {
        if (settable.getVisibility() == Settable.FULL
                || settable.getVisibility() == Settable.NOT_EDITABLE) {
            return true;
        }
        if (target.getAttribute("_expertMode") != null
                && settable.getVisibility() == Settable.EXPERT) {
            return true;
        }
        if((settable.getVisibility() == Settable.EXPERT) && (target instanceof Actor)) {
        	Actor actorTarget = (Actor) target;
        	try {
        		return actorTarget.getDirectorAdapter().isExpertMode();
        	} catch (Exception e) {
        		// means we're not using a Passerelle director
        		// just return false then
        	}
        }
        return false;
    }
	
    /** Notify any panels in this configurer that implement the
     *  CloseListener interface that the specified window has closed.
     *  The second argument, if non-null, gives the name of the button
     *  that was used to close the window.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        Iterator listeners = _closeListeners.iterator();
        while (listeners.hasNext()) {
            CloseListener listener = (CloseListener)listeners.next();
            listener.windowClosed(window, button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of panels in this configurer that implement CloseListener,
    // if there are any.
    private List _closeListeners = new LinkedList();

    // The object that this configurer configures.
    private NamedObj _object;

    // A record of the original values.
    private HashMap _originalValues;

}
