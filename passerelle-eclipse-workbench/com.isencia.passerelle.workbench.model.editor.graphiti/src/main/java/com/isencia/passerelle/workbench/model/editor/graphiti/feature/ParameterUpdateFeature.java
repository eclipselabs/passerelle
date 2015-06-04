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
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;

/**
 * @author erwin
 *
 */
public class ParameterUpdateFeature extends AbstractUpdateFeature {

  public ParameterUpdateFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  public boolean canUpdate(IUpdateContext context) {
    String boCategory = Graphiti.getPeService().getPropertyValue(context.getPictogramElement(), "__BO_CATEGORY");
    return ("PARAMETER".equals(boCategory));
  }

  /**
   * The indirect update handling of graphiti means that complex diff-checking is needed here between the stuff that is currently maintained in the graphical
   * model, and the actual configuration&status of an actor.
   * <p>
   * This is different from ptolemy's Vergil, and most other Passerelle model editors, where updates are done through editor components and/or parameter
   * configuration dialogs and are then applied immediately to the graphical model through MOML change requests, GEF commands etc.
   * </p>
   * <p>
   * Currently following items are checked :
   * <ul>
   * <li>Parameter's name</li>
   * <li>Parameter's value</li>
   * </ul>
   * </p>
   */
  @Override
  public IReason updateNeeded(IUpdateContext context) {
    // retrieve name from business model
    String pName = null;
    boolean pNameChanged = false;
    boolean pValueChanged = false;

    PictogramElement pictogramElement = context.getPictogramElement();
    Object bo = getBusinessObjectForPictogramElement(pictogramElement);
    if (bo instanceof Variable && pictogramElement instanceof ContainerShape) {
      Variable parameter = (Variable) bo;
      ContainerShape cs = (ContainerShape) pictogramElement;

      pName = parameter.getName();

      for (Shape shape : cs.getChildren()) {
        String boName = Graphiti.getPeService().getPropertyValue(shape, "__BO_NAME");
        if(!boName.equals(pName)) {
          pNameChanged=true;
        }
        String boCategory = Graphiti.getPeService().getPropertyValue(shape, "__BO_CATEGORY");
        if (shape.getGraphicsAlgorithm() instanceof Text) {
          Text text = (Text) shape.getGraphicsAlgorithm();
          if ("PARAMETER".equalsIgnoreCase(boCategory)) {
            String boValue = Graphiti.getPeService().getPropertyValue(shape, "__BO_VALUE");
            Parameter p = (Parameter) parameter.getAttribute(boName);
            if (!parameter.getExpression().equals(boValue)) {
              pValueChanged=true;
            }
          }
        }
      }
    }
    // build diff result and store some useful info in the update context
    if (pNameChanged || pValueChanged) {
      StringBuilder diffResultBldr = new StringBuilder();
      if(pNameChanged) {
        diffResultBldr.append("Parameter name changed; ");
        context.putProperty("PARAMETERNAME_CHANGED", "true");
      }
      if(pValueChanged) {
        diffResultBldr.append("Parameter change; ");
        context.putProperty("PARAMETER_CHANGED", "true");
      }
      return Reason.createTrueReason(diffResultBldr.toString());
    } else {
      return Reason.createFalseReason();
    }
  }

  @Override
  public boolean update(IUpdateContext context) {
    boolean result = false;
    PictogramElement pe = context.getPictogramElement();
    Object bo = getBusinessObjectForPictogramElement(pe);
    if((pe instanceof ContainerShape) && (bo instanceof Variable)) {
      ContainerShape cs = (ContainerShape) pe;
      Variable changedParameter = (Variable) bo;
      
      for (Shape shape : cs.getChildren()) {
        String boCategory = Graphiti.getPeService().getPropertyValue(shape, "__BO_CATEGORY");
        if("PARAMETER".equals(boCategory)) {
          Text text = (Text) shape.getGraphicsAlgorithm();
          String pName = changedParameter.getDisplayName();
          String pVal = changedParameter.getExpression();
          text.setValue(pName + " : " + pVal);
          Graphiti.getPeService().setPropertyValue(shape, "__BO_VALUE",pVal);
          result = true;
        }
      }
    }
    return result;
  }

}
