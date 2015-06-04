package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.gef.requests.CreationFactory;


public class ClassTypeFactory implements CreationFactory {

	private Class<?> type;
	private Object name;
	public ClassTypeFactory(Class<?> type,Object name) {
		this.type = type;
		this.name = name;
	}

	public Object getNewObject() {
		return name;
	}

	public Object getObjectType() {
		return type;
	}

}
