package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class ReConnectLinkCommand extends Command {

  private CompositeEntity parent;
  private IPasserelleMultiPageEditor editor;

  private DeleteLinkCommand deleteLinkCommand;
  private CreateConnectionCommand createLinkCommand;

  @Override
  public boolean canExecute() {
    // TODO Auto-generated method stub
    return (((source != null && target != null) || link != null) && (newTarget != null || newSource != null));
  }

  private static final Logger logger = LoggerFactory.getLogger(ReConnectLinkCommand.class);

  private Link link;

  public Link getLink() {
    return link;
  }

  public void setLink(Link link) {
    this.link = link;
  }

  protected NamedObj source;

  public IPasserelleMultiPageEditor getEditor() {
    return editor;
  }

  public void setEditor(IPasserelleMultiPageEditor editor) {
    this.editor = editor;
  }

  public NamedObj getSource() {
    return (NamedObj) link.getHead();
  }

  public NamedObj getTarget() {
    return (NamedObj) link.getTail();
  }

  protected NamedObj newSource;

  public NamedObj getNewSource() {
    return newSource;
  }

  public void setNewSource(NamedObj newSource) {
    this.newSource = newSource;
  }

  protected NamedObj newTarget;

  public NamedObj getNewTarget() {
    return newTarget;
  }

  public void setNewTarget(NamedObj newTarget) {
    this.newTarget = newTarget;
  }

  protected NamedObj target;

  public ReConnectLinkCommand() {
    super("Reconnect");
  }

  public ReConnectLinkCommand(IPasserelleMultiPageEditor editor, Link link) {
    super();
    this.editor = editor;
    this.parent = this.editor.getSelectedContainer();
    this.link = link;
  }

  public Logger getLogger() {
    return logger;
  }

  public void execute() {
    doExecute();
  }

  protected void doExecute() {
    if (newSource != null || newTarget != null) {
      deleteLinkCommand = new DeleteLinkCommand(parent, link, editor);
      deleteLinkCommand.doExecute();
      createLinkCommand = new CreateConnectionCommand(editor);
      if (newSource != null) {
        createLinkCommand.setTarget(getTarget());
        createLinkCommand.setSource(newSource);
      } else {
        createLinkCommand.setTarget(newTarget);
        createLinkCommand.setSource((NamedObj) link.getHead());
      }
      createLinkCommand.doExecute();

    }

  }

  public void redo() {
    doExecute();
  }

  public void setParent(CompositeEntity p) {
    parent = p;
  }

  public void undo() {
    try {
      createLinkCommand.undo();
      // deleteLinkCommand.undo();
    } catch (Exception e) {

    }
  }

}
