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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
import org.eclipse.graphiti.ui.internal.util.ReflectionUtil;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class PasserelleEditorInputFactory implements IElementFactory {

  @Override
  public IAdaptable createElement(IMemento memento) {
    // get diagram URI
    final String diagramUriString = memento.getString(PasserelleEditorInput.KEY_URI);
    if (diagramUriString == null) {
      return null;
    }
    // get diagram type provider id
    final String providerID = memento.getString(PasserelleEditorInput.KEY_PROVIDER_ID);
    URI diagramUri = URI.createURI(diagramUriString);
    return new PasserelleEditorInput(diagramUri, providerID);
  }

  public static DiagramEditorInput adaptToDiagramEditorInput(IEditorInput otherInput) {
    if (otherInput instanceof DiagramEditorInput) {
      DiagramEditorInput input = (DiagramEditorInput) otherInput;
      return input;
    }
    IFile file = ReflectionUtil.getFile(otherInput);
    if (file != null) {
      URI diagramFileUri = GraphitiUiInternal.getEmfService().getFileURI(file);
      return createDiagramEditorInput(diagramFileUri);
    }
    if (otherInput instanceof URIEditorInput) {
      final URIEditorInput uriInput = (URIEditorInput) otherInput;
      URI diagramFileUri = uriInput.getURI();
      return createDiagramEditorInput(diagramFileUri);
    }

    return null;
  }

  private static DiagramEditorInput createDiagramEditorInput(URI diagramFileUri) {
    if (diagramFileUri != null) {
      // the file's first base node has to be a diagram
      URI diagramUri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
      // take the first installed provider for this diagram type
      return new PasserelleEditorInput(diagramUri, null);
    }
    return null;
  }
}
