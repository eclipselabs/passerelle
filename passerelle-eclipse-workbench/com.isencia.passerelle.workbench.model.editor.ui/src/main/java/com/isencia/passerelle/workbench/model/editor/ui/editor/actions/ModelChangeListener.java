package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.EventListener;

public interface ModelChangeListener extends EventListener {

	/**
	 * Called on termination
	 * @param evt
	 */
	public void executionStarted(final ModelChangeEvent evt);
	
	/**
	 * Called on termination
	 * @param evt
	 */
	public void executionTerminated(final ModelChangeEvent evt);
}
