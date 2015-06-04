package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.IBody;

public class RectangularActorFigure extends ActorFigure {

	public RectangularActorFigure(String name, Class type, Image image,
			Clickable[] clickables) {
		super(name, type, image, clickables);
	}

	protected IFigure generateBody(Image image, Clickable[] clickables) {
		Body body = new Body(getColor());
		body.initImage(image);
		for (Clickable clickable : clickables)
			body.initClickable(clickable);
		return (body);
	}

	private class Body extends RoundedRectangle implements IBody {
		/**
		 * @param s
		 */
		public Body(Color color) {
			BorderLayout layout = new BorderLayout();
			setLayoutManager(layout);
			setBackgroundColor(getColor());
			setOpaque(true);
		}

		public void initImage(Image image) {
			if (image != null) {
				ImageFigure imageFigure = new ImageFigure(image);
				imageFigure.setAlignment(PositionConstants.CENTER);
				add(imageFigure, BorderLayout.CENTER);
			}
		}

		public void initClickable(Clickable clickable) {
			if (clickable != null) {
				add(clickable, BorderLayout.BOTTOM);
			}
		}

		protected void fillShape(Graphics graphics) {
			graphics.pushState();
			graphics.setForegroundColor(ColorConstants.white);
			graphics.setBackgroundColor(getBackgroundColor());
			final Rectangle bounds = getBounds();
			graphics.fillGradient(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2, false);
			graphics.popState();
		}
		protected void outlineShape(Graphics graphics) {
			
			graphics.setForegroundColor(ColorConstants.gray);
			super.outlineShape(graphics);
		}

		public Dimension getPreferredSize(int wHint, int hHint) {
			Dimension size = getParent().getSize().getCopy();
			return size;
		}

	}
}
