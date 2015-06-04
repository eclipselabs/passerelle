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
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;
import ptolemy.kernel.Relation;

public class ConnectionAddFeature extends AbstractAddFeature {

  private static final IColorConstant CONNECTION_FOREGROUND = new ColorConstant(98, 131, 167);

  public ConnectionAddFeature(IFeatureProvider fp) {
    super(fp);
  }

  public PictogramElement add(IAddContext context) {
    IAddConnectionContext addConContext = (IAddConnectionContext) context;
    Relation addedRelation = (Relation) context.getNewObject();
    IPeCreateService peCreateService = Graphiti.getPeCreateService();

    // CONNECTION WITH POLYLINE
    Connection connection = peCreateService.createFreeFormConnection(getDiagram());
    connection.setStart(addConContext.getSourceAnchor());
    connection.setEnd(addConContext.getTargetAnchor());

    IGaService gaService = Graphiti.getGaService();
    Polyline polyline = gaService.createPolyline(connection);
    polyline.setLineWidth(2);
    polyline.setForeground(manageColor(CONNECTION_FOREGROUND));

    // create link and wire it
    link(connection, addedRelation);

    return connection;
  }

  public boolean canAdd(IAddContext context) {
    if (context instanceof IAddConnectionContext && context.getNewObject() instanceof Relation) {
      return true;
    }
    return false;
  }
}
