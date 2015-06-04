package com.isencia.passerelle.workbench.model.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PasserellePerspective implements IPerspectiveFactory {
	
	public static final String ID = "com.isencia.passerelle.workbench.model.ui.perspective";


	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
				
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.35f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
		navigatorFolder.addView("com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeView");

		IFolderLayout outlineFolder = layout.createFolder("outline-folder",IPageLayout.BOTTOM,0.6f,"navigator-folder");
        outlineFolder.addView(IPageLayout.ID_OUTLINE);
        
        IFolderLayout viewFolder = layout.createFolder("view-folder",IPageLayout.BOTTOM,0.6f,editorArea);
        // This view is specific to passerelle and is simpler and nicer to use than standard properties view.
//        viewFolder.addView(IPageLayout.ID_PROP_SHEET);
        viewFolder.addView("com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView");
//        viewFolder.addView("com.isencia.passerelle.workbench.model.editor.ui.views.execTrace.ExecutionTracesView");
        viewFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        viewFolder.addView("org.eclipse.pde.runtime.LogView");
        
        // Ensure that the run menu is visible
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        
	}
}
