package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.opm.OPMLink;
import com.isencia.passerelle.workbench.model.ui.command.LinkCreateBendpointCommand;
import com.isencia.passerelle.workbench.model.ui.command.LinkMoveBendpointCommand;
 
/**
 * Policy used by the {@link OPMLink} to manage link bendpoints. 
 * @author vainolo
 *
 */
public class LinkBendpointEditPolicy extends BendpointEditPolicy {
 
  public LinkBendpointEditPolicy(PasserelleModelMultiPageEditor editor) {
    super();
    this.editor = editor;
  }

  private PasserelleModelMultiPageEditor editor;
  
  /**
   * {@inheritDoc}
   */
  @Override protected Command getCreateBendpointCommand(final BendpointRequest request) {
    LinkCreateBendpointCommand command = new LinkCreateBendpointCommand(editor);
     
    Point p = request.getLocation();
     
    command.setOPMLink((OPMLink) request.getSource().getModel());
    command.setLocation(p);
    command.setIndex(request.getIndex());
     
    return command;
  }
 
  /**
   * {@inheritDoc}
   */
  @Override protected Command getMoveBendpointCommand(final BendpointRequest request) {
     
    LinkMoveBendpointCommand command = new LinkMoveBendpointCommand((CompositeEntity) getHost().getRoot().getContents().getModel());
    
    Point p = request.getLocation();
     
    command.setOPMLink((OPMLink) request.getSource().getModel());
    command.setLocation(p);
    command.setIndex(request.getIndex());
     
    return command;
  }
 
  /**
   * {@inheritDoc}
   */
  @Override protected Command getDeleteBendpointCommand(final BendpointRequest request) {
    return null;
  }
}