package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.IBody;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class CompoundOutputFigure extends CompoundIOFigure {
  public static final String OUTPUT_PORT_NAME = "output";

  public CompoundOutputFigure(String name, Class type) {
    super(name, type);
    addInput(OUTPUT_PORT_NAME, OUTPUT_PORT_NAME);
    setBackgroundColor(ColorConstants.white);
    setBorder(null);
  }

  @Override
  protected Color getBackGroundcolor() {
    return ColorConstants.white;
  }

  private class Body extends RectangleFigure implements IBody {
    /**
     * @param s
     */
    public Body() {
      BorderLayout layout = new BorderLayout();
      setLayoutManager(layout);
      setBorder(null);

      setBackgroundColor(ColorConstants.white);
      setOpaque(true);
      Image image = PaletteBuilder.getInstance().getIcon(type).createImage();

      final Label label = new Label(image);
      label.setBackgroundColor(ColorConstants.white);
      label.setOpaque(false);
      label.setBorder(null);
      add(label, BorderLayout.CENTER);
    }

    public void initClickable(Clickable clickable) {
      if (clickable != null) {
        add(clickable, BorderLayout.BOTTOM);
      }
    }

    public Dimension getPreferredSize(int wHint, int hHint) {
      Dimension size = getParent().getSize().getCopy();
      return size;
    }

    public void initImage(Image image) {

    }

  }

  protected IFigure generateBody(Image image, Clickable[] clickables) {
    Body body = new Body();
    body.setBorder(null);
    for (Clickable clickable : clickables)
      body.initClickable(clickable);
    return (body);
  }
}
