package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.EventObject;
import java.util.Set;

public class ModelChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2271985931037709671L;
	private int returnCode;


	public ModelChangeEvent(Object source, final int code) {
		super(source);
		this.returnCode = code;
	}

	public ModelChangeEvent(Object source) {
		this(source, -1);
	}

	public int getReturnCode() {
		return returnCode;
	}
}
