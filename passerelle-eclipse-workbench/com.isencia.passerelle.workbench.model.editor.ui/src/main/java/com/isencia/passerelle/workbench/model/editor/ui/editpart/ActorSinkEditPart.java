package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;

public class ActorSinkEditPart extends ActorEditPart {
	   
	public final static Color ACTOR_BACKGROUND_COLOR = new Color(null,252,175,62);

	@Override
	protected IFigure createFigure() {
		ActorFigure createFigure = (ActorFigure) super.createFigure();
		createFigure.setBackgroundColor(ACTOR_BACKGROUND_COLOR);
		return createFigure;
	}

}
