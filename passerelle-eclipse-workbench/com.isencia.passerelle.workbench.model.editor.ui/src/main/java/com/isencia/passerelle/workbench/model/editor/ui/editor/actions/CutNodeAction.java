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

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.LinkEditPart;
import com.isencia.passerelle.workbench.model.ui.command.CutNodeCommand;

public class CutNodeAction extends SelectionAction {
  private CutNodeCommand CutNodeCommand;

  private CutNodeCommand getCutNodeCommand() {
    if (CutNodeCommand == null) {
      PasserelleModelEditor workbenchPart = (PasserelleModelEditor) getWorkbenchPart();
      return CutNodeCommand = new CutNodeCommand(workbenchPart.getParent());
    }
    return CutNodeCommand;
  }

  public CutNodeAction(IWorkbenchPart part) {
    super(part);
    setLazyEnablementCalculation(true);
  }

  protected void init() {
    setId(ActionFactory.CUT.getId());
    setText("Cut");
    setToolTipText("Cut");
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
    setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
    setEnabled(false);
  }

  private Command createCutCommand(List<Object> selectedObjects) {
    if (selectedObjects == null || selectedObjects.isEmpty()) {
      return null;
    }
    CutNodeCommand cmd = getCutNodeCommand();
    cmd.emptyElementList();
    Iterator<Object> it = selectedObjects.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      if (!(o instanceof AbstractEditPart)) {
        return null;
      }
      if (o instanceof AbstractBaseEditPart || o instanceof LinkEditPart) {
        AbstractEditPart ep = (AbstractEditPart) o;

        Object NamedObj = ep.getModel();

        if (!cmd.isCopyableNamedObj(NamedObj))
          return null;
        cmd.addElement(NamedObj);
      }
    }
    return cmd;
  }

  @Override
  protected boolean calculateEnabled() {
    return checkSelectedObjects();
  }

  private boolean checkSelectedObjects() {
    if (getSelectedObjects() == null)
      return false;
    for (Object o : getSelectedObjects()) {
      if (o instanceof EditPart && !(o instanceof DiagramEditPart)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void run() {
    Command cmd = createCutCommand(getSelectedObjects());
    if (cmd != null && cmd.canExecute()) {
      execute(cmd);
    }
  }

}
