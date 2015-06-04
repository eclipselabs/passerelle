package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;

public class CutNodeCommand extends CopyNodeCommand {
  private IPasserelleMultiPageEditor editor;

  public CutNodeCommand(IPasserelleMultiPageEditor multiPageEditor) {
    super();
    this.editor = multiPageEditor;
  }

  private List<Command> cmdStack = new ArrayList<Command>();

  @Override
  public void execute() {
    if (canExecute()) {
      super.execute();
      Iterator it = list.iterator();
      while (it.hasNext()) {
        Object o = it.next();
        if (o instanceof LinkWithBendPoints) {
          Link link = (Link) o;
          cmdStack.add(new DeleteLinkCommand((CompositeEntity) link.getRelation().getContainer(), link,editor));
        }
      }

      it = list.iterator();
      while (it.hasNext()) {
        Object o = it.next();
        if (o instanceof NamedObj) {
          NamedObj no = (NamedObj) o;
          DeleteComponentCommand deleteComponentCommand = new DeleteComponentCommand();
          deleteComponentCommand.setChild(no);
          deleteComponentCommand.setParent((CompositeEntity) no.getContainer());
          cmdStack.add(deleteComponentCommand);
        }

      }
      Clipboard.getDefault().setContents(list);
    }
  }

  @Override
  public void undo() {
    Iterator<Command> it = cmdStack.iterator();
    while (it.hasNext()) {
      Command cmd = it.next();
      cmd.undo();
    }

  }

  @Override
  public void redo() {
    Iterator<Command> it = cmdStack.iterator();
    while (it.hasNext()) {
      Command cmd = it.next();
      cmd.redo();
    }
  }

  public boolean isCopyableNamedObj(Object NamedObj) {
    if (NamedObj instanceof Director)
      return false;
    return true;
  }

  private IOPort searchPort(Enumeration enumeration) {
    while (enumeration.hasMoreElements()) {
      return (IOPort) enumeration.nextElement();
    }
    return null;
  }

  @Override
  public boolean canUndo() {
    return !(list.isEmpty());
  }
}
