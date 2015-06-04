package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.swt.graphics.Color;

/**
 * 
 */
public class DiagramNonResizableEditPolicy extends NonResizableEditPolicy {

	private static final Color background = new Color(null, 30, 30, 30);

	/**
	 * Creates the figure used for feedback.
	 * 
	 * @return the new feedback figure
	 */
	protected IFigure createDragSourceFeedbackFigure() {
		IFigure figure = createFigure((GraphicalEditPart) getHost(), null);

		figure.setBounds(getInitialFeedbackBounds());
		addFeedback(figure);
		return figure;
	}

	protected IFigure createFigure(GraphicalEditPart part, IFigure parent) {
		IFigure child = getCustomFeedbackFigure(part.getModel());

		if (parent != null)
			parent.add(child);

		Rectangle childBounds = part.getFigure().getBounds().getCopy();

		IFigure walker = part.getFigure().getParent();

		while (walker != ((GraphicalEditPart) part.getParent()).getFigure() && walker != null) {
				walker.translateToParent(childBounds);
				walker = walker.getParent();
		}

		child.setBounds(childBounds);

		Iterator<?> i = part.getChildren().iterator();

		while (i.hasNext())
			createFigure((GraphicalEditPart) i.next(), child);

		return child;
	}

	/**
	 * Creates the figure that will be shown to the user during drag
	 * 
	 * @param modelPart
	 *            part for which to create the feedback figure
	 * @return the created figure
	 */
	protected IFigure getCustomFeedbackFigure(Object modelPart) {
		IFigure figure;

		// Based on the modelPart, we could create another figure that is shown
		// during drag
		figure = new RectangleFigure();
		((RectangleFigure) figure).setXOR(true);
		((RectangleFigure) figure).setFill(true);
		figure.setBackgroundColor(background);
		figure.setForegroundColor(ColorConstants.white);

		return figure;
	}

	/**
	 * Returns the layer used for displaying feedback.
	 * 
	 * @return the feedback layer
	 */
	protected IFigure getFeedbackLayer() {
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#getInitialFeedbackBounds()
	 */
	protected Rectangle getInitialFeedbackBounds() {
		return getHostFigure().getBounds();
	}

}
