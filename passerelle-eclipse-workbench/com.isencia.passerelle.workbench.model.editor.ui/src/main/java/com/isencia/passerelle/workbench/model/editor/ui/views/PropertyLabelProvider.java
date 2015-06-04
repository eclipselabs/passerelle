package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;

import ptolemy.kernel.util.Attribute;

public class PropertyLabelProvider extends ColumnLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element == null) return "";
		if (element instanceof GeneralAttribute){
			GeneralAttribute.ATTRIBUTE_TYPE type = ((GeneralAttribute)element).getType();
			switch (type){
				case NAME:
					return "Name";
				case TYPE:
					return "Type";
				case CLASS:
					return "Class";
			}
			return "";
		}
			final Attribute att = (Attribute)element;
		return att.getDisplayName();
	}

	private final Color lightGrey = new Color(null, 240,240,240);

	public Color getBackground(Object element) {
		if (element instanceof GeneralAttribute) return lightGrey;
		return null;
	}
	
	public void dispose() {
		super.dispose();
		lightGrey.dispose();
	}
}
