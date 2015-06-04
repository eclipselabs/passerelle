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
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.swt.graphics.Image;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import com.isencia.passerelle.workbench.model.editor.graphiti.ImageConstants;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramTypeProvider;

/**
 * EditPart for components in the Tree.
 */
public class OutlineEditPart extends org.eclipse.gef.editparts.AbstractTreeEditPart implements ValueListener, ChangeListener {

  /**
   * Constructor initializes this with the given model.
   * 
   * @param model
   *          The underlying flow model object (e.g. an actor)
   */
  public OutlineEditPart(NamedObj model) {
    super(model);
    if (model instanceof Parameter) {
      Parameter parameter = (Parameter) model;
      parameter.addValueListener(this);
    }
  }

  public void activate() {
    if (isActive())
      return;
    super.activate();

    if (getModel() instanceof Changeable) {
      Changeable changeable = (Changeable) getModel();
      changeable.addChangeListener(this);
    }
  }

  public void deactivate() {
    if (!isActive())
      return;
    if (getModel() instanceof Changeable) {
      Changeable changeable = (Changeable) getModel();
      changeable.removeChangeListener(this);
    }
    super.deactivate();
  }

  /**
   * Returns <code>null</code> as a Tree EditPart holds no children under it.
   * 
   * @return <code>null</code>
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected List getModelChildren() {
    List children = new ArrayList();
    NamedObj flowModel = (NamedObj) getModel();
    children.addAll(flowModel.attributeList(Parameter.class));
    if (flowModel instanceof ComponentEntity) {
      ComponentEntity actor = (ComponentEntity) flowModel;
      children.addAll(actor.portList());
      if (flowModel instanceof CompositeEntity) {
        CompositeEntity composite = (CompositeEntity) flowModel;
        children.addAll(composite.entityList());
      }
    }
    return children;
  }

  @Override
  protected Image getImage() {
    NamedObj flowModel = (NamedObj) getModel();
    if (flowModel instanceof Director)
      return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_DIRECTOR);
    else if (flowModel instanceof Parameter)
      return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_PARAMETER);
    else if (flowModel instanceof IOPort) {
      IOPort port = (IOPort) flowModel;
      if (port.isInput()) {
        return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_INPUTPORT);
      } else {
        return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_OUTPUTPORT);
      }
    } else if (flowModel instanceof TypedAtomicActor) {
      return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_ACTOR);
    } else if (flowModel instanceof CompositeActor) {
      return GraphitiUi.getImageService().getImageForId(PasserelleDiagramTypeProvider.ID, ImageConstants.IMG_COMPOSITE);
    } else {
      return super.getImage();
    }
  }

  @Override
  protected String getText() {
    NamedObj flowModel = (NamedObj) getModel();
    if (flowModel instanceof Parameter) {
      Parameter param = (Parameter) flowModel;
      String name = param.getName();
      String value = param.getExpression();
      return (name + "=" + (value == null ? "" : value));
    } else {
      return flowModel.getName();
    }
  }

  /**
   * NOTE This can be called from non-UI thread!
   */

  public void valueChanged(Settable settable) {
    try {
      getRoot().getViewer().getControl().getDisplay().asyncExec(new Runnable() {
        public void run() {
          refreshVisuals();
        }
      });
    } catch (Exception e) {
    }
  }

  public void changeExecuted(ChangeRequest changerequest) {
    try {
      refreshVisuals();
      refreshChildren();
    } catch (Exception e) {
    }
  }

  public void changeFailed(ChangeRequest changerequest, Exception exception) {
  }
}
