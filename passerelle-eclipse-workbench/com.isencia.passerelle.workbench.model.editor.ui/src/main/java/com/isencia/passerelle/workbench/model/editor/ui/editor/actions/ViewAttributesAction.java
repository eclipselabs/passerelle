package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.List;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.properties.ActorDialog;

public class ViewAttributesAction extends SelectionAction {
	public static final String ID = "edit";
	public static ImageDescriptor ICON = Activator.getImageDescriptor("icons/edit.gif");
	public static ImageDescriptor ICON_DISABLED = Activator.getImageDescriptor("icons/edit.gif");

	public ViewAttributesAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setText("Edit Attributes");
		setId(ID);
		setHoverImageDescriptor(ICON);
		setImageDescriptor(ICON);
		setDisabledImageDescriptor(ICON_DISABLED);
		setEnabled(false);
	}

	@Override
	protected boolean calculateEnabled() {
		boolean check = checkSelectedObjects();
		return check;
	}

	private boolean checkSelectedObjects() {
		if (getSelectedObjects() == null)
			return false;
		for (Object o : getSelectedObjects()) {
			if (o instanceof EditPart) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		try {
			List selection = getSelectedObjects();
			for (Object o : selection) {
				if (o instanceof EditPart) {
						ActorDialog dialog = new ActorDialog(getWorkbenchPart(), (NamedObj) ((EditPart) o).getModel());
						dialog.open();
						return;
				}
			}
		} catch (Exception e1) {
		}
	}

}
