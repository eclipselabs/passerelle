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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.swt.graphics.Image;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.passerelle.workbench.model.editor.graphiti.ImageConstants;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramTypeProvider;

/**
 * Tree EditPart for the Container.
 */
public class OutlineContainerEditPart extends OutlineEditPart {
  private EditPart context;
  
  /**
   * Constructor, which initializes this using the model given as input.
   */
  public OutlineContainerEditPart(EditPart context, CompositeActor model) {
    super(model);
    this.context = context;
  }

  /**
   * Creates and installs pertinent EditPolicies.
   */
  protected void createEditPolicies() {
    super.createEditPolicies();
  }

  @Override
  protected Image getImage() {
    return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, 
        ImageConstants.IMG_COMPOSITE);
  }

  /**
   * Returns the children of this from the model, as this is capable enough of holding EditParts.
   * 
   * @return List of children.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected List getModelChildren() {
    ArrayList children = new ArrayList();

    CompositeActor actor = (CompositeActor) getModel();
    children.addAll(actor.attributeList(Parameter.class));
    children.addAll(actor.attributeList(TextAttribute.class));
    children.addAll(actor.attributeList(IOPort.class));
    children.addAll(actor.portList());
    List entities = actor.entityList();
    if (entities != null)
      children.addAll(entities);
    // Only show children 1 level deep
    boolean showChildren = !(context != null && context.getParent() != null);
    if (!showChildren)
      return children;

    if (actor.isOpaque()) {
      children.add(actor.getDirector());
    }

    return children;
  }
}
