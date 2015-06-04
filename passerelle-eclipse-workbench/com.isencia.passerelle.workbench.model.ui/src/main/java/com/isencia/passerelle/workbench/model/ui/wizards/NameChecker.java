package com.isencia.passerelle.workbench.model.ui.wizards;

public interface NameChecker {

	public boolean isNameValid(final String name);
	public String  getErrorMessage(final String name);
}
