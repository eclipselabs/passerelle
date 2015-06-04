/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.editor.IDiagramEditorInput;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.graphiti.feature.ModelElementCreateFeatureFromPaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.graphiti.input.PasserelleEditorInputFactory;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserelleIndependenceSolver;
import com.isencia.passerelle.workbench.model.editor.graphiti.outline.DiagramEditorOutlinePage;
import com.isencia.passerelle.workbench.model.editor.ui.palette.DragSupportBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorPalettePage;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeViewerPage;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class PasserelleDiagramEditor extends DiagramEditor {

  public final static String EDITOR_ID = "com.isencia.passerelle.workbench.model.editor.graphiti.PasserelleDiagramEditor";

  @Override
  protected DiagramEditorInput convertToDiagramEditorInput(IEditorInput input) throws PartInitException {
    IEditorInput newInput = PasserelleEditorInputFactory.adaptToDiagramEditorInput(input);
    if (!(newInput instanceof IDiagramEditorInput)) {
      throw new PartInitException("Unknown editor input: " + input); //$NON-NLS-1$
    }
    return (DiagramEditorInput) newInput;
  }
  
  @Override
  protected DiagramBehavior createDiagramBehavior() {
    return new PasserelleDiagramBehavior(this);
  }

  public PasserelleIndependenceSolver getIndependenceSolver() {
    return ((PasserelleDiagramTypeProvider) getDiagramTypeProvider()).getIndependenceSolver();
  }

  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);

    getDiagramBehavior().getRefreshBehavior().refresh();

    getSite().getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          EclipseUtils.getActivePage().showView(ActorAttributesView.ID);
        } catch (Throwable ignored) {
          ignored.printStackTrace();
        }
      }
    });
  }

  public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
    if (IContentOutlinePage.class.equals(type)) {
      DiagramEditorOutlinePage outlinePage = new DiagramEditorOutlinePage(this);
      return outlinePage;
    }
    if (type == ActorPalettePage.class || type == Page.class) {
      ActorTreeViewerPage actorTreeViewPage = new ActorTreeViewerPage(getActionRegistry(), new MyDragSupportBuilder());
      return actorTreeViewPage;
    }
    return super.getAdapter(type);
  }

  private class MyDragSupportBuilder implements DragSupportBuilder {
    @Override
    public void addDragSupport(TreeViewer treeViewer) {
      int ops = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transfers = new Transfer[] { TemplateTransfer.getInstance() };
      SelectionDragAdapter dragListener = new SelectionDragAdapter(treeViewer);
      treeViewer.addDragSupport(ops, transfers, dragListener);
    }
  }

  public class SelectionDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    private TreeViewer fViewer;

    public SelectionDragAdapter(TreeViewer viewer) {
      Assert.isNotNull(viewer);
      fViewer = viewer;
    }

    public Transfer getTransfer() {
      return TemplateTransfer.getInstance();
    }

    public void dragStart(DragSourceEvent event) {
      ISelection _selection = fViewer.getSelection();
      boolean doit = !_selection.isEmpty();
      if (doit) {
        if (_selection instanceof ITreeSelection) {
          ITreeSelection selection = (ITreeSelection) _selection;
          final Object selected = selection.getFirstElement();
          if (selected instanceof PaletteItemDefinition) {
            TemplateTransfer.getInstance().setTemplate(new PaletteItemDefCreationFactory((PaletteItemDefinition) selected));
          }
        } else {
          doit = false;
        }
      }
      event.doit = doit;
    }

    public void dragSetData(DragSourceEvent event) {
      event.data = TemplateTransfer.getInstance().getTemplate();
    }

    public void dragFinished(DragSourceEvent event) {
      TemplateTransfer.getInstance().setTemplate(null);
    }
  }

  public class PaletteItemDefCreationFactory implements CreationFactory {
    PaletteItemDefinition selected;

    public PaletteItemDefCreationFactory(PaletteItemDefinition selected) {
      this.selected = selected;
    }

    public Object getObjectType() {
      return ICreateFeature.class;
    }

    public Object getNewObject() {
      return new ModelElementCreateFeatureFromPaletteItemDefinition(selected, getDiagramTypeProvider().getFeatureProvider());
    }
  }

}
