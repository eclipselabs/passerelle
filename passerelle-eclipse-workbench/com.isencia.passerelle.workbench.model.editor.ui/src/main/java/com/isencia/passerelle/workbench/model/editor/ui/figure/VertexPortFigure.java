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
public class VertexPortFigure extends PortFigure {



	public VertexPortFigure(String name, int width, int height, Color color) {
		super(name, width, height);
		setFillColor(color);
	}

	protected void outlineShape(Graphics graphics) {

		graphics.setBackgroundColor(getFillColor());
		graphics.setForegroundColor(ColorConstants.black);

		PointList pts = new PointList();
		if (getName().equals(VertexFigure.VERTEX_OUTPUT)) {
			pts.addPoint(bounds.getTopLeft());
			pts.addPoint(bounds.getRight());
			pts.addPoint(bounds.getBottomLeft());
			pts.addPoint(bounds.getTopLeft());
		} else {
			pts.addPoint(bounds.getTopRight());
			pts.addPoint(bounds.getLeft());
			pts.addPoint(bounds.getBottomRight());
			pts.addPoint(bounds.getTopRight());
		}

		graphics.fillPolygon(pts);
//		graphics.drawPolyline(pts);
	}

}
