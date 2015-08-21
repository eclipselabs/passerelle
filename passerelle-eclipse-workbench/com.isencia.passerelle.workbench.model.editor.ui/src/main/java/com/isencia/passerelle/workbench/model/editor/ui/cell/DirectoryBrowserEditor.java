package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

public class DirectoryBrowserEditor extends DialogCellEditor {

	protected String   stringValue = "";
	protected String   currentPath;

	public DirectoryBrowserEditor(Composite aComposite) {
		super(aComposite);
	}

	protected Object openDialogBox(Control cellEditorWindow) {
		Display display = cellEditorWindow.getDisplay();
		DirectoryDialog fd = new DirectoryDialog(display.getActiveShell());
		fd.setText("Open");
		if (currentPath!=null) {
			fd.setFilterPath(currentPath);
		}
		String selected = fd.open();
		return selected;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}
}
