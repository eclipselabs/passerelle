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

import ptolemy.actor.gui.EditorFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * PasserelleEditorFactory
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PasserelleEditorFactory extends EditorFactory {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1552894915611896868L;

	/**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public PasserelleEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.gui.EditorFactory#createEditor(ptolemy.kernel.util.NamedObj, java.awt.Frame)
     */
    public void createEditor(NamedObj object, Frame parent) {
        new EditParametersDialog(parent, object);
    }

}
