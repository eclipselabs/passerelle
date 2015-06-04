package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class ShadowBorder extends AbstractBorder {

  private static final Color SHADOW_COLOR = new Color(null, 221, 221, 221);

  public Insets getInsets(IFigure ifigure) {
    return new Insets(0, 0, 0, 0);
  }

  public void paint(IFigure figure, Graphics graphics, Insets insets) {
    Rectangle rect = getPaintRectangle(figure, insets);
    graphics.setForegroundColor(ColorConstants.darkGray);
    rect.resize(-3, -3);
    graphics.drawRectangle(rect);
    graphics.restoreState();
    graphics.setLineWidth(2);
    rect.resize(2, 2);
    graphics.setForegroundColor(SHADOW_COLOR);
    graphics.drawLine(rect.x + 3, rect.bottom(), rect.right(), rect.bottom());
    graphics.drawLine(rect.right(), rect.y + 3, rect.right(), rect.bottom());
  }

}
