package com.isencia.passerelle.workbench.model.editor.ui;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Image;

public interface IBody extends IFigure {
	void initImage(Image image);
	void initClickable(Clickable clickable);
}
