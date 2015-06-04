package com.isencia.passerelle.workbench.model.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class NameWizard extends Wizard {
	

	private String newName;
	private NamePage page;

	public NameWizard(final String oldName, final NameChecker checker) {
		this.page = new NamePage("NameCompositeWizard", oldName, checker);
		addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		this.newName = page.getName();
		return true;
	}

	public String getRenameValue() {
		return newName;
	}
}