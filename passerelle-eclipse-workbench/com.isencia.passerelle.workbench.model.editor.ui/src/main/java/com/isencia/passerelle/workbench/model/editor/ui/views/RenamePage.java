package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

class RenamePage extends WizardPage {
  /**
	 * 
	 */
  private String oldName;
  private String newName;

  public RenamePage(final String pageName, final String oldName) {
    super(pageName);
    this.oldName = oldName;
    setTitle("Rename");
    setDescription("Rename an actor");
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    Label lab = new Label(composite, SWT.NONE);
    lab.setText("Rename to: ");
    final Text nameText = new Text(composite, SWT.BORDER);
    nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    nameText.setText(oldName);
    nameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        newName = nameText.getText();
      }
    });
    setControl(composite);
  }

  public String getName() {
    return newName;
  }
}