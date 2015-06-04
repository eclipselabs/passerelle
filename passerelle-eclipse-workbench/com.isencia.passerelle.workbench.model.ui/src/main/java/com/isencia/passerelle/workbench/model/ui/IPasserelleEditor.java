package com.isencia.passerelle.workbench.model.ui;

import org.eclipse.draw2d.IFigure;

import ptolemy.actor.CompositeActor;

public interface IPasserelleEditor {
	CompositeActor getContainer();
	IFigure        getWorkflowFigure();
}
