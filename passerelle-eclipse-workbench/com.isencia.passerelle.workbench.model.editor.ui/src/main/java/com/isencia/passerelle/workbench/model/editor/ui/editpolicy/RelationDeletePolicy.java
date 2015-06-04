package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.ui.command.DeleteLinkCommand;

public class RelationDeletePolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

  protected Command getDeleteCommand(GroupRequest request) {
    DiagramEditPart contents = (DiagramEditPart)getHost().getRoot().getContents();
    LinkHolder linkHolder = contents.getMultiPageEditorPart();
    DeleteLinkCommand deleteCmd = new DeleteLinkCommand((CompositeEntity) contents.getModel(), (Link) getHost().getModel(),linkHolder);
    return deleteCmd;
  }

}
