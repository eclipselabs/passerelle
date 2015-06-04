/*
 * Created on Jul 18, 2004
 */
package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;

public class CommentDirectEditPolicy extends DirectEditPolicy {

	private String oldValue;

	/**
	 * @see DirectEditPolicy#getDirectEditCommand(org.eclipse.gef.requests.DirectEditRequest)
	 */
	protected Command getDirectEditCommand(DirectEditRequest request) {
		CellEditor editor = request.getCellEditor();
		
		return null;
	}

	@Override
	protected void showCurrentEditValue(DirectEditRequest request) {
			CellEditor editor = request.getCellEditor();
		
	}

}