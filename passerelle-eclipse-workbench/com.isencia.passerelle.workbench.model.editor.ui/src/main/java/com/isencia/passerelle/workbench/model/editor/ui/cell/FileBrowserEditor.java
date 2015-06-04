package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class FileBrowserEditor extends DialogCellEditor {

	protected String   stringValue = "";
	protected String[] fileFilter;
	protected String   currentPath;

	public FileBrowserEditor(Composite aComposite) {
		super(aComposite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.
	 * swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) {
		Display display = cellEditorWindow.getDisplay();
		FileDialog fd = new FileDialog(display.getActiveShell(), SWT.OPEN);
		fd.setText("Open");
		if (currentPath!=null) {
			fd.setFilterPath(currentPath);
		}
		 
		if (fileFilter!=null) {
			fd.setFilterExtensions(fileFilter);
		}
		if (getValue() != null) {
			fd.setFileName((String) getValue());
		}
		String selected = fd.open();
		return selected;
	}

	public void setFilter(String[] fileFilter) {
		this.fileFilter = fileFilter;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

}
