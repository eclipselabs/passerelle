package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.HelpUtils;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;

public class HelpAction extends SelectionAction {
  private PasserelleModelMultiPageEditor parent;

  public HelpAction(IEditorPart part, PasserelleModelMultiPageEditor parent) {
    super(part);
    this.parent = parent;
    setLazyEnablementCalculation(true);
  }

  private final String icon = "icons/help.gif";

  @Override
  protected void init() {
    super.init();
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    setText("Help");
    setId(ActionFactory.HELP_CONTENTS.getId());
    Activator.getImageDescriptor(icon);
    setHoverImageDescriptor(Activator.getImageDescriptor(icon));
    setImageDescriptor(Activator.getImageDescriptor(icon));
    setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
    setEnabled(false);

  }

  @Override
  protected boolean calculateEnabled() {
    return true;
  }

  @Override
  public void run() {
    List selection = getSelectedObjects();
    for (Object o : selection) {
      if (o instanceof ActorEditPart) {
        final String path = HelpUtils.getContextId(o);
        WorkbenchHelpSystem.getInstance().displayHelpResource(path);
        break;
      }
    }

  }

  @Override
  protected void setSelection(ISelection selection) {
    // TODO Auto-generated method stub
    super.setSelection(selection);
  }

}
