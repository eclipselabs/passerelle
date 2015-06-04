package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Image;

public class CommentFigure extends Label {

	public CommentFigure(String label, Image image) {
		
		super();
		ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(false);
		setLayoutManager(layout);
		setForegroundColor(ColorConstants.black);
		
		setText(label);
		setIcon(image);
  	  	setOpaque(false);
  	  	
	}
	
	public void setText(String text) {
		text = text.replace("\\n", "\n");
		super.setText(text);
	}

}
