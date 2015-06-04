package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.business.ICommand;
import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.utils.EditorUtils;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;

public class PasteNodeCommand extends Command {
  private IPasserelleMultiPageEditor editor;

  public PasteNodeCommand(IPasserelleMultiPageEditor editor) {
    super();
    this.editor = editor;
  }

  private List<ICommand> list = new ArrayList<ICommand>();

  @Override
  public boolean canExecute() {
    ArrayList clipBoardList = (ArrayList) Clipboard.getDefault().getContents();
    if (clipBoardList == null || clipBoardList.isEmpty())
      return false;
    return true;
  }

  public boolean isPastableNamedObj(NamedObj namedObj) {
    if (namedObj instanceof Director)
      return false;
    return true;
  }

  private NamedObj getParent(NamedObj actor) {
    if (actor == null)
      return null;
    if (actor.getContainer() == null) {
      return actor;
    }
    return (getParent(actor.getContainer()));
  }

  @Override
  public void execute() {
    if (!canExecute())
      return;
    ArrayList clipboardList = (ArrayList) Clipboard.getDefault().getContents();
    CompositeActor selectedContainer = editor.getSelectedContainer();
    Map<NamedObj, NamedObj> map = new HashMap<NamedObj, NamedObj>();

    Iterator<Object> it = clipboardList.iterator();
    list.clear();
    // first create all the copied nodes: the nodes have to exist before you
    // can create the copied connections
    while (it.hasNext()) {
      try {
        Object o = it.next();
        if (o instanceof NamedObj) {
          NamedObj child = (NamedObj) o;
          double[] location = EditorUtils.getLocation(child);
          CopyComponentCommand cmd = new CopyComponentCommand(selectedContainer, child,new double[] { location[0] + 100, location[1] + 100 });
         
          cmd.execute();
          list.add(cmd);
          NamedObj newChild = cmd.getNewChild();
          map.put(child, newChild);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    it = clipboardList.iterator();
    boolean hasLinks = false;
    while (it.hasNext()) {
      try {
        Object o = it.next();
        if (o instanceof LinkWithBendPoints) {
          hasLinks = true;
          Link link = (Link) o;
          NamedObj head = (NamedObj) link.getHead();
          NamedObj tail = (NamedObj) link.getTail();
          NamedObj newHead = map.get(head);
          if (newHead == null) {
            newHead = searchPort(head, map);
          }
          NamedObj newTail = map.get(tail);
          if (newTail == null) {
            newTail = searchPort(tail, map);
          }

          if (newTail != null && newHead != null) {
            CreateConnectionCommand createLinkCommand = new CreateConnectionCommand(newHead, newTail, editor);
            list.add(createLinkCommand);
            createLinkCommand.execute();
          }
        }
      } catch (Exception e) {
        redo();
      }
    }

  }

  @Override
  public void redo() {
    Iterator<ICommand> it = list.iterator();
    while (it.hasNext()) {
      ICommand cmd = it.next();
      if (cmd != null)
        cmd.redo();
    }
  }

  @Override
  public boolean canUndo() {
    return !(list.isEmpty());
  }

  @Override
  public void undo() {
    Iterator<ICommand> it = list.iterator();
    while (it.hasNext()) {
      ICommand cmd = it.next();
      cmd.undo();
    }
  }

  private NamedObj searchPort(NamedObj head, Map<NamedObj, NamedObj> map) {
    NamedObj container = head.getContainer();
    if (container == null) {
      return null;
    }
    Actor parentActor = (Actor) map.get(container);
    if (head instanceof IOPort) {
      IOPort port = (IOPort) head;
      String portName = port.getName();
      if (parentActor instanceof ComponentEntity) {
        ComponentEntity ce = (ComponentEntity) parentActor;
        for (Object p : ce.portList()) {
          Port cPort = (Port) p;
          if (cPort.getName().equals(portName)) {
            return cPort;
          }
        }
      }
    }
    return null;
  }
}
