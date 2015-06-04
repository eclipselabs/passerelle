package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import com.isencia.passerelle.workbench.model.editor.ui.views.DragTargetListener;

public class DefaultDragSupportBuilder implements DragSupportBuilder {

  @Override
  public void addDragSupport(TreeViewer treeViewer) {
    int ops = DND.DROP_MOVE | DND.DROP_COPY;
    Transfer[] transfers = new Transfer[] { TemplateTransfer.getInstance() };
    transfers = new Transfer[] { TemplateTransfer.getInstance() };
    DragTargetListener dragListener = new DragTargetListener(treeViewer);
    treeViewer.addDragSupport(ops, transfers, dragListener);
  }

}
