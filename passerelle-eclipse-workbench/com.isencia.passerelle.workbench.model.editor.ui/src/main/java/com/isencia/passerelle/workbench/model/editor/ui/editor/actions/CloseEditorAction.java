package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.WorkbenchUtility;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class CloseEditorAction extends SelectionAction {
	private PasserelleModelMultiPageEditor parent;

	public CloseEditorAction(IEditorPart part,
			PasserelleModelMultiPageEditor parent) {
		super(part);
		this.parent = parent;
		setLazyEnablementCalculation(true);
	}

	private final String icon = "icons/close_view.gif";

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Close page");
		setId(ActionFactory.CLOSE.getId());
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);

	}

	@Override
	protected boolean calculateEnabled() {
		return WorkbenchUtility.containsCompositeEntity(getSelectedObjects()) != null;
	}

	@Override
	public void run() {
		parent.removePage(parent.getActivePage());
	}

}
