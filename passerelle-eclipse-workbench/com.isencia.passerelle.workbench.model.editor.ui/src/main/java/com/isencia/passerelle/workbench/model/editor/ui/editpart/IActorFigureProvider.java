package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.Clickable;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;

public interface IActorFigureProvider {

	/**
	 * A method which can return an alternative actor figure
	 * @param actorFigure
	 * @return
	 */
	public ActorFigure getActorFigure(String displayName, Image createImage, Clickable[] clickables);

}
