package com.isencia.passerelle.workbench.model.editor.ui.router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Ray;
import org.eclipse.draw2d.geometry.Rectangle;

public final class SCAManhattanConnectionRouter extends AbstractRouter
{

	private Map<Integer, Integer> rowsUsed = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> colsUsed = new HashMap<Integer, Integer>();
	private Map<Connection, Object> constraints = new HashMap<Connection, Object>();
	//private Hashtable offsets = new Hashtable(7);

	private Map<Connection, ReservedInfo> reservedInfo = new HashMap<Connection, ReservedInfo>();

	private IFigure parent;

	private class ReservedInfo {
		public List<Integer> reservedRows = new ArrayList<Integer>(2);
		public List<Integer> reservedCols = new ArrayList<Integer>(2);
	}

	private static Ray 	UP		= new Ray(0, -1),
	DOWN	= new Ray(0, 1),
	LEFT	= new Ray(-1, 0),
	RIGHT	= new Ray(1, 0);



	/**
	 * 
	 */
	public SCAManhattanConnectionRouter(IFigure parent) {
		super();
		this.parent = parent;
	}

	/**
	 * @see ConnectionRouter#invalidate(Connection)
	 */
	public void invalidate(Connection connection) {
		removeReservedLines(connection);
	}

	private int getColumnNear(Connection connection, int r, int n, int x) {
		int min = Math.min(n, x),
		max = Math.max(n, x);
		if (min > r) {
			max = min;
			min = r - (min - r);
		}
		if (max < r) {
			min = max;
			max = r + (r - max);
		}
		int proximity = 0;
		int direction = -1;
		if (r % 6 != 0)
			r = r - ( r % 6);
		Integer i;
		while (proximity < r) {
			i = new Integer(r + proximity * direction);
			if (!colsUsed.containsKey(i)) {
				colsUsed.put(i, i);
				reserveColumn(connection, i);
				return i.intValue();
			}
			int j = i.intValue();
			if (j <= min)
				return j + 6;
			if (j >= max)
				return j - 6;
			if (direction == 1)
				direction = -1;
			else {
				direction = 1;
				proximity += 6;
			}
		}
		return r;
	}

	/**
	 * Returns the direction the point <i>p</i> is in relation to the given rectangle.
	 * Possible values are LEFT (-1,0), RIGHT (1,0), UP (0,-1) and DOWN (0,1).
	 * 
	 * @param r the rectangle
	 * @param p the point
	 * @return the direction from <i>r</i> to <i>p</i>
	 */
	protected Ray getDirection(Rectangle r, Point p) {
		int i, distance = Math.abs(r.x - p.x);
		Ray direction;

		direction = LEFT;

		i = Math.abs(r.y - p.y);
		if (i <= distance) {
			distance = i;
			direction = UP;
		}

		//i = Math.abs(r.bottom() - p.y);
		//if (i <= distance) {
		//	distance = i;
		//	direction = DOWN;
		//}

		i = Math.abs(r.right() - p.x);
		if (i < distance) {
			distance = i;
			direction = RIGHT;
		}

		return direction;
	}

	protected Ray getEndDirection(Connection conn) {
		ConnectionAnchor anchor = conn.getTargetAnchor();
		Point p = getEndPoint(conn);
		Rectangle rect;
		if (anchor.getOwner() == null)
			rect = new Rectangle(p.x, p.y - 1, 2, 2); 		// my workaround starts
		else {
			rect = conn.getTargetAnchor().getOwner().getBounds().getCopy();
			conn.getTargetAnchor().getOwner().translateToAbsolute(rect);
		}
		return getDirection(rect, p);
	}

	protected int getRowNear(Connection connection, int r, int n, int x) {
		int min = Math.min(n, x),
		max = Math.max(n, x);
		if (min > r) {
			max = min;
			min = r - (min - r);
		}
		if (max < r) {
			min = max;
			max = r + (r - max);
		}

		int proximity = 0;
		int direction = -1;
		if (r % 6 != 0)
			r = r - ( r % 6);
		Integer i;
		while (proximity < r) {
			i = new Integer(r + proximity * direction);
			if (!rowsUsed.containsKey(i)) {
				rowsUsed.put(i, i);
				reserveRow(connection, i);
				return i.intValue();
			}
			int j = i.intValue();
			if (j <= min)
				return j + 6;
			if (j >= max)
				return j - 6;
			if (direction == 1)
				direction = -1;
			else {
				direction = 1;
				proximity += 6;
			}
		}
		return r;
	}

	protected Ray getStartDirection(Connection conn) {
		ConnectionAnchor anchor = conn.getSourceAnchor();
		Point p = getStartPoint(conn);
		Rectangle rect;
		if (anchor.getOwner() == null)
			rect = new Rectangle(p.x - 1, p.y - 1, 2, 2);
		else {
			rect = conn.getSourceAnchor().getOwner().getBounds().getCopy();
			conn.getSourceAnchor().getOwner().translateToAbsolute(rect);
		}
		return getDirection(rect, p);
	}

	protected void processPositions(Ray start, Ray end, List positions, 
			boolean horizontal, Connection conn) {
		removeReservedLines(conn);

		int pos[] = new int[positions.size() + 2];
		if (horizontal)
			pos[0] = start.x;
		else
			pos[0] = start.y;
		int i;
		for (i = 0; i < positions.size(); i++) {
			pos[i + 1] = ((Integer)positions.get(i)).intValue();
		}
		if (horizontal == (positions.size() % 2 == 1))
			pos[++i] = end.x;
		else
			pos[++i] = end.y;

		PointList points = new PointList();
		points.addPoint(new Point(start.x, start.y));
		Point p;
		int current, prev, min, max;
		boolean adjust;
		for (i = 2; i < pos.length - 1; i++) {
			horizontal = !horizontal;
			prev = pos[i - 1];
			current = pos[i];

			adjust = (i != pos.length - 2);
			if (horizontal) {
				if (adjust) {
					min = pos[i - 2];
					max = pos[i + 2];
					pos[i] = current = getRowNear(conn, current, min, max);
				}
				p = new Point(prev, current);
			} else {
				if (adjust) {
					min = pos[i - 2];
					max = pos[i + 2];
					pos[i] = current = getColumnNear(conn, current, min, max);
				}
				p = new Point(current, prev);
			}
			points.addPoint(p);
		}
		points.addPoint(new Point(end.x, end.y));
		conn.setPoints(points);
	}

	/**
	 * @see ConnectionRouter#remove(Connection)
	 */
	public void remove(Connection connection) {
		removeReservedLines(connection);
	}

	protected void removeReservedLines(Connection connection) {
		ReservedInfo rInfo = (ReservedInfo) reservedInfo.get(connection);
		if (rInfo == null) 
			return;

		for (int i = 0; i < rInfo.reservedRows.size(); i++) {
			rowsUsed.remove(rInfo.reservedRows.get(i));
		}
		for (int i = 0; i < rInfo.reservedCols.size(); i++) {
			colsUsed.remove(rInfo.reservedCols.get(i));
		}
		reservedInfo.remove(connection);
	}

	protected void reserveColumn(Connection connection, Integer column) {
		ReservedInfo info = (ReservedInfo) reservedInfo.get(connection);
		if (info == null) {
			info = new ReservedInfo();
			reservedInfo.put(connection, info);
		}
		info.reservedCols.add(column);
	}

	protected void reserveRow(Connection connection, Integer row) {
		ReservedInfo info = (ReservedInfo) reservedInfo.get(connection);
		if (info == null) {
			info = new ReservedInfo();
			reservedInfo.put(connection, info);
		}
		info.reservedRows.add(row);
	}

	/**
	 * @see ConnectionRouter#route(Connection)
	 */
	public void route(Connection conn) {	
		if ((conn.getSourceAnchor() == null) || (conn.getTargetAnchor() == null)) 
			return;

		int i;
		Point startPoint = getStartPoint(conn);
		conn.translateToRelative(startPoint);
		Point endPoint = getEndPoint(conn);
		conn.translateToRelative(endPoint);

		Ray start = new Ray(startPoint);
		Ray end = new Ray(endPoint);
		Ray average = start.getAveraged(end);

		Ray direction = new Ray(start, end);
		Ray startNormal = getStartDirection(conn);
		Ray endNormal   = getEndDirection(conn);

		List<Integer> positions = new ArrayList<Integer>(5);
		boolean horizontal = startNormal.isHorizontal();
		if (horizontal) 
			positions.add(new Integer(start.y));
		else
			positions.add(new Integer(start.x));
		horizontal = !horizontal;

		if (startNormal.dotProduct(endNormal) == 0) {
			if ((startNormal.dotProduct(direction) >= 0) 
					&& (endNormal.dotProduct(direction) <= 0)) {
				// 0
			} else {
				// 2
				if (startNormal.dotProduct(direction) < 0)
					i = startNormal.similarity(start.getAdded(startNormal.getScaled(10)));
				else {
					if (horizontal) 
						i = average.y;
					else 
						i = average.x;
				}
				positions.add(new Integer(i));
				horizontal = !horizontal;

				if (endNormal.dotProduct(direction) > 0)
					i = endNormal.similarity(end.getAdded(endNormal.getScaled(10)));
				else {
					if (horizontal) 
						i = average.y;
					else 
						i = average.x;
				}
				positions.add(new Integer(i));
				horizontal = !horizontal;
			}
		} else {
			if (startNormal.dotProduct(endNormal) > 0) {
				//1
				if (startNormal.dotProduct(direction) >= 0)
					i = startNormal.similarity(start.getAdded(startNormal.getScaled(10)));
				else
					i = endNormal.similarity(end.getAdded(endNormal.getScaled(10)));
				positions.add(new Integer(i));
				horizontal = !horizontal;
			} else {
				//3 or 1
				if (startNormal.dotProduct(direction) < 0) {
					i = startNormal.similarity(start.getAdded(startNormal.getScaled(10)));
					positions.add(new Integer(i));
					horizontal = !horizontal;
				}

				// my tweak to route SCA wires starts
				if (isCycle(conn)) {
					if (horizontal)
						i = conn.getSourceAnchor().getOwner().getBounds().getTop().y - 10;// * index;
					else
						i = conn.getSourceAnchor().getOwner().getBounds().getRight().x + 10;// * index;
				} else {
					if (horizontal) {
						int j = average.y;

						int next = endNormal.similarity(end.getAdded(endNormal.getScaled(10)));

						Ray trial = new Ray(((Integer)positions.get(positions.size() - 1)).intValue(), j);
						IFigure figure = findFirstFigureAtStraightLine(trial, LEFT, Collections.EMPTY_LIST);

						while (figure != null && figure.getBounds().x + figure.getBounds().width > next) {
							j = figure.getBounds().y + figure.getBounds().height + 5;
							trial.y = j;
							figure = findFirstFigureAtStraightLine(trial, LEFT, Collections.EMPTY_LIST);						
						}

						i = j;

					} else {
						IFigure figure = findFirstFigureAtStraightLine(start, RIGHT, getExcludingFigures(conn));
						if (figure == null)
							i = average.x;
						else {
							i = Math.min(average.x, start.getAdded(new Ray(3 * (figure.getBounds().x - start.x) / 4, 0)).x);
							i = Math.max(start.x, i);
						}
						i = adjust(conn, i);
					}
				}
				// my tweak to route SCA wires ends

				positions.add(new Integer(i));
				horizontal = !horizontal;

				if (startNormal.dotProduct(direction) < 0) {
					i = endNormal.similarity(end.getAdded(endNormal.getScaled(10)));
					positions.add(new Integer(i));
					horizontal = !horizontal;
				} else {
					// my tweak to route SCA wires starts				
					boolean reroute = false;

					int j = end.y;

					IFigure figure = findFirstFigureAtStraightLine(new Ray(i, j), RIGHT, getExcludingFigures(conn));
					while (figure != null && figure.getBounds().x < end.x) {
						reroute = true;
						if (direction.dotProduct(DOWN) > 0) 
							j = figure.getBounds().y - 5;						
						else 
							j = figure.getBounds().y + figure.getBounds().height + 5; 

						figure = findFirstFigureAtStraightLine(new Ray(i, j), RIGHT, getExcludingFigures(conn));										
					}
					if (reroute) {
						i = j;
						positions.add(new Integer(i));
						horizontal = !horizontal;

						i = endNormal.similarity(end.getAdded(endNormal.getScaled(10)));
						positions.add(new Integer(i));
						horizontal = !horizontal;						

					}
					// my tweak to route SCA wires ends
				}
			}
		}
		if (horizontal) 
			positions.add(new Integer(end.y));
		else 
			positions.add(new Integer(end.x));

		processPositions(start, end, positions, startNormal.isHorizontal(), conn);
	}

	public Object getConstraint(Connection connection) {
		return constraints.get(connection);
	}

	public void setConstraint(Connection connection, Object constraint) {
		constraints.put(connection, constraint);
	}

	protected boolean isCycle(Connection conn) {
		IFigure source = conn.getSourceAnchor().getOwner();
		IFigure target = conn.getTargetAnchor().getOwner();

		return source != null && source.equals(target);
	}

	protected List getExcludingFigures(Connection conn) {
		List<IFigure> excluding = new ArrayList<IFigure>();

		excluding.add(conn.getSourceAnchor().getOwner());
		excluding.add(conn.getTargetAnchor().getOwner());

		return excluding;
	}

	protected IFigure findFirstFigureAtStraightLine(Ray start, Ray direction, List excluding) {
		IFigure figure = null;

		Collection<IFigure> figures = getNodeFigures();

		for (IFigure child : figures) {
			
			if (!excluding.contains(child)) {
				Rectangle rect = child.getBounds();
				if (LEFT.equals(direction)) {
					if (start.x > rect.x && start.y >= rect.y && start.y <= rect.y + rect.height) {					
						if (figure == null || child.getBounds().x > figure.getBounds().x)
							figure = child;
					}
				} else if (RIGHT.equals(direction)) {
					if (start.x < rect.x + rect.width && start.y >= rect.y && start.y <= rect.y + rect.height) {					
						if (figure == null || child.getBounds().x < figure.getBounds().x)
							figure = child;
					} 
				} else if (UP.equals(direction)){
					if (start.y > rect.y && start.x >= rect.x && start.x <= rect.x + rect.width) {
						if (figure == null || child.getBounds().y > figure.getBounds().y)
							figure = child;
					}			
				} else if (DOWN.equals(direction)){
					if (start.y < rect.y + rect.height && start.x >= rect.x && start.x <= rect.x + rect.width) {
						if (figure == null || child.getBounds().y < figure.getBounds().y)
							figure = child;
					}					
				}
			}
		}
		return figure;
	}

	protected int adjust(Connection connection, int col) {
		int column = col;

		Point start = connection.getSourceAnchor().getLocation(null);

		Collection<Connection> connections = getConnectionFigures();
		for (Connection conn : connections) {
			
			if (conn.equals(connection)) continue;

			Point end = conn.getTargetAnchor().getLocation(null);
			if (start.x < end.x && start.y == end.y) {
				if (conn.getPoints().getMidpoint().x <= col)
					column = conn.getPoints().getMidpoint().x - 5;
			}
		}
		return column;
	}

	private Collection<IFigure> getNodeFigures() {
		Collection<IFigure> ret = new HashSet<IFigure>(31);
		getNodeFigures(ret, parent);
		return ret;
	}

	private void getNodeFigures(Collection<IFigure> figures, IFigure figure) {

		if (figure instanceof RoundedRectangle) figures.add(figure);
		if (figure instanceof Ellipse)          figures.add(figure);
		if (figure instanceof RectangleFigure)  figures.add(figure);
		final List children = figure.getChildren();
		if (children!=null) for (Object child : children) {
			if (child instanceof IFigure) {
				getNodeFigures(figures, (IFigure)child);
			}
		}
	}

	private Collection<Connection> getConnectionFigures() {
		Collection<Connection> ret = new HashSet<Connection>(31);
		getConnectionFigures(ret, parent);
		return ret;
	}
	
	private void getConnectionFigures(Collection<Connection> connections, IFigure figure) {

		if (figure instanceof Connection) connections.add((Connection)figure);
		final List children = figure.getChildren();
		if (children!=null) for (Object child : children) {
			if (child instanceof IFigure) {
				getConnectionFigures(connections, (IFigure)child);
			}
		}
	}

}
