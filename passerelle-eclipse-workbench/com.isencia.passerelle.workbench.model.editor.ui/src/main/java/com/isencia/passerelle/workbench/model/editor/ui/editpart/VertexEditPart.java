package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ActorEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.VertexFigure;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class VertexEditPart extends AbstractNodeEditPart implements IActorNodeEditPart {

  public VertexEditPart() {
    super();
  }

  @Override
  protected IFigure createFigure() {
    return new VertexFigure(((Vertex) getModel()).getName(), Vertex.class, null);
  }

  @Override
  protected List getModelSourceConnections() {

    List<Link> allLinks = new ArrayList<Link>();

    Set<Link> links = getDiagram().getLinkHolder().getLinks(getModel());
    if (links != null)
      for (Link link : links) {
        if (link.getHead().equals(getModel())) {
          allLinks.add(link);
        }
      }
    return allLinks;

  }

  @Override
  protected List getModelTargetConnections() {
    List<Link> allLinks = new ArrayList<Link>();

    Set<Link> links = getDiagram().getLinkHolder().getLinks(getModel());
    if (links != null)
      for (Link link : links) {
        if (link.getTail().equals(getModel())) {
          allLinks.add(link);
        }
      }
    return allLinks;
  }

  public VertexFigure getVertexFigure() {
    return (VertexFigure) getFigure();
  }

  public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
    getLogger().debug("Get SourceConnectionAnchor based on ConnectionEditPart");
    return getVertexFigure().getInputAnchor(getLocation(connEditPart, false), ModelUtils.getLocation((Vertex) getModel()));
  }

  private double[] getLocation(ConnectionEditPart connEditPart, boolean isSource) {
    Object model = connEditPart.getModel();
    double[] location = { 0, 0 };
    if (model instanceof Link) {
      NamedObj port = null;
      Link link = (Link) model;
      Object source = null;
      if (!isSource)
        source = link.getTail();
      else
        source = link.getHead();
      if (source instanceof Port) {
        port = (Port) source;
        NamedObj container = port.getContainer();
        if (container instanceof CompositeEntity) {
          return ModelUtils.getLocation(port);
        }else {
          return ModelUtils.getLocation(container);
        }
      }
      if (source instanceof Vertex){
        return ModelUtils.getLocation((Vertex)source);
      }
    }
    return location;
  }

  public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
    getLogger().debug("Get TargetConnectionAnchor based on ConnectionEditPart");

    return getVertexFigure().getOutputAnchor(getLocation(connEditPart, true), ModelUtils.getLocation((Vertex) getModel()));
  }

  protected void createEditPolicies() {
    if (getParent() instanceof DiagramEditPart)
      installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActorEditPolicy(((DiagramEditPart) getParent()).getMultiPageEditorPart(), this));
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentNodeDeletePolicy(getDiagram(),((DiagramEditPart) getParent()).getMultiPageEditorPart()));
  }

  public ConnectionAnchor getSourceConnectionAnchor(Request request) {
    Point pt = new Point(((DropRequest) request).getLocation());
    return getVertexFigure().getSourceConnectionAnchorAt(pt);
  }

  public ConnectionAnchor getTargetConnectionAnchor(Request request) {
    Point pt = new Point(((DropRequest) request).getLocation());
    return getVertexFigure().getTargetConnectionAnchorAt(pt);
  }
  public Port getSourcePort(ConnectionAnchor anchor) {
    return null;
  }

  public Port getTargetPort(ConnectionAnchor anchor) {
    return null;
  }

}
