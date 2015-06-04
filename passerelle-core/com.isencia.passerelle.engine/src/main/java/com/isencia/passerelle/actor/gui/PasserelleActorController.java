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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PortConfigurerDialog;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.CustomizeDocumentationAction;
import ptolemy.vergil.basic.IconController;
import ptolemy.vergil.basic.RemoveCustomDocumentationAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.MoveAction;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.graph.EditorGraphController;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.actor.gui.graph.ViewOpener;
import diva.graph.GraphController;
import diva.graph.NodeInteractor;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JContextMenu;

/**
 * PasserelleActorController
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PasserelleActorController extends IconController {
    private final static Logger logger = LoggerFactory.getLogger(PasserelleActorController.class);

    /** The action that handles look inside.  This is accessed by
     *  by ActorViewerController to create a hot key for the editor.
     */
    protected LookInsideAction _lookInsideAction;
    protected SaveInLibraryAction _saveInLibraryAction;
    
    /**
     * @param controller
     */
    public PasserelleActorController(GraphController controller) {
        this(controller,FULL);
    }

    /**
     * @param controller
     * @param access
     */
    public PasserelleActorController(GraphController controller, Access access) {
        super(controller);
        
        Action[] actions = { _getDocumentationAction,
                new CustomizeDocumentationAction(),
                new RemoveCustomDocumentationAction() };
        _menuFactory.addMenuItemFactory(new MenuActionFactory(actions,
                "Documentation"));

        // Note that we also have "Send to Back" and "Bring to Front" in
        // vergil/basic/BasicGraphFrame.java
        Action[] appearanceActions = {
                new MoveAction("Send to Back", MoveAction.TO_FIRST),
                new MoveAction("Bring to Front", MoveAction.TO_LAST) };
        _appearanceMenuActionFactory = new MenuActionFactory(
                appearanceActions, "Appearance");
        _menuFactory.addMenuItemFactory(_appearanceMenuActionFactory);

        
        _lookInsideAction = new LookInsideAction(this);
        _saveInLibraryAction = new SaveInLibraryAction();
        ((NodeInteractor) getNodeInteractor()).setDragInteractor(new LocatableNodeDragInteractor(this));
        if (access == FULL) {
            _menuFactory.addMenuItemFactory(
            		new PortDialogFactory());

            _menuFactory.addMenuItemFactory(
                    new CompositeActorMenuItemFactory(this));
        }
        
    }

    /**
     * Sample code to show/hide control ports
     * for expert mode or when connected.
     * Needs finishing touches in port layout.
     * For the moment, when control ports are all hidden,
     * the input/error/output ports get suddenly at the top corners,
     *  which is not nice.
     */
//    protected void _drawChildren(Object node) {
//        GraphModel model = getController().getGraphModel();
//
//        if (model.isComposite(node)) {
//            Iterator children = model.nodes(node);
//
//            while (children.hasNext()) {
//                Object child = children.next();
//                boolean drawIt = true;
//                if(child instanceof IOPort) {
//                	IOPort port = (IOPort) child;
//	    			boolean isExpertMode = false;
//	    			if(port.getContainer() instanceof Actor) {
//	    				Actor containingActor = (Actor) port.getContainer();
//	    				try {
//	    					isExpertMode = ((Director)containingActor.getDirector()).isExpertMode();
//	    				} catch (Exception e) {}
//	    			}
//	    			boolean portIsConnected = port.getWidth() > 0;
//	    			
//	    			if(port instanceof ControlPort) {
//	    				if(!portIsConnected && !isExpertMode)
//	    					drawIt = false;
//	    			}
//                }
//    			
//                if(drawIt)
//                	getController().drawNode(child, node);
//            }
//        }
//    }

    /**
     * Facade method
     * 
     * @param editMsg
     */
    public void postUndoableEdit(String editMsg) {
        ((EditorGraphController)getController()).postUndoableEdit(editMsg);
    }

    /**
     * Snapshot taken from Ptolemy 4.0.1, adapted to disallow port configuration
     * for Passerelle actors.
     * Adding/removing ports etc in an uncontrolled fashion can break the actor's operation.
     * Passerelle actors that support this, will explicitly contain config parameters to set
     * the nr of ports.
     * 
     * We only allow this uncontrolled port configuration for plain Ptolemy actors.
     * 
     * @author erwin
     */
    private static class PortDialogFactory implements MenuItemFactory {

        ///////////////////////////////////////////////////////////////////
        //// public methods ////

        /**
         * Add an item to the given context menu that will open a dialog to add or
         * remove ports from an object.
         *
         * @param menu
         *            The context menu.
         * @param object
         *            The object whose ports are being manipulated.
         */
        public JMenuItem create(final JContextMenu menu, NamedObj object) {
            JMenuItem retv = null;
            // Removed this method since it was never used. EAL
            // final NamedObj target = _getItemTargetFromMenuTarget(object);
            final NamedObj target = object;

            // Ensure that we actually have a target, and that it's an Entity,
            // but not a Passerelle actor.
            if (target==null || !(target instanceof Entity) || (target instanceof Actor))
                return null;
            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            // Normally, this is a Frame, but just in case, we check.
            // If it isn't a Frame, then the edit parameters dialog
            // will not have the appropriate parent, and will disappear
            // when put in the background.
            // Note, this uses the "new" way of doing dialogs.
            Action configPortsAction = new AbstractAction(_configPorts) {

                public void actionPerformed(ActionEvent e) {
                    Component parent = menu.getInvoker();
                    ModelGraphPanel passerelleGraphPanel = null;
                    while (parent.getParent() != null) {
                    	if(parent instanceof ModelGraphPanel) {
                    		passerelleGraphPanel = (ModelGraphPanel) parent;
                    	}
                        parent = parent.getParent();
                    }
                    // this if for the Passerelle IDE
                    if (passerelleGraphPanel != null) {
                        DialogTableau dialogTableau =
                            DialogTableau.createDialog(
                                (Frame) parent,
                                _configuration,
                                passerelleGraphPanel.getEffigy(),
                                PortConfigurerDialog.class,
                                (Entity) target);
                        if (dialogTableau != null) {
                            dialogTableau.show();
                        }
                    }
                    // this should keep it working in Vergil
                    else if (parent instanceof TableauFrame) {
                        DialogTableau dialogTableau =
                            DialogTableau.createDialog(
                                (Frame) parent,
                                _configuration,
                                ((TableauFrame) parent).getEffigy(),
                                PortConfigurerDialog.class,
                                (Entity) target);
                        if (dialogTableau != null) {
                            dialogTableau.show();
                        }
                    }
                }
            };
            retv = menu.add(configPortsAction, _configPorts);

            return retv;
        }

        /**
         * Set the configuration for use by the help screen.
         *
         * @param configuration
         *            The configuration.
         */
        public void setConfiguration(Configuration configuration) {
            _configuration = configuration;
        }

        ///////////////////////////////////////////////////////////////////
        //// private variables ////

        /** The configuration. */
        private static String _configPorts = "Configure Ports";

//        private static String _configUnits = "Configure Units";

        private Configuration _configuration;
    }


    private class CompositeActorMenuItemFactory implements MenuItemFactory {
    	private PasserelleActorController ctrlr;

        public CompositeActorMenuItemFactory(
				PasserelleActorController ctrlr) {
            this.ctrlr = ctrlr;
		}

		public JMenuItem create(final JContextMenu menu, NamedObj object) {
            JMenuItem retv = null;
            
            if (object != null && object instanceof CompositeEntity) {
            	_saveInLibraryAction.setConfiguration(ctrlr._configuration);
                retv = menu.add(_lookInsideAction,"Look Inside");
//                retv = menu.add(_saveInLibraryAction,"Save In Library");
            }
            return retv;
        }
        
    }
    
    // An action to look inside a composite.
    private static class LookInsideAction extends FigureAction {

    	private PasserelleActorController ctrlr;


        public LookInsideAction(PasserelleActorController ctrlr) {
            super("Look Inside");
            this.ctrlr = ctrlr;
            // For some inexplicable reason, the I key doesn't work here.
            // Use L, which used to be used for layout.
            // Avoid Control_O, which is open file.
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent event) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            if (ctrlr._configuration == null) {
            	Object topLevel = target.toplevel();
            	if(topLevel instanceof Configuration)
            		ctrlr._configuration = (Configuration) topLevel;
            	else {
            		ctrlr._configuration = new Configuration(target.workspace());
            		try {
						new ModelDirectory(ctrlr._configuration, Configuration._DIRECTORY_NAME);
					} catch (Exception e) {
		                MessageHandler.error("Cannot look inside without a configuration.");
		                return;
					}
            	}
            }

            try {
                GraphController gCtrlr = ctrlr.getController();
                ((ViewOpener)gCtrlr).openView(target, ctrlr._configuration);
            } catch (Exception e) {
                MessageHandler.error(
                        "Cannot open view for looking inside.",e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is used in derived classes to
     *  to open files (such as documentation).  The configuration is
     *  is important because it keeps track of which files are already
     *  open and ensures that there is only one editor operating on the
     *  file at any one time.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _getDocumentationAction.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public members                        ////

    /** Indicator to give full access to the attribute. */
    public static final Access FULL = new Access();

    /** Indicator to give partial access to the attribute. */
    public static final Access PARTIAL = new Access();

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The appearance menu factory. */
    protected MenuActionFactory _appearanceMenuActionFactory;

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** The "get documentation" action. */
    private GetDocumentationAction _getDocumentationAction = new GetDocumentationAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A static enumerator for constructor arguments. */
    protected static class Access {
    }

}
