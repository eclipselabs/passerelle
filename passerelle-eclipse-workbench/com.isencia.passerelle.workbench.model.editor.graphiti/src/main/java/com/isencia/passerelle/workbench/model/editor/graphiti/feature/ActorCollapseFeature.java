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

import java.util.Iterator;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.platform.IPlatformImageConstants;
import org.eclipse.graphiti.services.Graphiti;
import ptolemy.actor.Actor;

public class ActorCollapseFeature extends AbstractCustomFeature {

  public ActorCollapseFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  public void execute(ICustomContext context) {
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      Object bo = getBusinessObjectForPictogramElement(pes[0]);
      if (bo instanceof Actor) {
        collapseShape(pes[0]);
      }
    }
  }

  private void collapseShape(PictogramElement pe) {
    ContainerShape cs = (ContainerShape) pe;
    int width = pe.getGraphicsAlgorithm().getWidth();
    int height = pe.getGraphicsAlgorithm().getHeight();

    int changeWidth = width;
    int changeHeight = 30;

    boolean visible = false;
    if (Graphiti.getPeService().getPropertyValue(pe, "isCollapsed") == null || Graphiti.getPeService().getPropertyValue(pe, "isCollapsed").equals("false")) {
      Graphiti.getPeService().setPropertyValue(pe, "initial_width", String.valueOf(width));
      Graphiti.getPeService().setPropertyValue(pe, "initial_height", String.valueOf(height));
      visible = false;
    } else if (Graphiti.getPeService().getPropertyValue(pe, "isCollapsed") != null
        && Graphiti.getPeService().getPropertyValue(pe, "isCollapsed").equals("true")) {
      changeWidth = Integer.parseInt(Graphiti.getPeService().getPropertyValue(pe, "initial_width"));
      changeHeight = Integer.parseInt(Graphiti.getPeService().getPropertyValue(pe, "initial_height"));
      Graphiti.getPeService().setPropertyValue(pe, "isCollapsed", "false");
      visible = true;
    }

    ResizeShapeContext context1 = new ResizeShapeContext(cs);
    context1.setSize(changeWidth, changeHeight);
    context1.setLocation(cs.getGraphicsAlgorithm().getX(), cs.getGraphicsAlgorithm().getY());
    IResizeShapeFeature rsf = getFeatureProvider().getResizeShapeFeature(context1);
    if (rsf.canExecute(context1)) {
      rsf.execute(context1);
    }

    if (!visible) {
      Graphiti.getPeService().setPropertyValue(pe, "isCollapsed", "true");
    }
    // visible/invisible all the children
    makeChildrenInvisible(cs, visible);
  }

  @Override
  public boolean canExecute(ICustomContext context) {
    boolean ret = false;
    PictogramElement[] pes = context.getPictogramElements();
    if (pes != null && pes.length == 1) {
      Object bo = getBusinessObjectForPictogramElement(pes[0]);
      if (bo instanceof Actor) {
        ret = true;
      }
    }
    return ret;
  }

  @Override
  public String getName() {
    return "Collapse";
  }

  @Override
  public String getDescription() {
    return "Collapse Actor";
  }

  @Override
  public String getImageId() {
    return IPlatformImageConstants.IMG_EDIT_COLLAPSE;
  }

  @Override
  public boolean isAvailable(IContext context) {
    return true;
  }

  /**
   * Sets visibility for the children of a model element's shape. Following child elements are included :
   * <ul>
   * <li>Attributes/parameters</li>
   * <li>When the model element
   * </ul>
   * 
   * @param cs
   * @param visible
   */
  public void makeChildrenInvisible(ContainerShape cs, boolean visible) {
    if (cs.getChildren().isEmpty()) {
      return;
    } else {
      Iterator<Shape> iter = cs.getChildren().iterator();
      while (iter.hasNext()) {
        Shape shape = iter.next();
        String boCategory = Graphiti.getPeService().getPropertyValue(shape, "__BO_CATEGORY");
        if (shape instanceof ContainerShape) {
          makeChildrenInvisible((ContainerShape) shape, visible);
          shape.setVisible(visible);
        } else if ("PARAMETER".equals(boCategory)) {
          shape.setVisible(visible);
        } else if ("INPUT".equals(boCategory)) {

        } else if ("OUTPUT".equals(boCategory)) {

        }
      }
    }
  }
}
