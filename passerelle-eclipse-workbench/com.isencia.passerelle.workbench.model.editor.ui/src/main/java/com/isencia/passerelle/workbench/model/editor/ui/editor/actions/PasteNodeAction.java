package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.ui.command.PasteNodeCommand;

public class PasteNodeAction extends SelectionAction {

  private PasteNodeCommand getPasteNodeCommand() {
    PasserelleModelEditor workbenchPart = (PasserelleModelEditor) getWorkbenchPart();
    return new PasteNodeCommand(workbenchPart.getParent());
  }

  public PasteNodeAction(PasserelleModelEditor part, CompositeActor actor) {
    super(part);
    setLazyEnablementCalculation(true);
  }

  protected void init() {
    super.init();
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setText("Paste");
    setId(ActionFactory.PASTE.getId());
    setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
    setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
    setEnabled(false);
  }

  private Command createPasteCommand(List<Object> selectedObjects) {
    return getPasteNodeCommand();
  }

  @Override
  protected boolean calculateEnabled() {
    return true;
  }

  @Override
  public void run() {
    Command command = createPasteCommand(getSelectedObjects());
    if (command != null && command.canExecute())
      execute(command);
  }
}
