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

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.ui.IWorkbenchPart;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramFeatureProvider;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorDialog;

public class ActorConfigureFeature extends AbstractCustomFeature {

  private boolean hasDoneChanges = false;

  public ActorConfigureFeature(PasserelleDiagramFeatureProvider fp) {
    super(fp);
  }

  @Override
  public PasserelleDiagramFeatureProvider getFeatureProvider() {
    return (PasserelleDiagramFeatureProvider) super.getFeatureProvider();
  }

  @Override
  public String getName() {
    return "Configure";
  }

  @Override
  public String getDescription() {
    return "Configure an actor or director";
  }

  @Override
  public boolean canExecute(ICustomContext context) {
    boolean ret = false;
    PictogramElement pe = context.getInnerPictogramElement();
    GraphicsAlgorithm ga = context.getInnerGraphicsAlgorithm();
    // prevent double click action on actor's name's Text field
    if (pe != null && !(ga instanceof Text)) {
      String boCategory = Graphiti.getPeService().getPropertyValue(pe, "__BO_CATEGORY");
      if ("ACTOR".equals(boCategory) || "DIRECTOR".equals(boCategory)) {
        ret = true;
      }
    }
    return ret;
  }

  @Override
  public void execute(ICustomContext context) {
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      Object bo = getBusinessObjectForPictogramElement(pes[0]);
      if (bo instanceof NamedObj) {
        NamedObj modelElement = (NamedObj) bo;
        ActorDialog dialog = new ActorDialog((IWorkbenchPart) getDiagramEditor(), modelElement);
        dialog.open();
        this.hasDoneChanges = true;
      }
    }
  }

  @Override
  public boolean hasDoneChanges() {
    return this.hasDoneChanges;
  }
}