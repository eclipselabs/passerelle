package com.isencia.passerelle.actor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingConstants;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.PortSite;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.core.ControlPort;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;

public class ActorPortLayout extends AbstractGlobalLayout {

	private static Font _portLabelFont = new Font("SansSerif", Font.PLAIN, 8);

	private GraphController graphController;

	public ActorPortLayout(GraphController graphController) {
		super(new BasicLayoutTarget(graphController));
		this.graphController = graphController;
	}

	// /////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Layout the ports of the specified node.
	 * 
	 * @param node
	 *            The node, which is assumed to be an entity.
	 */
	public void layout(Object node) {
		GraphModel model = graphController.getGraphModel();

		// System.out.println("layout = " + node);
		// new Exception().printStackTrace();
		Iterator nodes = model.nodes(node);
		Vector westPorts = new Vector();
		Vector eastPorts = new Vector();
		Vector southPorts = new Vector();
		Vector northPorts = new Vector();

		while (nodes.hasNext()) {
			Port port = (Port) nodes.next();
			int portRotation = _getCardinality(port);
			int direction = _getDirection(portRotation);
			if (direction == SwingConstants.WEST) {
				westPorts.add(port);
			} else if (direction == SwingConstants.NORTH) {
				northPorts.add(port);
			} else if (direction == SwingConstants.EAST) {
				eastPorts.add(port);
			} else {
				southPorts.add(port);
			}
		}

		CompositeFigure figure = (CompositeFigure) getLayoutTarget().getVisualObject(node);

		_reOrderPorts(westPorts);
		_placePortFigures(figure, westPorts, SwingConstants.WEST);
		_reOrderPorts(eastPorts);
		_placePortFigures(figure, eastPorts, SwingConstants.EAST);
		_reOrderPorts(southPorts);
		_placePortFigures(figure, southPorts, SwingConstants.SOUTH);
		_reOrderPorts(northPorts);
		_placePortFigures(figure, northPorts, SwingConstants.NORTH);
	}

	// /////////////////////////////////////////////////////////////
	// // private methods ////
	// re-order the ports according to _ordinal property
	private void _reOrderPorts(Vector ports) {
		int size = ports.size();
		Enumeration enumeration = ports.elements();
		Port port;
		StringAttribute ordinal = null;
		int number = 0;
		int index = 0;

		while (enumeration.hasMoreElements()) {
			port = (Port) enumeration.nextElement();
			ordinal = (StringAttribute) port.getAttribute("_ordinal");

			if (ordinal != null) {
				number = Integer.parseInt(ordinal.getExpression());

				if (number >= size) {
					ports.remove(index);

					try {
						ordinal.setExpression(Integer.toString(size - 1));
					} catch (Exception e) {
						MessageHandler.error("Error setting ordinal property", e);
					}

					ports.add(port);
				} else if (number < 0) {
					ports.remove(index);

					try {
						ordinal.setExpression(Integer.toString(0));
					} catch (Exception e) {
						MessageHandler.error("Error setting ordinal property", e);
					}

					ports.add(0, port);
				} else if (number != index) {
					ports.remove(index);
					ports.add(number, port);
				}
			}

			index++;
		}
	}

	// Place the ports.
	private void _placePortFigures(CompositeFigure figure, List portList, int direction) {
		Iterator ports = portList.iterator();
		int number = 0;
		int count = portList.size();

		Figure background = figure.getBackgroundFigure();

		if (background == null) {
			// This could occur if the icon has a _hide parameter.
			background = figure;
		}

		while (ports.hasNext()) {
			IOPort port = (IOPort) ports.next();
			Figure portFigure = graphController.getFigure(port);

			// If there is no figure, then ignore this port. This may
			// happen if the port hasn't been rendered yet.
			if (portFigure == null) {
				continue;
			}

			Rectangle2D portBounds = portFigure.getShape().getBounds2D();
			PortSite site = new PortSite(background, port, number, count, direction);
			number++;

			// NOTE: previous expression for port location was:
			// 100.0 * number / (count+1)
			// But this leads to squished ports with uneven spacing.
			// Note that we don't use CanvasUtilities.translateTo because
			// we want to only get the bounds of the background of the
			// port figure.
			double x = site.getX() - portBounds.getCenterX();
			double y = site.getY() - portBounds.getCenterY();
			portFigure.translate(x, y);

			boolean isExpertMode = false;
			if(port.getContainer() instanceof Actor) {
				Actor containingActor = (Actor) port.getContainer();
				try {
					isExpertMode = containingActor.getDirectorAdapter().isExpertMode();
				} catch (Exception e) {}
			}
			boolean portIsConnected = port.getWidth() > 0;
			if(port instanceof ControlPort) {
				if(!portIsConnected && !isExpertMode) {
					portFigure.setVisible(false);
				  continue;
				}
			}

			// If the actor contains a variable named "_showRate",
			// with value true, then visualize the rate information.
			// NOTE: Showing rates only makes sense for IOPorts.
			Attribute showRateAttribute = port.getAttribute("_showRate");

			if (port instanceof IOPort && showRateAttribute instanceof Variable) {
				boolean showRate = false;

				try {
					showRate = ((Variable) showRateAttribute).getToken().equals(BooleanToken.TRUE);
				} catch (Exception ex) {
					// Ignore.
				}

				if (showRate) {
					// Infer the rate. See DFUtilities.
					String rateString = "";
					Variable rateParameter = null;

					if (((IOPort) port).isInput()) {
						rateParameter = (Variable) port.getAttribute("tokenConsumptionRate");

						if (rateParameter == null) {
							String altName = "_tokenConsumptionRate";
							rateParameter = (Variable) port.getAttribute(altName);
						}
					} else if (((IOPort) port).isOutput()) {
						rateParameter = (Variable) port.getAttribute("tokenProductionRate");

						if (rateParameter == null) {
							String altName = "_tokenProductionRate";
							rateParameter = (Variable) port.getAttribute(altName);
						}
					}

					if (rateParameter != null) {
						try {
							rateString = rateParameter.getToken().toString();
						} catch (KernelException ex) {
							// Ignore.
						}
					}

					LabelFigure labelFigure = _createPortLabelFigure(rateString, _portLabelFont, x, y, direction);
					labelFigure.setFillPaint(Color.BLUE);
					figure.add(labelFigure);
				}
			}

			// If the port contains an attribute named "_showName",
			// then render the name of the port as well. If the
			// attribute is a boolean-valued parameter, then
			// show the name only if the value is true.
			Token showToken = PtolemyPreferences.preferenceValueLocal(port.toplevel(), "_showPortNames");
//			Attribute showAttribute = port.getAttribute("_showName");
			String toShow = null;
			if (showToken != null) {
				boolean show = true;
				if (showToken instanceof BooleanToken) {
					show = ((BooleanToken) showToken).booleanValue();
				}

				if (show) {
					toShow = port.getDisplayName();
				}
			}
			// In addition, if the port contains an attribute
			// called "_showInfo", then if that attribute is
			// a variable, then its value is shown. Otherwise,
			// if it is a Settable, then its expression is shown.
			Attribute showInfo = port.getAttribute("_showInfo");
			try {
				if (showInfo instanceof Variable && !((Variable) showInfo).isStringMode()) {
					String value = ((Variable) showInfo).getToken().toString();
					if (toShow != null) {
						toShow += " (" + value + ")";
					} else {
						toShow = value;
					}
				} else if (showInfo instanceof Settable) {
					if (toShow != null) {
						toShow += " (" + ((Settable) showInfo).getExpression() + ")";
					} else {
						toShow = ((Settable) showInfo).getExpression();
					}
				}
			} catch (IllegalActionException e) {
				toShow += e.getMessage();
			}

			if (toShow != null) {
				LabelFigure labelFigure = _createPortLabelFigure(toShow, _portLabelFont, x, y, direction);
				figure.add(labelFigure);
			}
		}
	}

	private LabelFigure _createPortLabelFigure(String string, Font font, double x, double y, int direction) {
		LabelFigure label;

		if (direction == SwingConstants.SOUTH) {
			// The 1.0 argument is the padding.
			label = new LabelFigure(string, font, 1.0, SwingConstants.SOUTH_WEST);

			// Shift the label down so it doesn't
			// collide with ports.
			label.translateTo(x, y + 5);

			// Rotate the label.
			AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2.0, x, y + 5);
			label.transform(rotate);
		} else if (direction == SwingConstants.EAST) {
			// The 1.0 argument is the padding.
			label = new LabelFigure(string, font, 1.0, SwingConstants.SOUTH_WEST);

			// Shift the label right so it doesn't
			// collide with ports.
			label.translateTo(x + 5, y);
		} else if (direction == SwingConstants.WEST) {
			// The 1.0 argument is the padding.
			label = new LabelFigure(string, font, 1.0, SwingConstants.SOUTH_EAST);

			// Shift the label left so it doesn't
			// collide with ports.
			label.translateTo(x - 5, y);
		} else { // Must be north.

			// The 1.0 argument is the padding.
			label = new LabelFigure(string, font, 1.0, SwingConstants.SOUTH_WEST);

			// Shift the label right so it doesn't
			// collide with ports. It will probably
			// collide with the actor name.
			label.translateTo(x, y - 5);

			// Rotate the label.
			AffineTransform rotate = AffineTransform.getRotateInstance(-Math.PI / 2.0, x, y - 5);
			label.transform(rotate);
		}

		return label;
	}
	
    /** Return one of {-270, -180, -90, 0, 90, 180, 270} specifying
     *  the orientation of a port. This depends on whether the port
     *  is an input, output, or both, whether the port has a parameter
     *  named "_cardinal" that specifies a cardinality, and whether the
     *  containing actor has a parameter named "_rotatePorts" that
     *  specifies a rotation of the ports.  In addition, if the
     *  containing actor has a parameter named "_flipPortsHorizonal"
     *  or "_flipPortsVertical" with value true, then any ports that end up on the left
     *  or right (top or bottom) will be reversed.
     *  @param port The port.
     *  @return One of {-270, -180, -90, 0, 90, 180, 270}.
     */
    protected static int _getCardinality(Port port) {
        // Determine whether the port has an attribute that specifies
        // which side of the icon it should be on, and whether the
        // actor has an attribute that rotates the ports. If both
        // are present, the port attribute takes precedence.

        boolean isInput = false;
        boolean isOutput = false;
        boolean isInputOutput = false;

        // Figure out what type of port we're dealing with.
        // If ports are not IOPorts, then draw then as ports with
        // no direction.
        if (port instanceof IOPort) {
            isInput = ((IOPort) port).isInput();
            isOutput = ((IOPort) port).isOutput();
            isInputOutput = isInput && isOutput;
        }

        StringAttribute cardinal = null;
        int portRotation = 0;
        try {
            cardinal = (StringAttribute) port.getAttribute("_cardinal",
                    StringAttribute.class);
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter rotationParameter = (Parameter) container
                        .getAttribute("_rotatePorts", Parameter.class);
                if (rotationParameter != null) {
                    Token rotationValue = rotationParameter.getToken();
                    if (rotationValue instanceof IntToken) {
                        portRotation = ((IntToken) rotationValue).intValue();
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }
        if (cardinal == null) {
            // Port cardinality is not specified in the port.
            if (isInputOutput) {
                portRotation += -90;
            } else if (isOutput) {
                portRotation += 180;
            }
        } else if (cardinal.getExpression().equalsIgnoreCase("NORTH")) {
            portRotation = 90;
        } else if (cardinal.getExpression().equalsIgnoreCase("SOUTH")) {
            portRotation = -90;
        } else if (cardinal.getExpression().equalsIgnoreCase("EAST")) {
            portRotation = 180;
        } else if (cardinal.getExpression().equalsIgnoreCase("WEST")) {
            portRotation = 0;
        } else { // this shouldn't happen either
            portRotation += -90;
        }

        // Ensure that the port rotation is one of
        // {-270, -180, -90, 0, 90, 180, 270}.
        portRotation = 90 * ((portRotation / 90) % 4);

        // Finally, check for horizontal or vertical flipping.
        try {
            NamedObj container = port.getContainer();
            if (container != null) {
                Parameter flipHorizontalParameter = (Parameter) container
                        .getAttribute("_flipPortsHorizontal", Parameter.class);
                if (flipHorizontalParameter != null) {
                    Token rotationValue = flipHorizontalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == 0 || portRotation == -180) {
                            portRotation += 180;
                        } else if (portRotation == 180) {
                            portRotation = 0;
                        }
                    }
                }
                Parameter flipVerticalParameter = (Parameter) container
                        .getAttribute("_flipPortsVertical", Parameter.class);
                if (flipVerticalParameter != null) {
                    Token rotationValue = flipVerticalParameter.getToken();
                    if (rotationValue instanceof BooleanToken
                            && ((BooleanToken) rotationValue).booleanValue()) {
                        if (portRotation == -270 || portRotation == -90) {
                            portRotation += 180;
                        } else if (portRotation == 90 || portRotation == 270) {
                            portRotation -= 180;
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore and use defaults.
        }

        return portRotation;
    }

    /** Return the direction associated with the specified angle,
     *  which is assumed to be one of {-270, -180, -90, 0, 90, 180, 270}.
     *  @param portRotation The angle
     *  @return One of SwingUtilities.NORTH, SwingUtilities.EAST,
     *  SwingUtilities.SOUTH, or SwingUtilities.WEST.
     */
    protected static int _getDirection(int portRotation) {
        int direction;
        if (portRotation == 90 || portRotation == -270) {
            direction = SwingConstants.NORTH;
        } else if (portRotation == 180 || portRotation == -180) {
            direction = SwingConstants.EAST;
        } else if (portRotation == 270 || portRotation == -90) {
            direction = SwingConstants.SOUTH;
        } else {
            direction = SwingConstants.WEST;
        }
        return direction;
    }

}
