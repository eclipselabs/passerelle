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
package com.isencia.passerelle.workbench.model.editor.graphiti.input;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;

public class PasserelleEditorInput extends DiagramEditorInput {

  public PasserelleEditorInput(URI diagramUri, String providerId) {
    super(diagramUri, providerId);
  }
  
  @Override
  public String getFactoryId() {
    return PasserelleEditorInputFactory.class.getName();
  }

  @Override
  public Object getAdapter(Class adapter) {
    if (IResource.class.isAssignableFrom(adapter)) {
      return GraphitiUiInternal.getEmfService().getFile(getUri());
      } 
    return null;

  }
}
