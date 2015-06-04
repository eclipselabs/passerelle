package com.isencia.passerelle.workbench.model.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

class NamePage extends WizardPage {
	/**
	 * 
	 */
	private String oldName;
	private String newName;
	private NameChecker checker;

	public NamePage(final String pageName, final String oldName, NameChecker checker) {
		super(pageName);
		this.oldName = oldName;
		this.newName = oldName;
		this.checker = checker;
		setTitle("Name");
		setDescription("Name of composite");
	}


	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label lab = new Label(composite, SWT.NONE);
		lab.setText("Name of composite ");
		final Text nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		nameText.setText(oldName);
		nameText.addModifyListener(new ModifyListener() {	

			public void modifyText(ModifyEvent e) {
				newName = nameText.getText();
				getContainer().updateMessage();
			}
		});
		setControl(composite);
	}
	
	public String getName() {
		return newName;
	}
	
	public String getErrorMessage() {
		if (getName()==null || "".equals(getName())) {
			return "Name is required";
		}
		if (!checker.isNameValid(getName())) {
			return checker.getErrorMessage(getName());
		}

		return null;
	}
}