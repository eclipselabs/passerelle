package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;

/**
 * Figure used to draw output ports
 * 
 * @author Dirk Jacobs
 * 
 */
public class ActorPortFigure extends PortFigure {

	public ActorPortFigure(String name) {
		super(name);
		setFillColor(ColorConstants.white);
	}
	public ActorPortFigure(String name,int width,int height,Color color) {
		super(name,width,height);
		setFillColor(color);
	}
	protected void outlineShape(Graphics graphics) {
		
		graphics.setBackgroundColor(getFillColor());
		graphics.setForegroundColor(ColorConstants.gray);

		PointList pts = new PointList();
		pts.addPoint(bounds.getTopLeft());
		pts.addPoint(bounds.getTopLeft().x + width - 1, bounds.y
				+ height / 2);
		pts.addPoint(bounds.getBottomLeft());
		pts.addPoint(bounds.getTopLeft());
		graphics.fillPolygon(pts);
		graphics.drawPolyline(pts);
	}


}
