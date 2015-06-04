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

import java.util.List;
import com.isencia.passerelle.actor.gui.PasserelleEditorFactory;
import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * A ParameterGroup acts as a container for a number of
 * plain actor parameters.
 * 
 * It serves mainly to enable the usage of sub-panels in
 * the actor configuration dialogs.
 * 
 * @author erwin
 */
public class ParameterGroup extends Parameter {

	/**
	 * @param container
	 * @param name
	 * @throws ptolemy.kernel.util.IllegalActionException
	 * @throws ptolemy.kernel.util.NameDuplicationException
	 */
	public ParameterGroup(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		new PasserelleEditorFactory(this,"_editorFactory");
		new PasserelleEditorPaneFactory(this, "_editorPaneFactory");
	}

	/**
	 * Returns the list of parameters that belong to this group.
	 * Members become part of a group by just using the group as
	 * their container in their constructor.
	 * 
	 * This method iterates over all child entitities and returns
	 * all of them that are Parameters.
	 * @return
	 */
	public Parameter[] getMembers() {
		Parameter[] results = new Parameter[0];
		List members = attributeList(Parameter.class);
		if(members!=null) {
			results = (Parameter[]) members.toArray(results);
		}
		return results;
	}

	@Override
	public void attributeChanged(Attribute attribute) throws IllegalActionException {
		if(attribute instanceof Parameter) {
			// forward any changes in child params to the container actor of this group
			getContainer().attributeChanged(attribute);
		} else {
			super.attributeChanged(attribute);
		}
	}
	
	
}
