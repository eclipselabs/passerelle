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

import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICopyContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.AbstractCopyFeature;
import ptolemy.kernel.util.NamedObj;

public class ModelElementCopyFeature extends AbstractCopyFeature {
  
  public ModelElementCopyFeature(IFeatureProvider fp) {
      super(fp);
  }

  public boolean canCopy(ICopyContext context) {
      final PictogramElement[] pes = context.getPictogramElements();
      if (pes == null || pes.length == 0) {  // nothing selected
          return false;
      }
      
      // return true, if all selected elements are potential model elements, i.e. NamedObjs
      for (PictogramElement pe : pes) {
          final Object bo = getBusinessObjectForPictogramElement(pe);
          if (!(bo instanceof NamedObj)) {
              return false;
          }
      }
      return true;
  }

  public void copy(ICopyContext context) {
      // get the business-objects for all pictogram-elements
      // we already verified, that all business-objects are potential model elements
      PictogramElement[] pes = context.getPictogramElements();
      Object[] bos = new Object[pes.length ];
      for (int i = 0; i < pes.length ; i++) {
          PictogramElement pe = pes[i];
          bos[i] = getBusinessObjectForPictogramElement(pe);
      }
      // put all business objects to the clipboard
      putToClipboard(bos);
  }
  
  @Override
  protected void putToClipboard(Object[] objects) {
    Clipboard.getDefault().setContents(objects);
  }
}
