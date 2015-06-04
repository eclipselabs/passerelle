package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundIOFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundInputFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompoundOutputFigure;

/**
 * <code>PortEditPart</code> is the EditPart for the Port model objects
 * 
 * @author Dirk Jacobs
 */
public class PortEditPart extends ActorEditPart {
  private boolean isInput;

  public PortEditPart(boolean isInput) {
    super();
    this.isInput = isInput;
  }

  @Override
  protected IFigure createFigure() {
    if (isInput)
      return new CompoundInputFigure(((IOPort) getModel()).getName(), getModel().getClass());
    else
      return new CompoundOutputFigure(((IOPort) getModel()).getName(), getModel().getClass());
  }

  public CompoundIOFigure getComponentFigure() {
    return (CompoundIOFigure) getFigure();
  }

  @Override
  protected List getModelSourceConnections() {
    if (isInput) {
      return getPortConnections();
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  protected List getModelTargetConnections() {
    if (!isInput) {
      return getPortConnections();
    }
    return Collections.EMPTY_LIST;
  }

  protected List getPortConnections() {
    List allLinks = new ArrayList();
    Set<Link> links = getDiagram().getLinkHolder().getLinks(getModel());
    if (links != null) {
      for (Link link : links) {
        NamedObj container = ((IOPort) getModel()).getContainer();
        NamedObj relContainer = link.getRelation().getContainer();
        if (container != null && container.equals(relContainer)) {
          allLinks.add(link);
        }
      }

    }
    return allLinks;
  }



  public Port getSourcePort(ConnectionAnchor anchor) {
    getLogger().trace("Get Source port  based on anchor");
    IOPort port = ((TypedIOPort) getModel());
    if (port.isInput()) {
      return port;
    }
    return null;

  }

  public Port getTargetPort(ConnectionAnchor anchor) {
    getLogger().trace("Get Target port  based on anchor");

    IOPort port = ((TypedIOPort) getModel());
    if (!port.isInput()) {
      return port;
    }
    return null;
  }

  public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
    return getComponentFigure().getConnectionAnchor(CompoundInputFigure.INPUT_PORT_NAME);
  }

  public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
    return getComponentFigure().getConnectionAnchor(CompoundOutputFigure.OUTPUT_PORT_NAME);

  }

}
