package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.LinkEditPart;
import com.isencia.passerelle.workbench.model.ui.command.CopyNodeCommand;

public class CopyNodeAction extends SelectionAction {
  public CopyNodeAction(IWorkbenchPart part) {
    super(part);
    setLazyEnablementCalculation(true);
  }

  private CopyNodeCommand CopyNodeCommand;

  private CopyNodeCommand getCopyNodeCommand() {
    if (CopyNodeCommand == null) {
      return CopyNodeCommand = new CopyNodeCommand();
    }
    return CopyNodeCommand;
  }

  @Override
  protected void init() {
    super.init();
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setText("Copy");
    setId(ActionFactory.COPY.getId());
    setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
    setEnabled(false);
  }

  private Command createCopyCommand(List<Object> selectedObjects) {
    if (selectedObjects == null || selectedObjects.isEmpty()) {
      return null;
    }
    CopyNodeCommand cmd = getCopyNodeCommand();
    cmd.emptyElementList();
    Iterator<Object> it = selectedObjects.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      if (!(o instanceof AbstractEditPart)) {
        return null;
      }
      AbstractEditPart ep = (AbstractEditPart) o;

      Object NamedObj = ep.getModel();

      if (!cmd.isCopyableNamedObj(NamedObj))
        return null;
      cmd.addElement(NamedObj);
    }
    return cmd;
  }

  @Override
  protected boolean calculateEnabled() {
    boolean check = checkSelectedObjects();
    return check;
  }

  private boolean checkSelectedObjects() {
    if (getSelectedObjects() == null)
      return false;
    for (Object o : getSelectedObjects()) {
      if (o instanceof AbstractEditPart && !(o instanceof DiagramEditPart)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void run() {
    Command cmd = createCopyCommand(getSelectedObjects());
    if (cmd != null && cmd.canExecute()) {
      cmd.execute();
    }
  }

}
