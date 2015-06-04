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

import java.util.List;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IPasteContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.AbstractPasteFeature;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.editor.common.utils.EditorUtils;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.DiagramFlowRepository;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserelleIndependenceSolver;
import com.isencia.passerelle.workbench.model.ui.command.CopyComponentCommand;

public class ModelElementPasteFeature extends AbstractPasteFeature {

  public ModelElementPasteFeature(IFeatureProvider fp) {
    super(fp);
  }

  public boolean canPaste(IPasteContext context) {
    // only support pasting directly in the diagram (nothing else selected)
    PictogramElement[] pes = context.getPictogramElements();
    if (pes.length != 1 || !(pes[0] instanceof Diagram)) {
      return false;
    }
    // can paste, if all objects on the clipboard are potential model elements, i.e NamedObjs
    Object[] fromClipboard = getFromClipboard();
    if (fromClipboard == null || fromClipboard.length == 0) {
      return false;
    }
    for (Object object : fromClipboard) {
      if (!(object instanceof NamedObj)) {
        return false;
      }
    }
    return true;
  }

  public void paste(IPasteContext context) {
    Diagram d = getFeatureProvider().getDiagramTypeProvider().getDiagram();
    Flow flow = DiagramFlowRepository.getFlowForDiagram(d);
    // get the NamedObjs from the clipboard and copy them
    // then create new pictogram elements using the add feature
    Object[] objects = getFromClipboard();
    for (Object object : objects) {
      if (object instanceof NamedObj) {
        NamedObj child = (NamedObj) object;
        double[] location = EditorUtils.getLocation(child);
        double newX = location[0] + 10;
        double newY = location[1] + 10;
        CopyComponentCommand cmd = new CopyComponentCommand(flow, child, new double[] { newX, newY });
        cmd.execute();
        NamedObj copiedChild = cleanUUIDs(cmd.getNewChild());
        AddContext ac = new AddContext();
        ac.setLocation((int)newX, (int)newY);
        ac.setTargetContainer(d);
        addGraphicalRepresentation(ac, copiedChild);
      }
    }
  }

  @Override
  protected Object[] getFromClipboard() {
    Object contents = Clipboard.getDefault().getContents();
    if(contents==null) {
      return null;
    } else if(contents.getClass().isArray()) {
      return (Object[]) contents;
    } else {
      return new Object[] {contents};
    }
  }
  
  @SuppressWarnings("unchecked")
  private NamedObj cleanUUIDs(NamedObj no) {
    Attribute uuidAttr = no.getAttribute(PasserelleIndependenceSolver.PASS_UUID_ATTR);
    if(uuidAttr!=null) {
      try {
        uuidAttr.setContainer(null);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    List<Attribute> attrs = no.attributeList();
    for (Attribute attribute : attrs) {
      cleanUUIDs(attribute);
    }
    
    if(no instanceof ComponentEntity) {
      ComponentEntity entity = (ComponentEntity) no;
      List<Port> ports = entity.portList();
      for (Port p : ports) {
        cleanUUIDs(p);
      }
      if(no instanceof CompositeEntity) {
        CompositeEntity entityHolder = (CompositeEntity) no;
        List<Entity> entities = entityHolder.entityList();
        for (Entity e : entities) {
          cleanUUIDs(e);
        }
        List<Relation> relations = entityHolder.relationList();
        for (Relation r : relations) {
          cleanUUIDs(r);
        }
      }
    }
    
    return no;
  }
}
