package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.Clickable;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;

public class CustomFigureEditPart extends ActorEditPart {
	
	private IActorFigureProvider provider;

	public CustomFigureEditPart() {
		
	}
	
	public CustomFigureEditPart(IActorFigureProvider prov) {
		this.provider = prov;
	}

	public void setFigureProvider(final IActorFigureProvider provider) {
		this.provider = provider;
	}

	protected ActorFigure getActorFigure(String displayName, Image createImage, Clickable[] clickables) {
		ActorFigure fig = provider.getActorFigure(displayName, createImage, clickables);
		if (fig==null) fig = super.getActorFigure(displayName, createImage, clickables);
		return fig;
	}

}
