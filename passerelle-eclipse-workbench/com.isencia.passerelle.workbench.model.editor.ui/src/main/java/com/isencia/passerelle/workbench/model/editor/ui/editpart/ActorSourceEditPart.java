package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.PortFigure;

public class ActorSourceEditPart extends ActorEditPart {
	
	public final static Color PORT_FILL_COLOR = new Color(null,255,225,20);
	public final static Color ACTOR_BACKGROUND_COLOR = new Color(null,138,226,52);

	@Override
	protected IFigure createFigure() {
		ActorFigure createFigure = (ActorFigure) super.createFigure();
		createFigure.setBackgroundColor(ACTOR_BACKGROUND_COLOR);
		
		// Make trigger yellow
		final PortFigure trigger = createFigure.getInputPort("trigger");
		if (trigger!=null) {
			trigger.setFillColor(PORT_FILL_COLOR);
		}
		
		return createFigure;
	}

}
