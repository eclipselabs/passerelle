package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.IBody;

public class EllipseActorFigure extends ActorFigure {
	
	public EllipseActorFigure(String name,Image image, Clickable[] clickables) {
		this(name,null, image, clickables);
	}

	public EllipseActorFigure(String name,Class type, Image image, Clickable[] clickables) {
		super(name,type, image, clickables);
	}

	protected IFigure generateBody(Image image, Clickable[] clickables) {
		Body body = new Body();
		//body.setBorder(new LineBorder(new Color(null, 225,225,225), 1, Graphics.LINE_SOLID));
		body.initImage(image);
		for (Clickable clickable : clickables)
			body.initClickable(clickable);
		return body;
	}

	private class Body extends Ellipse implements IBody {
		/**
		 * @param s
		 */
		public Body() {
			BorderLayout layout = new BorderLayout();
			setLayoutManager(layout);
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
		protected void outlineShape(Graphics graphics) {
			
			graphics.setForegroundColor(ColorConstants.gray);
			super.outlineShape(graphics);
		}

		protected void fillShape(Graphics graphics) {
			graphics.setBackgroundColor(getBackgroundColor());
			super.fillShape(graphics);
		}

		public Dimension getPreferredSize(int wHint, int hHint) {
			Dimension size = getParent().getSize().getCopy();
			return size;
		}

	}
}
