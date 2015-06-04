/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.workbench.model.editor.graphiti.wizard;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.eclipse.graphiti.dt.IDiagramType;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.isencia.passerelle.workbench.model.editor.graphiti.Messages;
import com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleGraphitiEditorPlugin;

public class DiagramTypeWizardPage extends AbstractWizardPage implements ITextProvider {

	private static final String PAGE_DESC = Messages.DiagramTypeWizardPage_PageDescription;
	private static final String PAGE_TITLE = Messages.DiagramTypeWizardPage_PageTitle;

	private static final String DEFAULT_TYPE = "tutorial"; //$NON-NLS-1$
	private static final String SELECTED_TYPE = "selectedtype"; //$NON-NLS-1$

	Combo comboBox;

	public DiagramTypeWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	protected DiagramTypeWizardPage(String pageName) {
		super(pageName);
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESC);
	}

	@Override
	protected void createWizardContents(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setFont(parent.getFont());
		projectLabel.setText(Messages.DiagramTypeWizardPage_DiagramTypeField);

		// new project name entry field
		comboBox = new Combo(projectGroup, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 250;
		comboBox.setLayoutData(data);
		comboBox.setFont(parent.getFont());
		comboBox.setVisibleItemCount(12);
		comboBox.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IDialogSettings dialogSettings = PasserelleGraphitiEditorPlugin.getDefault().getDialogSettings();
				dialogSettings.put(SELECTED_TYPE, comboBox.getText());
			}
		});

		// set the contents of the Combo-widget
		comboBox.setItems(getAllAvailableDiagramTypes());
		if (getInitialValue() != null) {
			comboBox.setText(getInitialValue());
		}
	}

	protected String[] getAllAvailableDiagramTypes() {
		Vector<String> diagramIds = new Vector<String>();
		for (IDiagramType diagramType : GraphitiUi.getExtensionManager().getDiagramTypes()) {
			diagramIds.add(diagramType.getId());
		}

		return diagramIds.toArray(new String[] {});
	}

	protected String getInitialValue() {
		// Get last choice
		IDialogSettings dialogSettings = PasserelleGraphitiEditorPlugin.getDefault().getDialogSettings();
		String selType = dialogSettings.get(SELECTED_TYPE);
		List<String> asList = Arrays.asList(comboBox.getItems());
		if (asList.contains(selType)) {
			return selType;
		} else if (asList.contains(DEFAULT_TYPE)) {
			return DEFAULT_TYPE;
		}
		return null;
	}

	public String getText() {
		return comboBox.getText();
	}
}
