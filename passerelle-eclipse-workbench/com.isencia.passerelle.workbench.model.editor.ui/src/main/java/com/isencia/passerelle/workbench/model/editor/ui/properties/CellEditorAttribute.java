package com.isencia.passerelle.workbench.model.editor.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Control;

public interface CellEditorAttribute {

	/**
	 * Implement this method to provide a custom cell editor for a parameter.
	 * @param control
	 * @return the cell editor
	 */
	public CellEditor createCellEditor(final Control control);
	
	/**
	 * May implement to return null. If implemented the text returned will be used in the Attribute Table.
	 * @return
	 */
	public String getRendererText();
}
