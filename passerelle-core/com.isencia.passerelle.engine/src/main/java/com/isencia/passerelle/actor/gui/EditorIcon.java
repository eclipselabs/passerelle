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
/* An Icon is the graphical representation of an entity.

 Copyright (c) 1999-2001 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

										PT_COPYRIGHT_VERSION_2
										COPYRIGHTENDKEY

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/
package com.isencia.passerelle.actor.gui;


import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.vergil.icon.XMLIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;

/**
 * EditorIcon
 * 
 * An adapted icon, that puts the name above the icon, i.o. on top of it.
 * 
 * @author erwin
 */
public class EditorIcon extends XMLIcon {

	private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
    private static Font _parameterFont = new Font("SansSerif", Font.PLAIN, 9);

	/**
	 * Create a new icon with the given name in the given container.
	 * @param container The container.
	 * @param name The name of the attribute.
	 * @exception IllegalActionException If the attribute is not of an
	 *  acceptable class for the container.
	 * @exception NameDuplicationException If the name coincides with
	 *  an attribute already in the container.
	 */
	public EditorIcon(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

//	/**
//	 * Create a new Diva figure that visually represents this icon.
//	 * The figure will be an instance of
//	 * CompositeFigure with the figure returned by createBackgroundFigure
//	 * as its background.  This method adds a LabelFigure to the
//	 * CompositeFigure that contains the name of the container of this icon.
//	 * Subclasses of this
//	 * class should never return null, even if the icon has not been properly
//	 * initialized.
//	 * @return A new CompositeFigure.
//	 */
//	public Figure createFigure() {
//	Figure background = createBackgroundFigure();
//	Rectangle2D backBounds = background.getBounds();
//		Figure figure = new CompositeFigure(background);
//		Nameable container = getContainer();
//		// FIXME this is a bad way to do this.
//		if(!(container instanceof Attribute) ||
//				container instanceof ptolemy.actor.Director) {
//			LabelFigure label = new LabelFigure(container.getName(),
//					_labelFont, 1.0, SwingConstants.SOUTH_WEST);
//			label.translateTo(backBounds.getX(), backBounds.getY()-3);
//			((CompositeFigure)figure).add(label);
//		}
//	return figure;
//	}
	
    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of CompositeFigure with the
     *  figure returned by createBackgroundFigure() as its background.
     *  This method adds a LabelFigure to the CompositeFigure that
     *  contains the name of the container of this icon, unless the
     *  container has a parameter called "_hideName" with value true.
     *  If the container has an attribute called "_centerName" with
     *  value true, then the name is rendered
     *  in the center of the background figure, rather than above it.
     *  This method should never return null, even if the icon has
     *  not been properly initialized.
     *  @return A new CompositeFigure consisting of the background figure
     *   and a label.
     */
    public Figure createFigure() {
        Figure background = createBackgroundFigure();
        Rectangle2D backBounds = background.getBounds();
        CompositeFigure figure = new CompositeFigure(background);

        NamedObj container = (NamedObj) getContainerOrContainerToBe();

        // Create the label, unless this is a visible attribute,
        // which typically carries no label.
        // NOTE: backward compatibility problem...
        // Old style annotations now have labels...
        if (!_isPropertySet(container, "_hideName")) {
            String name = container.getDisplayName();

            // Do not add a label figure if the name is null.
            if ((name != null) && !name.equals("")) {
                if (!_isPropertySet(container, "_centerName")) {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.SOUTH_WEST);

                    // Shift the label slightly right so it doesn't
                    // collide with ports.
                    label.translateTo(backBounds.getX() + 5, backBounds.getY());
                    figure.add(label);
                } else {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.CENTER);
                    label.translateTo(backBounds.getCenterX(), backBounds
                            .getCenterY());
                    figure.add(label);
                }
            }
        }

        // If specified by a preference, then show parameters.
        Token show = PtolemyPreferences.preferenceValueLocal(container,
                "_showParameters");

        if (show instanceof StringToken) {
            String value = ((StringToken) show).stringValue();
            boolean showOverriddenParameters = value
                    .equals("Overridden parameters only");
            boolean showAllParameters = value.equals("All");

            if ((showOverriddenParameters && !_isPropertySet(container,
                    "_hideAllParameters"))
                    || showAllParameters) {
                StringBuffer parameters = new StringBuffer();
                Iterator settables = container.attributeList(Settable.class)
                        .iterator();

                while (settables.hasNext()) {
                    Settable settable = (Settable) settables.next();

                    if (settable.getVisibility() != Settable.FULL) {
                        continue;
                    }

                    if (!showAllParameters
                            && !((NamedObj) settable).isOverridden()) {
                        continue;
                    }

                    if (!showAllParameters
                            && _isPropertySet((NamedObj) settable, "_hide")) {
                        continue;
                    }

                    String name = settable.getName();
                    String displayName = settable.getDisplayName();
                    parameters.append(displayName);

                    if (showAllParameters && !name.equals(displayName)) {
                        parameters.append(" (" + name + ")");
                    }

                    parameters.append(": ");
                    parameters.append(settable.getExpression());

                    if (settables.hasNext()) {
                        parameters.append("\n");
                    }
                }

                LabelFigure label = new LabelFigure(parameters.toString(),
                        _parameterFont, 1.0, SwingConstants.NORTH_WEST);

                // Shift the label slightly right so it doesn't
                // collide with ports.
                label.translateTo(backBounds.getX() + 5, backBounds.getY()
                        + backBounds.getHeight());
                figure.add(label);
            }
        }

        return figure;
    }

}
