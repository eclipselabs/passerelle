package com.isencia.passerelle.workbench.model.editor.ui.figure;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;

public class VertexFigure extends AbstractNodeFigure {
  public static final String VERTEX_OUTPUT = "vertexOutput";
  public static final String VERTEX_INPUT = "vertexInput";
  private InputPorts inputPorts = null;
  private OutputPorts outputPorts = null;
  public final static int ANCHOR_WIDTH = 4;
  public final static int ANCHOR_HEIGHT = 8;

  @Override
  protected int getAnchorWidth() {
    // TODO Auto-generated method stub
    return ANCHOR_WIDTH;
  }

  @Override
  protected int getAnchorHeight() {
    // TODO Auto-generated method stub
    return ANCHOR_HEIGHT;
  }

  private List<ConnectionAnchor> inputAnchors = new ArrayList<ConnectionAnchor>();
  private List<ConnectionAnchor> outputAnchors = new ArrayList<ConnectionAnchor>();

  public List<ConnectionAnchor> getOutputAnchors() {
    return outputAnchors;
  }

  public List<ConnectionAnchor> getInputAnchors() {
    return inputAnchors;
  }

  public ConnectionAnchor getInputAnchor(double[] location, double[] vertexLocation) {
    int index = 0;
    double diff = 0;
    for (ConnectionAnchor anchor : inputAnchors) {
      double anchorx = vertexLocation[0] + anchor.getReferencePoint().x;
      double anchory = vertexLocation[1] + anchor.getReferencePoint().y;
      double temp = Math.pow(anchorx - location[0], 2) + Math.pow(anchory - location[1], 2);
      if (diff == 0 || temp < diff) {
        diff = temp;
        index = inputAnchors.indexOf(anchor);
      }
    }
    return inputAnchors.get(index);

  }

  public ConnectionAnchor getOutputAnchor(double[] location, double[] vertexLocation) {
    int index = 0;
    double diff = 0;
    for (ConnectionAnchor anchor : outputAnchors) {
      double anchorx = anchor.getReferencePoint().x + vertexLocation[0];
      double anchory = anchor.getReferencePoint().y + vertexLocation[1];

      double temp = Math.pow(anchorx - location[0], 2) + Math.pow(anchory - location[1], 2);
      if (diff == 0 || temp < diff) {
        diff = temp;
        index = outputAnchors.indexOf(anchor);
      }
    }

    return outputAnchors.get(index);
 //   return outputAnchors.get(0);
  }

  public VertexFigure(String name, Class type, Image image) {
    super(name, false, type);
    ToolbarLayout layout = new ToolbarLayout();
    layout.setVertical(false);
    setLayoutManager(layout);
    layout.setSpacing(0);
    setOpaque(false);
    setBackgroundColor(ColorConstants.white);
    setForegroundColor(ColorConstants.white);
    inputPorts = new InputPorts();
    add(inputPorts);

    outputPorts = new OutputPorts();
    add(outputPorts);

    VertexPortFigure inputPortFigure = new VertexPortFigure(VERTEX_INPUT, getAnchorWidth(), getAnchorHeight(), ColorConstants.black);
    inputPortFigure.setToolTip(new Label(VERTEX_INPUT));
    inputPortFigure.setLocation(new Point(0, 0));

    addAnchor(getSourceConnectionAnchors(), inputAnchors, inputPortFigure, 0, getAnchorHeight() / 2);
//    addAnchor(getSourceConnectionAnchors(), inputAnchors, inputPortFigure, getAnchorWidth(), 0);
//    addAnchor(getSourceConnectionAnchors(), inputAnchors, inputPortFigure, getAnchorWidth(), getAnchorHeight());
//    addAnchor(getSourceConnectionAnchors(), inputAnchors, inputPortFigure, 2 * getAnchorWidth(), getAnchorHeight() / 2);
    inputPorts.add(inputPortFigure);

    VertexPortFigure outputPortFigure = new VertexPortFigure(VERTEX_OUTPUT, getAnchorWidth(), getAnchorHeight(), ColorConstants.black);
    inputPortFigure.setToolTip(new Label(VERTEX_OUTPUT));
    inputPortFigure.setLocation(new Point(0, 0));

    addAnchor(getTargetConnectionAnchors(), outputAnchors, outputPortFigure, getAnchorWidth() - 1, getAnchorHeight() / 2);
//    addAnchor(getTargetConnectionAnchors(), outputAnchors, outputPortFigure, 0, getAnchorHeight() - 1);
//    addAnchor(getTargetConnectionAnchors(), outputAnchors, outputPortFigure, 0, 0);
//    addAnchor(getTargetConnectionAnchors(), outputAnchors, outputPortFigure, -getAnchorWidth() + 1, getAnchorHeight() / 2);

    outputPorts.add(outputPortFigure);
  }

  private FixedConnectionAnchor addAnchor(Vector<ConnectionAnchor> anchorVector, List<ConnectionAnchor> inputAnchors, PortFigure inputPortFigure, int offsetH, int offsetV) {
    FixedConnectionAnchor inputAnchor = new FixedConnectionAnchor(inputPortFigure);
    inputAnchor.offsetV = offsetV;
    inputAnchor.offsetH = offsetH;
    anchorVector.add(inputAnchor);
    inputAnchors.add(inputAnchor);
    return inputAnchor;
  }

  private class Ports extends Figure {
    public Ports() {
      ToolbarLayout layout = new ToolbarLayout();
      layout.setVertical(true);
      layout.setSpacing(getAnchorSpacing());
      setLayoutManager(layout);
      setOpaque(false);
      setBorder(new PortsBorder());
    }
  }

  private class PortsBorder extends AbstractBorder {

    public Insets getInsets(IFigure ifigure) {
      return new Insets(getAnchorWidth(), 0, 0, 0);
    }

    public void paint(IFigure ifigure, Graphics g, Insets insets) {
    }

  }

  private class InputPorts extends Ports {
    public InputPorts() {
      super();
    }
  }

  private class OutputPorts extends Ports {
    public OutputPorts() {
      super();
    }

  }
}
