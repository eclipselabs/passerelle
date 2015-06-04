package com.isencia.passerelle.workbench.model.editor.ui.figure;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;

public abstract class AbstractNodeFigure extends AbstractBaseFigure {
	
	public final static int ANCHOR_MARGIN = 10;
	public final static int ANCHOR_SPACING = 3;
	public final static int ANCHOR_WIDTH = 7;
	public final static int ANCHOR_HEIGTH = 9;
	
	protected HashMap<String, Object> connectionAnchors = new HashMap<String, Object>(7);

	protected Vector<ConnectionAnchor> inputConnectionAnchors = new Vector<ConnectionAnchor>(2, 2);
	protected Vector<ConnectionAnchor> outputConnectionAnchors = new Vector<ConnectionAnchor>(2, 2);
	
    public AbstractNodeFigure(String name,Class type) {
    	super(name,type);
    }
    public AbstractNodeFigure(String name,boolean withLabel,Class type) {
    	super(name,withLabel,type);
    }

	public ConnectionAnchor connectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration<ConnectionAnchor> e = getSourceConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		e = getTargetConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public ConnectionAnchor getConnectionAnchor(String terminal) {
		return (ConnectionAnchor) connectionAnchors.get(terminal);
	}

	public String getConnectionAnchorName(ConnectionAnchor c) {
		Set<Map.Entry<String, Object>> connectionAnchorsSet = connectionAnchors.entrySet();
		if( connectionAnchorsSet==null || connectionAnchorsSet.size()==0)
			return null;
		
		for (Map.Entry<String, Object> connectionAnchor : connectionAnchorsSet) {
			if (connectionAnchor.getValue().equals(c))
				return connectionAnchor.getKey();
			
		}
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration e = getSourceConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector<ConnectionAnchor> getSourceConnectionAnchors() {
		return outputConnectionAnchors;
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration e = getTargetConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector<ConnectionAnchor> getTargetConnectionAnchors() {
		return inputConnectionAnchors;
	}
	
	public HashMap getConnectionAnchors() {
		return connectionAnchors;
	}
	protected int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	protected int getMinHeight() {
		return MIN_HEIGHT;
	}

	protected int getAnchorHeight() {
		return ANCHOR_HEIGTH;
	}

	protected int getAnchorMargin() {
		return ANCHOR_MARGIN;
	}

	protected int getAnchorSpacing() {
		return ANCHOR_SPACING;
	}
	protected int getAnchorWidth() {
		return ANCHOR_WIDTH;
	}
}
