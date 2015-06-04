package com.isencia.passerelle.workbench.model.ui;

import com.isencia.passerelle.editor.common.model.LinkHolder;

import ptolemy.actor.CompositeActor;

public interface IPasserelleMultiPageEditor extends LinkHolder{
	CompositeActor getSelectedContainer();
	CompositeActor getModel();
	IPasserelleEditor getSelectedPage();
	void selectPage(CompositeActor actor);
	void setPasserelleEditorActive();
	void removePage(int pageIndex);
}
