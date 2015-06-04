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
package com.isencia.passerelle.workbench.model.editor.graphiti.feature;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.DiagramFlowRepository;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;

/**
 * Creates a new model element based on a drag-n-drop from the Passerelle palette, after prompting the user for the name.
 * 
 * @author erwin
 */
public class ModelElementCreateFeatureFromPaletteItemDefinition extends AbstractCreateFeature {

  private PaletteItemDefinition selectedActorTemplate;

  public ModelElementCreateFeatureFromPaletteItemDefinition(PaletteItemDefinition selected, IFeatureProvider fp) {
    // set name and description of the creation feature
    super(fp, "Actor", "Create Actor");
    this.selectedActorTemplate = selected;
  }

  public boolean canCreate(ICreateContext context) {
    return context.getTargetContainer() instanceof Diagram;
  }

  public Object[] create(ICreateContext context) {
    // !!actor name can now be set via direct editing after D-n-D
    // ask user for actor name
    // String actorName = DiagramUtils.askString(TITLE, USER_QUESTION, "");
    // if (actorName == null || actorName.trim().length() == 0) {
    // return EMPTY;
    // }

    return create(context, selectedActorTemplate.getName());
  }

  @SuppressWarnings("unchecked")
  public Object[] create(ICreateContext context, String actorName) {
    try {
      Diagram d = getFeatureProvider().getDiagramTypeProvider().getDiagram();
      Flow flow = DiagramFlowRepository.getFlowForDiagram(d);

      // create actor
      CreateComponentCommand create = new CreateComponentCommand(null, flow);
      create.setName(actorName);
      create.setClazz(selectedActorTemplate.getClazz());
      create.setLocation(new double[] { context.getX(), context.getY() });
      create.setLabel("add actor");
      create.execute();
      NamedObj result = create.getChild();
      // do the add
      addGraphicalRepresentation(context, result);
      // activate direct editing after object creation
      getFeatureProvider().getDirectEditingInfo().setActive(true);
      // return newly created business object(s)
      return new Object[] { result };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
