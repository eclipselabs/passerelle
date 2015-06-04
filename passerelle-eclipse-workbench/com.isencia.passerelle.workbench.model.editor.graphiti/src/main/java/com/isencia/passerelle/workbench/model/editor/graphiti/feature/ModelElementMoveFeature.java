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
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;

/**
 * We want to replicate basic graphical model info in the MOML, so when moving an element, it's location must be updated in the MOML as well.
 * 
 * @author erwin
 */
public class ModelElementMoveFeature extends DefaultMoveShapeFeature {

  public ModelElementMoveFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  protected void postMoveShape(IMoveShapeContext context) {
    super.postMoveShape(context);
    Shape s = context.getShape();
    Object bo = getBusinessObjectForPictogramElement(s);
    if(bo instanceof NamedObj) {
      NamedObj modelElement = (NamedObj)bo;
      SetConstraintCommand cmd = new SetConstraintCommand();
      cmd.setModel(modelElement);
      cmd.setLocation(new double[]{context.getX(), context.getY()});
      if(cmd.canExecute()) {
        cmd.execute();
      }
    }
  }
}
