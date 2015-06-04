package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

public class AppliableTableViewer extends TableViewer {

	public AppliableTableViewer(Composite parent, int style) {
		super(parent, style);
	}
	public void applyEditorValue() {
		super.applyEditorValue();
	}		
}