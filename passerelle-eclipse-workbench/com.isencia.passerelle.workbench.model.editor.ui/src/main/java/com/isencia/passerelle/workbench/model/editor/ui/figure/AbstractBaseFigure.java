/* Copyright 2014 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

import com.isencia.passerelle.workbench.model.editor.ui.INameable;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class AbstractBaseFigure extends Figure implements INameable {
  public final static int DEFAULT_WIDTH = 60;
  public final static int MIN_HEIGHT = 60;
  public final static Color DEFAULT_BACKGROUND_COLOR = ColorConstants.lightGray;
  public final static Color DEFAULT_FOREGROUND_COLOR = ColorConstants.gray;
  public final static Color LABEL_BACKGROUND_COLOR = new Color(null, 0, 0, 204);

  protected Class type;
  protected Label nameLabel = new Label();

  public AbstractBaseFigure(String name) {
    this(name, true, null);
  }

  public AbstractBaseFigure(String name, Class type) {
    this(name, true, type);
  }

  public AbstractBaseFigure(String name, boolean withLabel, Class type) {
    ToolbarLayout layout = new ToolbarLayout();
    layout.setVertical(true);
    layout.setSpacing(2);
    setLayoutManager(layout);
    if (withLabel) {
      nameLabel.setText(name);
      nameLabel.setOpaque(true);
      add(nameLabel);
    }
    setToolTip(new Label(name));
    this.type = type;
    if (type != null && PaletteBuilder.getInstance().getType(type) != null) {
      setToolTip(new Label(PaletteBuilder.getInstance().getType(type)));
    }
    setOpaque(false);
  }

  public Color getDefaultColor() {
    return DEFAULT_BACKGROUND_COLOR;
  }

  public Color getColor() {
    Color color = PaletteBuilder.getInstance().getColor(type);
    if (color != null) {
      return color;
    }
    return getDefaultColor();
  }

  // private String getNotTooLongName(String name) {
  // // TODO Add system property, for now always return full name
  // if (true)
  // return name;
  //
  // if (name.length() > 16) {
  // return name.substring(0, 3) + "..." + name.substring(name.length() - 10);
  // }
  // return name;
  // }

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
