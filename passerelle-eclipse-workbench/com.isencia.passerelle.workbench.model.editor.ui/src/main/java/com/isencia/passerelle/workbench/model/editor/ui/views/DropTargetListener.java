package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropTargetListener extends ViewerDropAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(DropTargetListener.class);

	public DropTargetListener(TreeViewer viewer) {
		super(viewer);
	}



	@Override
	public boolean performDrop(Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		// TODO Auto-generated method stub
		return false;
	}
}