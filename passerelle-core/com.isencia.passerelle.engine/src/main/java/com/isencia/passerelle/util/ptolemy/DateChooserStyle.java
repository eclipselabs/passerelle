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
package com.isencia.passerelle.util.ptolemy;

import java.io.File;
import java.net.URI;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DatePickerStyle
/**
   This attribute annotates user settable attributes to specify
   that the value of the parameter can be optionally given using a
   Date Picker.

	@author erwin
*/

public class DateChooserStyle extends ParameterEditorStyle {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public DateChooserStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public DateChooserStyle(Workspace workspace) {
        // This constructor is needed for Shallow codegen to work.
        super(workspace);
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Settable.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DateChooserStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param param The attribute that this annotates.
     *  @return True if the argument is a StringAttribute, false otherwise.
     */
    public boolean acceptable(Settable param) {
        if (!(param instanceof StringAttribute)) return false;
        else return true;
    }

    /** Create a new entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *  @param query The query into which to add the entry.
     */
    public void addEntry(PtolemyQuery query) {
        Settable container = (Settable)getContainer();
        String name = container.getName();
        String defaultValue = container.getExpression();
        defaultValue = container.getExpression();
        URI modelURI = URIAttribute.getModelURI(this);
        File directory = null;
        if (modelURI != null) {
            if (modelURI.getScheme().equals("file")) {
                File modelFile = new File(modelURI);
                directory = modelFile.getParentFile();
            }
        }
        query.addFileChooser(
                name,
                name,
                defaultValue,
                modelURI,
                directory,
                PtolemyQuery.preferredBackgroundColor(container),
                PtolemyQuery.preferredForegroundColor(container));
        query.attachParameter(container, name);
    }
}
