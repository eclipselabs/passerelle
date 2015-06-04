package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.Action;

import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RouterFactory.CONNECTION_TYPE;

public class ConnectionAction extends Action {

	private CONNECTION_TYPE type;

	public ConnectionAction(CONNECTION_TYPE type) {
		super("", Action.AS_CHECK_BOX);
		this.type = type;
		setId(getClass().getName()+type.hashCode());
		setChecked(type.equals(RouterFactory.getRouterType()));
	}
	
	@Override
	public void run() {
		RouterFactory.setConnectionType(type);
	}

}
