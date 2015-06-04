package com.isencia.passerelle.workbench.model.ui.command;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;

public class CopyNodeCommand extends Command {
  protected ArrayList<Object> list = new ArrayList<Object>();

  public boolean addElement(Object NamedObj) {
    if (!list.contains(NamedObj)) {
      if (isCopyableNamedObj(NamedObj))
        return list.add(NamedObj);
    }
    return false;
  }

  public void emptyElementList() {
    list.clear();
  }

  @Override
  public boolean canExecute() {
    if (list == null || list.isEmpty())
      return false;
    Iterator<Object> it = list.iterator();
    while (it.hasNext()) {
      if (!isCopyableNamedObj(it.next()))
        return false;
    }
    return true;
  }

  @Override
  public void execute() {
    if (canExecute())
      Clipboard.getDefault().setContents(list);
  }

  @Override
  public boolean canUndo() {
    return false;
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
}
