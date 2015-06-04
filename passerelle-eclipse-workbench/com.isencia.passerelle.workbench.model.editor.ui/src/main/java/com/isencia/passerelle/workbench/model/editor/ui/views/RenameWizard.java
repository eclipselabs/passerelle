package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.wizard.Wizard;

public class RenameWizard extends Wizard {
	

	private String newName;
	private RenamePage renamePage;

	public RenameWizard(String oldName) {
		this.renamePage = new RenamePage("RenameActorPage", oldName);
		addPage(renamePage);
	}

	@Override
	public boolean performFinish() {
		final String name = renamePage.getName();
		if (name==null || "".equals(name)) {
			renamePage.setErrorMessage("Name is required");
			return false;
		}
		this.newName = name;
		return true;
	}

	public String getRenameValue() {
		return newName;
	}
}