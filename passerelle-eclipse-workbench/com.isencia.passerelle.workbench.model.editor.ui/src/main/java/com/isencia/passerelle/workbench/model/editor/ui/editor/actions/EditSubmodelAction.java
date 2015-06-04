package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class EditSubmodelAction extends Action {

  private static final Logger logger = LoggerFactory.getLogger(EditSubmodelAction.class);

  private final String icon = "icons/edit.gif";
  private Object definition;

  public EditSubmodelAction(Object actionOrGroup) {

    super();
    setId("EditSubModel");
    setText("Edit submodel");
    this.definition = actionOrGroup;
    Activator.getImageDescriptor(icon);
    setHoverImageDescriptor(Activator.getImageDescriptor(icon));
    setImageDescriptor(Activator.getImageDescriptor(icon));
    setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
    setEnabled(checkEnabled());
  }

  protected boolean checkEnabled() {
    if (!(definition instanceof SubModelPaletteItemDefinition))
      return false;
    return true;
  }

  @Override
  public void run() {
    if (!(definition instanceof SubModelPaletteItemDefinition))
      return;
    if (definition != null) {
      try {
        final SubModelPaletteItemDefinition item = (SubModelPaletteItemDefinition) definition;
        final String name = item.getName();
        openFlowEditor(name);

      } catch (Exception e) {
        logger.error("Cannot edit submodel!", e);
      }
    }
  }

	public static void openFlowEditor(final String name) throws Exception,
			IOException, CoreException, PartInitException {
		// get object which represents the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// get location of workspace sub model directory
		String workspaceDirectoryPath = workspace.getRoot().getLocation().toFile().getAbsolutePath();
		String subModelDirectoryPath  = Activator.getDefault().getRepositoryService().getSubmodelFolder().toString();
		IFile file = null;
		
		// Check if the sub model directory is inside the workspace
		if (subModelDirectoryPath.startsWith(workspaceDirectoryPath)) {
		  // The previous approach with subModelDirectoryPath.split(workspaceDirectoryPath) 
		  // doesn't work well with windows paths.
			String subModelsPath = subModelDirectoryPath.substring(workspaceDirectoryPath.length());
			String subModelFilePath = subModelsPath+"/"+name+".moml";
			file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(subModelFilePath);
		} else {
			Flow flow = Activator.getDefault().getRepositoryService().getSubmodel(name);
			final IProject pass = ModelUtils.getPasserelleProject();
	
			file = pass.getFile(name + ".moml");
			StringWriter writer = new StringWriter();
			flow.exportMoML(writer);
	
			final ByteArrayInputStream contents = new ByteArrayInputStream(writer.toString().getBytes());
			if (!file.exists()) {
				file.create(contents, true, null);
			} else {
				file.setContents(contents, true, true, null);
			}
		}
		final IPasserelleMultiPageEditor ed = (IPasserelleMultiPageEditor) EclipseUtils.openEditor(file, PasserelleModelMultiPageEditor.ID);
		ed.setPasserelleEditorActive();
	}

}
