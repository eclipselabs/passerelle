package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import java.util.Enumeration;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;

/**
 * Defines the possible commands on the components
 * 
 * @author Dirk Jacobs
 * 
 */
public class ComponentNodeDeletePolicy extends org.eclipse.gef.editpolicies.ComponentEditPolicy {
  private DiagramEditPart diagram;
  private PasserelleModelMultiPageEditor multiPageEditor;

  private DeleteComponentCommand getDeleteComponentCommand() {
    return new DeleteComponentCommand();
  }

  public ComponentNodeDeletePolicy(DiagramEditPart diagram) {

    this.diagram = diagram;
  }

  public ComponentNodeDeletePolicy(DiagramEditPart diagram, PasserelleModelMultiPageEditor multiPageEditor) {
    this(diagram);
    this.multiPageEditor = multiPageEditor;
  }

  protected Command createDeleteCommand(GroupRequest request) {
    NamedObj child = (NamedObj) getHost().getModel();
    Object parent = getHost().getParent().getModel();
    DeleteComponentCommand deleteCmd = getDeleteComponentCommand();
    deleteCmd.setMultiPageEditor(multiPageEditor);
    if (multiPageEditor != null && child instanceof CompositeActor) {
      deleteCmd.emptyIndexList();
      addCompositeActorIndex(child, deleteCmd);
    }
    deleteCmd.setParent((CompositeEntity) parent);
    deleteCmd.setChild(child);

    return deleteCmd;
  }

  private void addCompositeActorIndex(NamedObj child, DeleteComponentCommand deleteCmd) {

    if (child instanceof CompositeActor) {
      int index = multiPageEditor.getPageIndex((CompositeActor) child);
      if (index != -1) {
        deleteCmd.addIndex(index);
      }
      Enumeration entities = ((CompositeActor) child).getEntities();
      while (entities.hasMoreElements()) {
        addCompositeActorIndex((NamedObj) entities.nextElement(), deleteCmd);
      }

    }
  }

}
