package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.workbench.model.editor.ui.cell.ResourceBrowserEditor;

public class ResourcePropertyDescriptor extends PropertyDescriptor {
	
	private ResourceParameter param;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public ResourcePropertyDescriptor(ResourceParameter param) {
		super(param.getName(), param.getDisplayName());
		this.param        = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new ResourceBrowserEditor(parent, param);
		if (getValidator() != null) editor.setValidator(getValidator());
		return editor;
	}

}
