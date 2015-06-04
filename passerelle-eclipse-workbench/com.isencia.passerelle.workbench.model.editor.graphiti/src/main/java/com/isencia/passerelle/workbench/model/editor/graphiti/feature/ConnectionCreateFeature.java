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
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.core.Port;

public class ConnectionCreateFeature extends AbstractCreateConnectionFeature {

  public ConnectionCreateFeature(IFeatureProvider fp) {
    // provide name and description for the UI, e.g. the palette
    super(fp, "Relation", "Create Connection");
  }

  public boolean canCreate(ICreateConnectionContext context) {
    // return true if both anchors belong to a Port
    Port source = getPort(context.getSourceAnchor());
    Port target = getPort(context.getTargetAnchor());
    if (source != null && source.isOutput() && target != null && target.isInput()) {
      return true;
    }
    return false;
  }

  public boolean canStartConnection(ICreateConnectionContext context) {
    // return true if start anchor belongs to a Port
    Port port = getPort(context.getSourceAnchor());
    if (port != null && port.isOutput()) {
      return true;
    }
    return false;
  }

  public Connection create(ICreateConnectionContext context) {
    try {
      Connection newConnection = null;

      // get Ports which should be connected
      Port source = getPort(context.getSourceAnchor());
      Port target = getPort(context.getTargetAnchor());

      if (source != null && target != null) {
        // create new business object
        Relation eReference = createRelation(source, target);
        // add connection for business object
        AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
        addContext.setNewObject(eReference);
        newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
      }

      return newConnection;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns the Port belonging to the anchor, or null if not available.
   */
  private Port getPort(Anchor anchor) {
    if (anchor != null) {
      Object object = getBusinessObjectForPictogramElement(anchor);
      if (object instanceof Port) {
        return (Port) object;
      } else {
        System.out.println("what's this???? "+object);
      }
    }
    return null;
  }

  /**
   * Creates a EReference between two EClasses.
   * 
   * @throws IllegalActionException
   */
  private Relation createRelation(Port source, Port target) throws IllegalActionException {
    CompositeActor flow = (CompositeActor) source.toplevel();
    return flow.connect(source, target);
  }
}
