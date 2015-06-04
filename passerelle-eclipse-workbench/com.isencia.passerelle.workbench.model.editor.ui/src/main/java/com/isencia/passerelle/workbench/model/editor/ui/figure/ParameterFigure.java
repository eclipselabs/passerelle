package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import com.isencia.passerelle.workbench.model.editor.ui.INameable;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class ParameterFigure extends Figure implements INameable {

  private static final Font NAME_FONT;
  static {
    Font f = JFaceResources.getDefaultFont();
    FontData fd = f.getFontData()[0];
    FontData nameFd = new FontData(fd.getName(), fd.getHeight(), SWT.BOLD);
    NAME_FONT = new Font(f.getDevice(), nameFd);
  }
  protected Label nameLabel;
  private Body body;

  public ParameterFigure(String name, String expression, Class<?> type, Image image) {
    ToolbarLayout layout = new ToolbarLayout();
    layout.setHorizontal(true);
    layout.setSpacing(5);
    setLayoutManager(layout);
    nameLabel = new Label(name, image);
    nameLabel.setOpaque(true);
    add(nameLabel);
    nameLabel.setFont(NAME_FONT);
    setToolTip(new Label(name));
    if (type != null && PaletteBuilder.getInstance().getType(type) != null) {
      setToolTip(new Label(PaletteBuilder.getInstance().getType(type)));
    }
    setOpaque(false);
    add(new Label(":"));
    body = new Body(expression);
    add(body);
  }
  
  public void setText(String text) {
    body.setText(text);
  }

  private class Body extends Label {
    public Body(String expression) {
      setForegroundColor(ColorConstants.black);
      setText(expression);
      setOpaque(false);
    }
    public void setText(String text) {
      text = text.replace("\\n", "\n");
      super.setText(text);
    }
  }

  public void validate() {
    LayoutManager layout = getLayoutManager();
    layout.setConstraint(body, new Rectangle(0, 0, -1, -1));
    super.validate();
  }

  public void setBackgroundColor(Color c) {
    if (body != null) {
      body.setBackgroundColor(c);
    }
  }
  
  public String getName() {
    return this.nameLabel.getText();
  }

  public void setName(String name) {
    this.nameLabel.setText(name);
    IFigure tt = getToolTip();
    if (tt != null && tt instanceof Label) {
      ((Label) tt).setText(name);
    } else {
      setToolTip(new Label(name));
    }
  }
}
