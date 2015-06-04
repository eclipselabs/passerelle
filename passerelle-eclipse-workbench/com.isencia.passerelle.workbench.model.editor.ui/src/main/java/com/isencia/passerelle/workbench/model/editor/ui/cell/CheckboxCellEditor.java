package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CheckboxCellEditor extends CellEditor {
	boolean value = false;
	private static final int defaultStyle = SWT.NONE;

	public CheckboxCellEditor() {
		setStyle(defaultStyle);
	}

	public CheckboxCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	public CheckboxCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	public void activate() {
		value = !value;
		fireApplyEditorValue();
	}

	protected Control createControl(Composite parent) {
		return null;
	}

	protected Object doGetValue() {
		return value ? Boolean.TRUE : Boolean.FALSE;
	}

	protected void doSetFocus() {
		// Ignore
	}

	protected void doSetValue(Object value) {
		Assert.isTrue(value instanceof Boolean);
		this.value = ((Boolean) value).booleanValue();
	}

	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		if (activationEvent.eventType != ColumnViewerEditorActivationEvent.TRAVERSAL) {
			super.activate(activationEvent);
		}
	}
}
