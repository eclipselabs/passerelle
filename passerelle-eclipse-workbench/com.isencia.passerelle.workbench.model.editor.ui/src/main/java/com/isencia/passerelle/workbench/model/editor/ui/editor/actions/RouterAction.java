package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.Action;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RouterFactory.ROUTER_TYPE;

public class RouterAction extends Action {

	private ROUTER_TYPE type;
	private int         option;

	public RouterAction(ROUTER_TYPE type, final int option) {
		super("", Action.AS_CHECK_BOX);
		this.type   = type;
		this.option = option;
		setId(getClass().getName()+type.hashCode());
		setChecked(type.equals(RouterFactory.getRouterType()));
	}
	
	@Override
	public void run() {
		RouterFactory.setRouter(type);
		Activator.getDefault().getPreferenceStore().setValue(RouterFactory.ROUTER_PREF, option);
	}

}
