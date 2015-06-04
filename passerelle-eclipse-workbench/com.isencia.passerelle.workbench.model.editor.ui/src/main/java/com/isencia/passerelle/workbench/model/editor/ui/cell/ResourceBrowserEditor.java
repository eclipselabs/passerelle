package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.eclipse.resources.util.ResourceUtils;
import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ResourceBrowserEditor extends DialogBrowserEditor {

	private static final Logger logger = LoggerFactory.getLogger(ResourceBrowserEditor.class);
	protected String            stringValue = "";
	protected ResourceParameter param;

	public ResourceBrowserEditor(Composite aComposite, ResourceParameter param) {
		super(aComposite);
		this.param = param;
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.
	 * swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow, Object textValue) {
		
		IResource currentValue=null;
		try {
			currentValue = getSelectedResource((String)textValue);
		} catch (Exception e1) {
			logger.error("Cannot get resource!", e1);
		}
        final Actor     actor        = (Actor)param.getContainer();
        final Parameter folder       = (Parameter)actor.getAttribute("Folder");
        final Parameter relative     = (Parameter)actor.getAttribute("Relative Path");
        boolean         isFolder     = false;
        try {
        	if (param.getResourceType()==IResource.FOLDER){
           		isFolder = true;
        	} else if (folder==null) {
        		isFolder = currentValue!=null ? currentValue instanceof IContainer : false;
        	} else {
        		isFolder     = ((BooleanToken)folder.getToken()).booleanValue();
        	}
		} catch (IllegalActionException e) {
			logger.error("Cannot read folder parameter", e);
		}
		IResource value = null;
		if (isFolder) {
			IContainer[] folders = WorkspaceResourceDialog.openFolderSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
                    "Choose folder in workspace", 
                    "Please choose a folder from the workspace.\nIf your folder is outside please import the file to a project first.\nNote that if you want this node to be a file set the 'Folder' parameter to false.",
                    false,
                    new Object[]{currentValue},
                    null);

			if (folders!=null && folders.length>0) value = folders[0];
		} else {
			IFile[] files = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
	        		                     "Choose file in workspace", 
	        		                     "Please choose a file from the workspace.\nIf your file is outside please import the file to a project first.\nNote that if you want this node to be a folder set the 'Folder' parameter to true.",
	        		                     false,
	        		                     new Object[]{currentValue},
	        		                     null);
	        
	        if (files!=null && files.length>0) value = files[0];
		}

		if (value==null) return textValue;
		
		if (value.isLinked(IResource.CHECK_ANCESTORS)) {
			// DO NOT USE RawLocation here or the logic for full path
			// will not work
			final String fullPath = value.getLocation().toOSString();
			if (relative!=null) {
				try {
					relative.setToken(new BooleanToken(false));
				} catch (IllegalActionException e) {
					logger.error("Cannot set Relative Path parameter to false",e);
				}
			}
			return fullPath.replace('\\', '/');
		} else {
			final String fullPath = value.getRawLocation().toOSString();
			try {
				if (relative!=null) relative.setToken(new BooleanToken(true));
			} catch (IllegalActionException e) {
				logger.error("Cannot set Relative Path parameter to false",e);
			}
			final String workspace= ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			return fullPath.substring(workspace.length(), fullPath.length()).replace('\\', '/');
		}

		
	}

	private IResource getSelectedResource(String textValue) throws Exception {
		if (textValue!=null) {
			final Actor  actor        = (Actor)param.getContainer();
			final String expandedPath = ModelUtils.substitute(textValue, actor);
			return ResourceUtils.getResource(expandedPath);
		}
		return ResourceUtils.getResource(param);
	}

}
