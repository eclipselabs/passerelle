package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.Clickable;
import org.eclipse.swt.graphics.Image;


public class CompositeActorFigure extends RectangularActorFigure {



	public CompositeActorFigure(String name,Class type, Image image, Clickable[] clickable) {
    	super(name,type, image, clickable);
    }
}
