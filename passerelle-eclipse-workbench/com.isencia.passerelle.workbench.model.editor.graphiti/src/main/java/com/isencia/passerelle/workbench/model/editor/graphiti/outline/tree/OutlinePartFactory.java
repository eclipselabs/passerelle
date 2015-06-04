/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.workbench.model.editor.graphiti.outline.tree;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.NamedObj;


public class OutlinePartFactory implements EditPartFactory {
	private Set<OutlineEditPart> parts = new HashSet<OutlineEditPart>();

  public Set<OutlineEditPart> getParts() {
		return parts;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		OutlineEditPart editPart = null;
		if (model instanceof CompositeActor) {
			editPart = new OutlineContainerEditPart(context, (CompositeActor) model);
		} else if(model instanceof NamedObj){
		  editPart = new OutlineEditPart((NamedObj) model);
		}
		parts.add(editPart);
		return editPart;
	}

}
