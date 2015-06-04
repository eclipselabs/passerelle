package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.ui.views.properties.ComboBoxLabelProvider;

public class CComboLabelProvider extends ComboBoxLabelProvider {

	public CComboLabelProvider(String[] values) {
		super(values);
	}
	
    public String getText(Object element) {
        if (element instanceof String) return (String)element;
        return super.getText(element);
    }
}
