package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.AccessibleAnchorProvider;
import org.eclipse.gef.NodeEditPart;

import com.isencia.passerelle.workbench.model.editor.ui.figure.AbstractNodeFigure;

public abstract class AbstractNodeEditPart extends AbstractBaseEditPart implements NodeEditPart {

  public Object getAdapter(Class key) {
    if (key == AccessibleAnchorProvider.class)
      return new DefaultAccessibleAnchorProvider() {
        public List<Point> getSourceAnchorLocations() {
          List<Point> list = new ArrayList<Point>();
          Vector<ConnectionAnchor> sourceAnchors = getComponentFigure().getSourceConnectionAnchors();
          for (int i = 0; i < sourceAnchors.size(); i++) {
            ConnectionAnchor anchor = (ConnectionAnchor) sourceAnchors.get(i);
            list.add(anchor.getReferencePoint().getTranslated(0, -3));
          }
          return list;
        }

        public List<Point> getTargetAnchorLocations() {
          List<Point> list = new ArrayList<Point>();
          Vector<ConnectionAnchor> targetAnchors = getComponentFigure().getTargetConnectionAnchors();
          for (int i = 0; i < targetAnchors.size(); i++) {
            ConnectionAnchor anchor = (ConnectionAnchor) targetAnchors.get(i);
            list.add(anchor.getReferencePoint().getTranslated(0, 3));
          }
          return list;
        }
      };
    return super.getAdapter(key);
  }
  
  public AbstractNodeFigure getComponentFigure() {
    return (AbstractNodeFigure) getFigure();
  }
}
