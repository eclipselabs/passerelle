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
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;

public class ModelElementNameDirectEditFeature extends AbstractDirectEditingFeature {

  public ModelElementNameDirectEditFeature(IFeatureProvider fp) {
    super(fp);
  }

  public int getEditingType() {
    return TYPE_TEXT;
  }

  @Override
  public boolean canDirectEdit(IDirectEditingContext context) {
    PictogramElement pe = context.getPictogramElement();
    GraphicsAlgorithm ga = context.getGraphicsAlgorithm();
    String boCategory = Graphiti.getPeService().getPropertyValue(pe, "__BO_CATEGORY");
    // The name of an actor (or other model element) is the only
    // Text element that is linked to it as its business object
    if (("PARAMETER".equals(boCategory) || "ACTOR".equals(boCategory) || "DIRECTOR".equals(boCategory)) && (ga instanceof Text)) {
      return true;
    }
    return false;
  }

  public String getInitialValue(IDirectEditingContext context) {
    // return the current name of the EClass
    PictogramElement pe = context.getPictogramElement();
    NamedObj bo = (NamedObj) getBusinessObjectForPictogramElement(pe);
    return bo.getName();
  }

  @Override
  public String checkValueValid(String value, IDirectEditingContext context) {
    PictogramElement pe = context.getPictogramElement();
    NamedObj bo = (NamedObj) getBusinessObjectForPictogramElement(pe);
    if (!bo.getName().equals(value)) {
      if (value.length() < 1)
        return "The name should be non-empty";
      if (value.contains("."))
        return "Dots are not allowed in names.";
      if (value.contains("\n"))
        return "Line breakes are not allowed in names.";

      if (bo.getContainer() instanceof CompositeEntity && ((CompositeEntity) bo.getContainer()).getEntity(value) != null) {
        return "Duplicate name";
      }
    }
    // null means, that the value is valid
    return null;
  }

  public void setValue(String value, IDirectEditingContext context) {
    // set the new name for the EClass
    PictogramElement pe = context.getPictogramElement();
    NamedObj bo = (NamedObj) getBusinessObjectForPictogramElement(pe);
    try {
      RenameCommand cmd = new RenameCommand(bo, value);
      if(cmd.canExecute()) {
        cmd.execute();
      }
    } catch (Exception e) {
      // TODO show error to user
      e.printStackTrace();
    }

    // Explicitly update the shape to display the new value in the diagram
    // Note, that this might not be necessary in future versions of Graphiti
    // (currently in discussion)

    // we know, that pe is the Shape of the Text, so its container is the
    // main shape of the actor
    updatePictogramElement(((Shape) pe).getContainer());
  }
}