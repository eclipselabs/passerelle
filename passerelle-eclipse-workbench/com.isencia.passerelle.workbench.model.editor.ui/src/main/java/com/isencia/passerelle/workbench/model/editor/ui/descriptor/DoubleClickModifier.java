package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 *
 */
public abstract class DoubleClickModifier implements ICellModifier {

	protected boolean enabled;
	
	protected DoubleClickModifier(final ColumnViewer viewer) {
		viewer.getControl().addListener(SWT.MouseDown, new Listener() {
		
			public void handleEvent(Event event) {
				setEnabled(false);
			}
		});
		viewer.getControl().addListener(SWT.MouseDoubleClick, new Listener() {
			
			public void handleEvent(Event event) {
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection.toList().size() != 1) {
					return;
				}
				final ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
				setEnabled(true);
				viewer.editElement(selection.getFirstElement(),cell!=null?cell.getColumnIndex():0);
			}
		});

	}
	
	/**
	 * The editor can be disabled, useful for double click table editing
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	

	public boolean canModify(Object element, String property) {
		return enabled;
	}

}
