package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.NodeEditPart;

import ptolemy.kernel.Port;

public interface IActorNodeEditPart extends NodeEditPart {
  Port getSourcePort(ConnectionAnchor anchor);

  Port getTargetPort(ConnectionAnchor anchor);

}
