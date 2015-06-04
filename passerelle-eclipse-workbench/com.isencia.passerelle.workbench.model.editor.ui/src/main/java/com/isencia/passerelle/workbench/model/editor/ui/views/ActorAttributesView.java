package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.ui.internal.parts.IShapeEditPart;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.AbstractBaseEditPart;

/**
 * Optional replacement for PropertiesView which renders the actor properties more simply but with more customizable rules.
 * 
 * @author gerring
 */
public class ActorAttributesView extends ViewPart implements ISelectionListener {

  public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView"; //$NON-NLS-1$

  private ActorAttributesTableViewer viewer;
  private IWorkbenchPart part;

  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    if ((part instanceof PasserelleModelMultiPageEditor) || (part instanceof IDiagramEditor)) {
      this.part = part;
      if (updateSelectedEntity(selection)) {
        return;
      }
      viewer.clear();
    }
  }

  @SuppressWarnings("restriction")
  protected boolean updateSelectedEntity(final ISelection selection) {
    if (!(selection instanceof StructuredSelection)) {
      viewer.clear();
      return false;
    } else {
      NamedObj selectedEntity = null;
      final Object sel = ((StructuredSelection) selection).getFirstElement();
      if (sel instanceof AbstractBaseEditPart) {
        selectedEntity = (NamedObj) ((AbstractBaseEditPart) sel).getModel();
      } else if (sel instanceof IShapeEditPart) {
        IShapeEditPart selectedShapePart = (IShapeEditPart) sel;
        PictogramElement pictogramElement = selectedShapePart.getPictogramElement();
        Object selectedActor = ((IDiagramEditor) part).getDiagramTypeProvider().getFeatureProvider().getBusinessObjectForPictogramElement(pictogramElement);
        selectedEntity = (NamedObj) selectedActor;
      }
      viewer.createTableModel(part, selectedEntity);
      return true;
    }
  }

  /**
   * Create contents of the view part.
   * 
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {
    this.viewer = new ActorAttributesTableViewer(null, part, parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

    if (getSite() != null) {
      getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
      // Required for documentation to work
      getSite().setSelectionProvider(viewer);
    }
  }

  @Override
  public void setFocus() {
    if (!viewer.getTable().isDisposed()) {
      viewer.getTable().setFocus();
    }
  }

  public void dispose() {
    viewer.dispose();
    getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
    super.dispose();
  }

  public ColumnViewer getViewer() {
    return this.viewer;
  }

  public void deleteSelectedParameter() throws IllegalActionException {
    viewer.deleteSelectedParameter();
  }
}
