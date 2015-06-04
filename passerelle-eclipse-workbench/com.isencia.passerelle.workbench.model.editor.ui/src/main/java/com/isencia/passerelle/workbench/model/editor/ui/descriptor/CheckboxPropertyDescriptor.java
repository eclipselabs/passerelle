package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.cell.CheckboxCellEditor;

public class CheckboxPropertyDescriptor extends PropertyDescriptor {
	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return super.getDisplayName();
	}

	private Image ticked, unticked;
	/**
	 * @param id
	 * @param displayName
	 */
	public CheckboxPropertyDescriptor(Object id, String displayName) {
		
		super(id, displayName);
		
		ticked   = Activator.getImageDescriptor("icons/ticked.png").createImage();
		unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
		
		setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (Boolean.TRUE.equals(element)) {
					return ticked;
				} else {
					return unticked;
				}
			}
			public String getText(Object element) {
				return null;
			}			
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		CellEditor editor = new CheckboxCellEditor(parent,SWT.CHECK);
		if (getValidator() != null)
			editor.setValidator(getValidator());
		return editor;
	}

}
