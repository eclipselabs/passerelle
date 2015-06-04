package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.palette.TreeViewCreationFactory;

public class DragTargetListener extends ViewerDragAdapter {

  public DragTargetListener(TreeViewer viewer) {
    super(viewer);
    this.viewer = viewer;
  }

  @Override
  public void dragStart(DragSourceEvent event) {
    boolean doit = !getViewer().getSelection().isEmpty();
    if (doit) {
      ITreeSelection selection = (ITreeSelection) getViewer().getSelection();
      if (selection != null && !selection.isEmpty()) {
        final Object selected = selection.getFirstElement();
        if (selected instanceof PaletteItemDefinition) {
          CreationFactory factory = new TreeViewCreationFactory((PaletteItemDefinition) selected);
          event.data = (PaletteItemDefinition) selected;
          TemplateTransfer.getInstance().setTemplate(factory);
        } else {
          doit = false;
        }
      }
    }
    event.doit = doit;
  }

  protected boolean validateTransfer(Object transfer) {
    return transfer instanceof PaletteItemDefinition;
  }

  /**
   * Returns viewer
   * 
   * @return viewer
   */
  protected TreeViewer getViewer() {
    return (TreeViewer) viewer;
  }
}