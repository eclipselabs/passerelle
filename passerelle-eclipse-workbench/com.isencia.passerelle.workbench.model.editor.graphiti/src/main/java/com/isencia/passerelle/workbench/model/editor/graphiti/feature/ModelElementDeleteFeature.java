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
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Attribute;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramFeatureProvider;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramTypeProvider;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserelleIndependenceSolver;

public class ModelElementDeleteFeature extends DefaultDeleteFeature {
  private final static Logger LOGGER = LoggerFactory.getLogger(ModelElementDeleteFeature.class);

  public ModelElementDeleteFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  protected void deleteBusinessObject(Object bo) {
    super.deleteBusinessObject(bo);
    
    IFeatureProvider featureProvider = getFeatureProvider();
    if(featureProvider instanceof PasserelleDiagramFeatureProvider) {
      PasserelleIndependenceSolver independenceSolver = ((PasserelleDiagramTypeProvider) featureProvider.getDiagramTypeProvider()).getIndependenceSolver();
      independenceSolver.removeBusinessObject(bo);
      if(bo instanceof ComponentEntity) {
        ComponentEntity modelElement = (ComponentEntity)bo;
        try {
          modelElement.setContainer(null);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else if(bo instanceof Attribute) {
        Attribute modelElement = (Attribute)bo;
        try {
          modelElement.setContainer(null);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }  else if(bo instanceof ComponentRelation) {
        ComponentRelation modelElement = (ComponentRelation)bo;
        try {
          modelElement.setContainer(null);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        LOGGER.warn("Delete not supported for {}", bo);
      }
    }
  }

}
