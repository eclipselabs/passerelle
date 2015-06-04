package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.workbench.model.editor.ui.cell.FileBrowserEditor;

public class FilePickerPropertyDescriptor extends PropertyDescriptor {
	
	private String[] fileFilter;
	private String   currentPath;

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public FilePickerPropertyDescriptor(Object id, String displayName) {
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
		FileBrowserEditor editor = new FileBrowserEditor(parent);
		editor.setFilter(fileFilter);
		editor.setCurrentPath(currentPath);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}

	public void setFilter(String[] filter) {
		this.fileFilter = filter;
	}

	public void setCurrentPath(String filterPath) {
		this.currentPath = filterPath;
	}

}
