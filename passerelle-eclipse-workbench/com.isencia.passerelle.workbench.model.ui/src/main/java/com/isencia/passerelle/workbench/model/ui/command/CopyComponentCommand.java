package com.isencia.passerelle.workbench.model.ui.command;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.business.ICommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class CopyComponentCommand extends org.eclipse.gef.commands.Command implements ICommand {
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.commands.Command#undo()
   */
  @Override
  public void undo() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
      @Override
      protected void _execute() throws Exception {
        copyCommand.undo();
      }

    });
  }

  com.isencia.passerelle.editor.common.business.CopyComponentCommand copyCommand;
  CompositeEntity parent;

  public CopyComponentCommand(CompositeEntity parent, NamedObj child,double[] location) {
    copyCommand = new com.isencia.passerelle.editor.common.business.CopyComponentCommand(parent, child,location);
    this.parent = parent;
  }

  @Override
  public boolean canExecute() {
    return true;
  }

  public void execute() {
    doExecute();
  }

  public void doExecute() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
      @Override
      protected void _execute() throws Exception {
        copyCommand.execute();
      }

    });
  }

  public void redo() {
    doExecute();

  }

  @Override
  public boolean canUndo() {
    return true;
  }
  public NamedObj getNewChild() {
    return copyCommand.getNewChild();
  }

}
