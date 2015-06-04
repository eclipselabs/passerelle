package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.ChangeRequest;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.DiagramXYLayoutEditPolicy;

/**
 * Holds all other ModelEditParts under this. It is activated by ModelEditor, to hold the entire model. It is sort of a
 * blank board where all other EditParts get added.
 */
public class DiagramEditPart extends ContainerEditPart implements LayerConstants {

  private PasserelleModelMultiPageEditor multiPageEditorPart;

  public DiagramEditPart(PasserelleModelMultiPageEditor parent, CompositeActor actor) {
    super(parent,actor);
    this.multiPageEditorPart = parent;
  }

  public PasserelleModelMultiPageEditor getMultiPageEditorPart() {
    return multiPageEditorPart;
  }

  /**
   * Installs EditPolicies specific to this.
   */
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.NODE_ROLE, null);
    installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
    installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramXYLayoutEditPolicy((XYLayout) getContentPane().getLayoutManager(), multiPageEditorPart));

    installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
  }

  /**
   * Returns a Figure to represent this.
   * 
   * @return Figure.
   */
  protected IFigure createFigure() {
    Figure f = new FreeformLayer();
    f.setLayoutManager(new FreeformLayout());
    f.setBorder(new MarginBorder(5));
    return f;
  }

  public void repaint() {
    getFigure().repaint();
  }

  @Override
  public void changeExecuted(ChangeRequest changerequest) {
    refreshChildren();
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter(Class adapter) {
    if (adapter == SnapToHelper.class) {
      List snapStrategies = new ArrayList();
      Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
      if (val != null && val.booleanValue())
        snapStrategies.add(new SnapToGuides(this));
      val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
      if (val != null && val.booleanValue())
        snapStrategies.add(new SnapToGeometry(this));
      val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
      if (val != null && val.booleanValue())
        snapStrategies.add(new SnapToGrid(this));

      if (snapStrategies.size() == 0)
        return null;
      if (snapStrategies.size() == 1)
        return snapStrategies.get(0);

      SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
      for (int i = 0; i < snapStrategies.size(); i++)
        ss[i] = (SnapToHelper) snapStrategies.get(i);
      return new CompoundSnapToHelper(ss);
    }
    return super.getAdapter(adapter);
  }

  public DragTracker getDragTracker(Request req) {
    if (req instanceof SelectionRequest && ((SelectionRequest) req).getLastButtonPressed() == 3)
      return new DeselectAllTracker(this);
    return new MarqueeDragTracker();
  }

  /**
   * Returns <code>NULL</code> as it does not hold any connections.
   * 
   * @return ConnectionAnchor
   */
  public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart editPart) {
    // Not source connection anchor for Diagram
    return null;
  }

  /**
   * Returns <code>NULL</code> as it does not hold any connections.
   * 
   * @return ConnectionAnchor
   */
  public ConnectionAnchor getSourceConnectionAnchor(int x, int y) {
    // Not source connection anchor for Diagram
    return null;
  }

  /**
   * Returns <code>NULL</code> as it does not hold any connections.
   * 
   * @return ConnectionAnchor
   */
  public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart editPart) {
    // Not target connection anchor for Diagram
    return null;
  }

  /**
   * Returns <code>NULL</code> as it does not hold any connections.
   * 
   * @return ConnectionAnchor
   */
  public ConnectionAnchor getTargetConnectionAnchor(int x, int y) {
    // Not target connection anchor for Diagram
    return null;
  }

}
