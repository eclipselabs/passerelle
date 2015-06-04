package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBookView;

import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeView;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeViewerPage;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class SubModelViewUtils {

	/**
	 * There are better ways of doing this, for instance telling the workbench that
	 * content has changed, using services, etc.
	 * 
	 * However this seems to work for now.
	 */
	public static void refreshPallette() {
		
		// Refresh Palette view if its there
		final IViewPart part = EclipseUtils.getPage().findView(ActorTreeView.ID);
        if (part!=null && part instanceof ActorTreeView) {
        	((ActorTreeView)part).refresh();
        }
        
        // Otherwise the pallette may be on a page view
        final IWorkbenchPart wbPart = EclipseUtils.getPage().getActivePart();
        if (wbPart instanceof PageBookView) {
        	final PageBookView pageView = (PageBookView)wbPart;
        	if (pageView.getCurrentPage() instanceof ActorTreeViewerPage) {
        		((ActorTreeViewerPage)pageView.getCurrentPage()).refresh();
        	}
        }
	}

}
