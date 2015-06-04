package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor.EDataTypeCellEditor;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor.IntegerCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class IntegerPropertyDescriptor extends PropertyDescriptor {
	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public IntegerPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
		
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new EDataTypeCellEditor(EcorePackage.Literals.EBIG_INTEGER ,parent);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}

}
