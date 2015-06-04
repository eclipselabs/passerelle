package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class SubModelActorFigure extends CompositeActorFigure {

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return ColorConstants.lightBlue;
	}

	public SubModelActorFigure(String name, Class type, Image image,
			Clickable[] clickable) {
		super(name, type, image, clickable);
		// TODO Auto-generated constructor stub
	}

}
