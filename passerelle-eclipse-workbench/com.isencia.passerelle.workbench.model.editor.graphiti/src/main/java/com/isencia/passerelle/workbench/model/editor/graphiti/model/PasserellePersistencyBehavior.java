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
package com.isencia.passerelle.workbench.model.editor.graphiti.model;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DefaultPersistencyBehavior;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.jface.util.SafeRunnable;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramBehavior;

public class PasserellePersistencyBehavior extends DefaultPersistencyBehavior {

  public PasserellePersistencyBehavior(DiagramBehavior diagramBehavior) {
    super(diagramBehavior);
  }
  
  protected PasserelleDiagramBehavior getDiagramBehavior() {
    return (PasserelleDiagramBehavior) diagramBehavior;
  }

  @Override
  public Diagram loadDiagram(URI uri) {
    Diagram diagram = super.loadDiagram(uri);
    final IFile diagramFile = getDiagramBehavior().getDiagramFile();
    IFile momlFile = getMomlFileForDiagram(diagramFile);
    if (momlFile.exists()) {
      try {
        DiagramFlowRepository.registerDiagramAndFlow(diagram, FlowManager.readMoml(momlFile.getLocationURI().toURL()));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return diagram;
  }

  @Override
  public void saveDiagram(IProgressMonitor monitor) {
    super.saveDiagram(monitor);
    final Diagram diagram = getDiagramBehavior().getDiagramTypeProvider().getDiagram();
    saveToMOML(DiagramFlowRepository.getFlowForDiagram(diagram), monitor);
  }

  protected void saveToMOML(final Flow f, final IProgressMonitor monitor) {
    SafeRunner.run(new SafeRunnable() {
      public void run() throws Exception {
        StringWriter writer = new StringWriter();
        f.exportMoML(writer);

        final IFile diagramFile = getDiagramBehavior().getDiagramFile();
        IFile momlFile = getMomlFileForDiagram(diagramFile);

        if (!momlFile.exists()) {
          momlFile.create(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), true, monitor);
        } else {
          momlFile.setContents(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), true, false, monitor);
        }
      }
    });
  }

  private IFile getMomlFileForDiagram(IFile diagramFile) {
    IFile momlFile = null;
    IContainer fileContainer = diagramFile.getParent();
    String diagramFileName = diagramFile.getName();
    String momlFileName = diagramFileName.substring(0, diagramFileName.lastIndexOf(".")) + ".moml";
    if (fileContainer instanceof IFolder) {
      momlFile = ((IFolder) fileContainer).getFile(momlFileName);
    } else if (fileContainer instanceof IProject) {
      momlFile = ((IProject) fileContainer).getFile(momlFileName);
    } else {
      throw new RuntimeException("Unable to save MOML file : Diagram file " + diagramFile + " not in a IFolder nor in a IProject");
    }
    return momlFile;
  }

}
