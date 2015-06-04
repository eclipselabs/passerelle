package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class DeleteAttributeHandler extends AbstractHandler implements IViewActionDelegate, ISelectionListener {

	private static Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
	private IViewPart view;
	

	public void run(IAction action) {
        doAction();		
	}


	public Object execute(ExecutionEvent event) throws ExecutionException {
        doAction();		
		return Boolean.TRUE;
	}

	private void doAction() {
		
		final IEditorPart ed = EclipseUtils.getActivePage().getActiveEditor();
		if (ed==null) return;
		
		final ActorAttributesView attView = (ActorAttributesView)EclipseUtils.getActivePage().getActivePart();
		try {
			attView.deleteSelectedParameter();
		} catch (IllegalActionException e) {
			logger.error("Cannot delete parameter ", e);
		}
	}



	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		selectionChanged(selection);
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		selectionChanged(selection);
	}

	private void selectionChanged(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			this.isEnabled = ((StructuredSelection)selection).getFirstElement() instanceof Attribute;
			fireHandlerChanged(new HandlerEvent(this, true, false));
		}
	}

	public void init(IViewPart view) {
		this.view = view;
		view.getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}
	
	public void dispose() {
		super.dispose();
		view.getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
	}


	private boolean isEnabled = false;
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(Object sel) {
		this.isEnabled = sel instanceof Attribute;
		fireHandlerChanged(new HandlerEvent(this, true, false));
	}
}
