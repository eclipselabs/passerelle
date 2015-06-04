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

import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICopyFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IPasteFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICopyContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IPasteContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
import ptolemy.actor.Director;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorAddFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorCollapseFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorConfigureFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ActorUpdateFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ConnectionAddFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ConnectionCreateFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.DirectorAddFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementCopyFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementDeleteFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementMoveFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementNameDirectEditFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementPasteFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ParameterAddFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ParameterUpdateFeature;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserelleIndependenceSolver;

/**
 * @author delerw
 */
public class PasserelleDiagramFeatureProvider extends DefaultFeatureProvider {

  PasserelleIndependenceSolver passerelleIndependenceSolver;

  /**
   * @param dtp
   */
  public PasserelleDiagramFeatureProvider(PasserelleDiagramTypeProvider dtp) {
    super(dtp);
    setIndependenceSolver(dtp.getIndependenceSolver());
  }

  @Override
  public IAddFeature getAddFeature(IAddContext context) {
    if (context.getNewObject() instanceof Actor) {
      return new ActorAddFeature(this);
    } else if (context.getNewObject() instanceof Relation) {
      return new ConnectionAddFeature(this);
    } else if (context.getNewObject() instanceof Director) {
      return new DirectorAddFeature(this);
    }  else if (context.getNewObject() instanceof Variable) {
      return new ParameterAddFeature(this);
    }
    return super.getAddFeature(context);
  }
  
  @Override
  public IDeleteFeature getDeleteFeature(IDeleteContext context) {
    return new ModelElementDeleteFeature(this);
  }

  @Override
  public ICreateConnectionFeature[] getCreateConnectionFeatures() {
    return new ICreateConnectionFeature[] { new ConnectionCreateFeature(this) };
  }

  @Override
  public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
    PictogramElement pe = context.getPictogramElement();
    Object bo = getBusinessObjectForPictogramElement(pe);
    if (bo instanceof NamedObj) {
      return new ModelElementNameDirectEditFeature(this);
    }
    return super.getDirectEditingFeature(context);
  }

  @Override
  public IUpdateFeature getUpdateFeature(IUpdateContext context) {
    String boCategory = Graphiti.getPeService().getPropertyValue(context.getPictogramElement(), "__BO_CATEGORY");
    if ("ACTOR".equals(boCategory)) {
      return new ActorUpdateFeature(this);
    } else if ("PARAMETER".equals(boCategory)) {
      return new ParameterUpdateFeature(this);
    } 
    return super.getUpdateFeature(context);
  }

  @Override
  public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
    Shape shape = context.getShape();
    Object bo = getBusinessObjectForPictogramElement(shape);
    if (bo instanceof NamedObj) {
      return new ModelElementMoveFeature(this);
    } else {
      return super.getMoveShapeFeature(context);
    }
  }
  
  @Override
  public ICopyFeature getCopyFeature(ICopyContext context) {
      return new ModelElementCopyFeature(this);
  }
  
  @Override
  public IPasteFeature getPasteFeature(IPasteContext context) {
      return new ModelElementPasteFeature(this);
   } 

  @Override
  public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
    // simply return all create connection features
    return getCreateConnectionFeatures();
  }

  @Override
  public ICustomFeature[] getCustomFeatures(ICustomContext context) {
    return new ICustomFeature[] { new ActorConfigureFeature(this), new ActorCollapseFeature(this) };
  }
}
