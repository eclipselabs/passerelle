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

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorGraphFrame;

//////////////////////////////////////////////////////////////////////////
//// GraphTableau
/**
   This is a graph editor for ptolemy models.  It constructs an instance
   of ActorGraphFrame, which contains an editor pane based on diva.

   @see ActorGraphFrame
   @author  Steve Neuendorffer and Edward A. Lee
   @version $Id: ActorGraphTableau.java,v 1.3 2005/10/28 14:07:19 erwin Exp $
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (neuendor)
   @Pt.AcceptedRating Red (johnr)
*/
public class ActorGraphTableau extends Tableau {

	ModelGraphPanel graphPanel = null;
	
    /** Create a tableau in the specified workspace.
     *  @param workspace The workspace.
     */
    public ActorGraphTableau(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Create a tableau with the specified container and name, with
     *  no specified default library.
     *  @param container The container.
     *  @param name The name.
     */
    public ActorGraphTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a tableau with the specified container, name, and
     *  default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     */
    public ActorGraphTableau(
            PtolemyEffigy container,
            String name,
            LibraryAttribute defaultLibrary)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        if (container instanceof PtolemyEffigy) {
            NamedObj model = container.getModel();
            if (model == null) {
                return;
            }
            if (!(model instanceof CompositeEntity)) {
                throw new IllegalActionException(this,
                        "Cannot graphically edit a model "
                        + "that is not a CompositeEntity. Model is a "
                        + model);
            }
            CompositeEntity entity = (CompositeEntity)model;

//            ActorGraphFrame frame = new ActorGraphFrame(
//                    entity, this, defaultLibrary);
//            setFrame(frame);
//            frame.setBackground(BACKGROUND_COLOR);
            
            setGraphPanel(new ModelGraphPanel(entity, container));
        }
    }

	/**
	 * @return Returns the graphPanel.
	 */
	public ModelGraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * @param graphPanel The graphPanel to set.
	 */
	public void setGraphPanel(ModelGraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The background color.
//    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create an factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create a tableau in the default workspace with no name for the
         *  given Effigy.  The tableau will created with a new unique name
         *  in the given model effigy.  If this factory cannot create a tableau
         *  for the given effigy (perhaps because the effigy is not of the
         *  appropriate subclass) then return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new ActorGraphTableau, if the effigy is a
         *  PtolemyEffigy, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *  tableau.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a graphTableau.
                ActorGraphTableau tableau =
                    (ActorGraphTableau)effigy.getEntity("graphTableau");
                if (tableau == null) {
                    // Check to see whether this factory contains a
                    // default library.
                    LibraryAttribute library = (LibraryAttribute)getAttribute(
                            "_library", LibraryAttribute.class);
                    tableau = new ActorGraphTableau(
                            (PtolemyEffigy)effigy, "graphTableau", library);
                }
                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }

}
