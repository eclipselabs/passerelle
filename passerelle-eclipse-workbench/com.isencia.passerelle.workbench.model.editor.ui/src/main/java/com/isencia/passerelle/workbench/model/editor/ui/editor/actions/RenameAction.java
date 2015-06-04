package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.HashMap;
import java.util.List;


import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.views.RenameWizard;
import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;

public class RenameAction extends SelectionAction {
	
	public RenameAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(false);
	}

	protected void init() {
		setText("Rename...");
		setToolTipText("Rename");
		setId(ActionFactory.RENAME.getId());
		ImageDescriptor icon = AbstractUIPlugin.imageDescriptorFromPlugin(
				"TutoGEF", "icons/rename-icon.png");
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}

	@Override
	protected boolean calculateEnabled() {
//		Command cmd = createRenameCommand("");
//		if (cmd == null)
//			return false;
		return true;
	}

	public Command createRenameCommand(String name,NamedObj model) {
		Request renameReq = new Request("rename");
		HashMap<String, String> reqData = new HashMap<String, String>();
		reqData.put("newName", name);
		renameReq.setExtendedData(reqData);

		try {
			RenameCommand renameCommand = new RenameCommand(model,name);
			return renameCommand;
		} catch (Exception ne) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					"Invalid Name", ne.getMessage());
			return null;
		}
	}

	public void run() {
		NamedObj node = getSelectedNode();
		RenameWizard wizard = new RenameWizard(node.getName());
		WizardDialog dialog = new WizardDialog(getWorkbenchPart().getSite().getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(400, 300);
		dialog.setTitle("Rename Actor");
		dialog.setMessage("This is the same as setting the 'Name' attribute in the Actor Attributes view.");
		if (dialog.open() == WizardDialog.OK) {
			String name = wizard.getRenameValue();
			execute(createRenameCommand(name,node));
		}
	}

	// Helper
	private NamedObj getSelectedNode() {
		List objects = getSelectedObjects();
		if (objects.isEmpty())
			return null;
		if (!(objects.get(0) instanceof EditPart))
			return null;
		EditPart part = (EditPart) objects.get(0);
		return (NamedObj) part.getModel();
	}
}
