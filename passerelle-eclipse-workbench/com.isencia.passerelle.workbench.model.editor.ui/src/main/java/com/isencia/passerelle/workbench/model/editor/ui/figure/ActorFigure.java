package com.isencia.passerelle.workbench.model.editor.ui.figure;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;

public abstract class ActorFigure extends AbstractNodeFigure {

  public final static Color ACTOR_BACKGROUND_COLOR = ColorConstants.lightGray;

  private IFigure body = null;
  private Ports inputPorts = null;
  private Ports outputPorts = null;
  private HashMap<String, PortFigure> inputPortMap = new HashMap<String, PortFigure>();

  public HashMap<String, PortFigure> getInputPortMap() {
    return inputPortMap;
  }

  public HashMap<String, PortFigure> getOutputPortMap() {
    return outputPortMap;
  }

  private HashMap<String, PortFigure> outputPortMap = new HashMap<String, PortFigure>();

  public ActorFigure(String name, Class type, Image image, Clickable[] clickables) {
    super(name, type);
    add(new CompositeFigure(image, clickables));
  }

  protected abstract IFigure generateBody(Image image, Clickable[] clickables);

  private class CompositeFigure extends Figure {

    public CompositeFigure(Image image, Clickable[] clickables) {

      BorderLayout layout = new BorderLayout();
      setLayoutManager(layout);
      setOpaque(false);

      inputPorts = new Ports();
      add(inputPorts, BorderLayout.LEFT);

      body = generateBody(image, clickables);
      if (body != null)
        add(body, BorderLayout.CENTER);

      outputPorts = new Ports();
      add(outputPorts, BorderLayout.RIGHT);

    }

    @Override
    public Dimension getMaximumSize() {
      return getPreferredSize(-1, -1);
    }

    @Override
    public Dimension getPreferredSize(int hint, int hint2) {
      Vector<ConnectionAnchor> targetConnectionAnchors = getTargetConnectionAnchors();
      Vector<ConnectionAnchor> sourceConnectionAnchors = getSourceConnectionAnchors();
      int maxAnchorCount = Math.max(targetConnectionAnchors.size(), sourceConnectionAnchors.size());
      if (maxAnchorCount > 0) {
        int height = maxAnchorCount * getAnchorHeight() + (maxAnchorCount > 1 ? ((maxAnchorCount - 1) * getAnchorSpacing()) : 0) + (2 * getAnchorMargin());
        if (height < getMinHeight())
          height = getMinHeight();
        return new Dimension(getDefaultWidth(), height);
      } else
        return super.getMinimumSize();
    }

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
      return new Insets(getAnchorMargin(), 0, 0, 0);
    }

    public void paint(IFigure figure, Graphics graphics, Insets insets) {
      // graphics.draw
    }

  }

  public void setBackgroundColor(Color c) {
    if (body != null) {
      body.setBackgroundColor(c);
    }
  }

  /**
   * Add an input port and its anchor
   * 
   * @param portName
   *          unique name to refer to the port
   * @param displayname
   */
  public PortFigure addInput(String portName, String displayName) {
    ActorPortFigure inputPortFigure = new ActorPortFigure(portName);
    inputPortFigure.setToolTip(new Label(displayName));
    int x = 0;
    int y = 0;
    inputPortFigure.setLocation(new Point(x, y));
    if (inputPorts != null) {
      inputPorts.add(inputPortFigure);
      inputPortMap.put(portName, inputPortFigure);
    }
    // TODO update Anchor with correct attributes
    FixedConnectionAnchor anchor = new FixedConnectionAnchor(inputPortFigure);

    anchor.offsetV = getAnchorHeight() / 2;
    getTargetConnectionAnchors().add(anchor);
    connectionAnchors.put(portName, anchor);
    return inputPortFigure;
  }

  public void removeInput(String portName) {
    PortFigure figure = inputPortMap.get(portName);
    if (figure != null) {
      inputPorts.remove(figure);
      inputPortMap.remove(portName);
      ConnectionAnchor anchor = getConnectionAnchor(portName);
      if (anchor != null) {
        getTargetConnectionAnchors().remove(anchor);
      }

    }
  }

  public void removeOutput(String portName) {
    PortFigure figure = outputPortMap.get(portName);
    if (figure != null) {
      outputPorts.remove(figure);
      outputPortMap.remove(portName);
      ConnectionAnchor anchor = getConnectionAnchor(portName);
      if (anchor != null) {
        getSourceConnectionAnchors().remove(anchor);
      }

    }
  }

  /**
   * Add an output port and its anchor
   * 
   * @param portName
   *          unique name to refer to the port
   * @param displayname
   */
  public PortFigure addOutput(String portName, String displayName) {
    ActorPortFigure outputPortFigure = new ActorPortFigure(portName);
    outputPortFigure.setToolTip(new Label(displayName));
    if (outputPorts != null) {
      outputPorts.add(outputPortFigure);
      outputPortMap.put(portName, outputPortFigure);
    }
    FixedConnectionAnchor anchor = new FixedConnectionAnchor(outputPortFigure);
    anchor.offsetV = getAnchorHeight() / 2;
    anchor.offsetH = getAnchorWidth() - 1;
    getSourceConnectionAnchors().add(anchor);
    connectionAnchors.put(portName, anchor);
    return outputPortFigure;
  }

  public PortFigure getInputPort(String name) {
    return inputPortMap.get(name);
  }

  public PortFigure getOutputPort(String name) {
    return outputPortMap.get(name);
  }
  public void setPortColor(String portName, boolean isSelected, int colorCode) {
	  PortFigure port = inputPortMap.get(portName);
	  if (port==null) port = outputPortMap.get(portName);
	  if (port==null) return;

	  port.setSelectedColor(isSelected, colorCode);
	  repaint();
  }
  
	private Image breakPointImage;
	public void setBreakPoint(boolean isBreak) {
		if (isBreak) {
			if (breakPointImage==null) {
				breakPointImage = Activator.getImageDescriptor("icons/break_point.png").createImage();
			}
			nameLabel.setIcon(breakPointImage);
			nameLabel.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA));
		} else {
			nameLabel.setIcon(null);
			nameLabel.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		}
		repaint();
	}

}
