package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.EventObject;

public class ExecuteActionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8386407740735359594L;

	private boolean isEnabled;
	public ExecuteActionEvent(Object source, final boolean isEnabled) {
		super(source);
		this.isEnabled = isEnabled;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

}
