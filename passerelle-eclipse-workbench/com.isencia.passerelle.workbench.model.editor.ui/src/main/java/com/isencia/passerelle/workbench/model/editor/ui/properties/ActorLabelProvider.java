package com.isencia.passerelle.workbench.model.editor.ui.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;

public class ActorLabelProvider implements ILabelProvider {

  public Image getImage(Object arg0) {
    return null;
  }

  public String getText(Object selection) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object element = structuredSelection.getFirstElement();
      if (element instanceof AbstractBaseEditPart) {
        AbstractBaseEditPart editPart = (AbstractBaseEditPart) element;
        return editPart.getEntity().getName();
      }
    }

    return "";
  }

  public void addListener(ILabelProviderListener arg0) {
  }

  public void dispose() {
  }

  public boolean isLabelProperty(Object arg0, String arg1) {
    return false;
  }

  public void removeListener(ILabelProviderListener arg0) {
  }

}
