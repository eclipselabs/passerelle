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
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.tb.ContextEntryHelper;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonEntry;
import org.eclipse.graphiti.tb.IContextButtonPadData;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorCollapseFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorConfigureFeature;

public class PasserelleToolBehaviorProvider extends DefaultToolBehaviorProvider {

  public PasserelleToolBehaviorProvider(IDiagramTypeProvider dtp) {
    super(dtp);
  }

  @Override
  public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
    ICustomFeature customFeature = new ActorConfigureFeature((PasserelleDiagramFeatureProvider) getFeatureProvider());
    // canExecute() tests especially if the context contains an actor
    if (customFeature.canExecute(context)) {
      return customFeature;
    } else {
      return super.getDoubleClickFeature(context);
    }
  }

  @Override
  public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
    IContextButtonPadData data = super.getContextButtonPad(context);
    PictogramElement pe = context.getPictogramElement();

    // 1. set the generic context buttons
    // note, that we do not add 'remove' (just as an example)
    setGenericContextButtons(data, pe, CONTEXT_BUTTON_DELETE | CONTEXT_BUTTON_UPDATE);

    // 2. set the collapse button
    // simply use a dummy custom feature (senseless example)
    CustomContext cc = new CustomContext(new PictogramElement[] { pe });
    ICustomFeature[] cf = getFeatureProvider().getCustomFeatures(cc);
    for (int i = 0; i < cf.length; i++) {
      ICustomFeature iCustomFeature = cf[i];
      if (iCustomFeature instanceof ActorCollapseFeature) {
        IContextButtonEntry collapseButton = ContextEntryHelper.createCollapseContextButton(true, iCustomFeature, cc);
        data.setCollapseContextButton(collapseButton);
        break;
      }
    }
    
    return data;
  }
}
