package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserellRootEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RouterFactory;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.LinkBendpointEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.RelationDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.RelationEndpointEditPolicy;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;
import com.isencia.passerelle.workbench.model.opm.OPMLink;
import com.isencia.passerelle.workbench.model.ui.command.IRefreshBendpoints;
import com.isencia.passerelle.workbench.model.ui.command.LinkCreateBendpointCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

/**
 * Implements a Relation Editpart to represent a Wire like connection.
 * 
 */
public class LinkEditPart extends AbstractConnectionEditPart implements ChangeListener, EditPartListener {

  public LinkEditPart() {
    super();
    // TODO Auto-generated constructor stub
  }

  private static Logger logger = LoggerFactory.getLogger(LinkEditPart.class);

  private static final Color alive = new Color(Display.getDefault(), 0, 74, 168), dead = new Color(Display.getDefault(), 0, 0, 0);

  private AccessibleEditPart acc;

  public Logger getLogger() {
    return logger;
  }

  public void activate() {
    super.activate();
    if (getSource() != null) {
      getSource().addEditPartListener(this);
    }
  }

  public void deactivate() {
    super.deactivate();
    if (getSource() != null) {
      getSource().removeEditPartListener(this);
    }
  }

  @Override
  protected void refreshVisuals() {
    Connection connection = getConnectionFigure();
    List<Point> modelConstraint = ((OPMLink) getModel()).getBendpoints();
    List<AbsoluteBendpoint> figureConstraint = new ArrayList<AbsoluteBendpoint>();
    for (Point p : modelConstraint) {
      figureConstraint.add(new AbsoluteBendpoint(p));
    }
    connection.setRoutingConstraint(figureConstraint);
  }

  public void activateFigure() {
    super.activateFigure();
    if (getRelation().getRelation() instanceof Changeable) {
      Changeable changeable = (Changeable) getRelation().getRelation();
      changeable.addChangeListener(this);
    }
  }

  public void deactivateFigure() {
    if (getRelation().getRelation() instanceof Changeable) {
      Changeable changeable = (Changeable) getRelation().getRelation();
      changeable.removeChangeListener(this);
    }
    super.deactivateFigure();
  }

  /**
   * Adds extra EditPolicies as required.
   */
  protected void createEditPolicies() {

    installEditPolicy(EditPolicy.CONNECTION_ROLE, new RelationDeletePolicy());
    installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new RelationEndpointEditPolicy());
    installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
    installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new LinkBendpointEditPolicy(((DiagramEditPart)((PasserellRootEditPart) getParent()).getContents()).getMultiPageEditorPart()));

  }

  /**
   * Returns a newly created Figure to represent the connection.
   * 
   * @return The created Figure.
   */
  protected IFigure createFigure() {

    final PolylineConnection connection = RouterFactory.getConnection();
    connection.setForegroundColor(ColorConstants.gray);
    final PasserellRootEditPart root = (PasserellRootEditPart) getRoot();
    connection.setConnectionRouter(RouterFactory.getRouter(root.getScaledLayers()));
    return connection;
  }

  public AccessibleEditPart getAccessibleEditPart() {
    if (acc == null)
      acc = new AccessibleGraphicalEditPart() {
        public void getName(AccessibleEvent e) {
          e.result = "Link";
          // e.result = LogicMessages.Wire_LabelText;
        }
      };

    return acc;
  }

  /**
   * Returns the model of this represented as a Relation.
   * 
   * @return Model of this as <code>Relation</code>
   */
  public LinkWithBendPoints getRelation() {
    return (LinkWithBendPoints) getModel();
  }

  /**
   * Refreshes the visual aspects of this, based upon the model (Wire). It changes the wire color depending on the state
   * of Wire.
   * 
   */
  protected void updateSelected() {

    final EditPart source = getSource();
    if (source != null) {

      final int sel = source.getSelected();
      final Polyline line = (Polyline) getFigure();
      if (sel != SELECTED_NONE) {
        line.setLineWidth(2);

      } else {
        line.setLineWidth(1);
      }
    }
  }

  public void changeExecuted(ChangeRequest changerequest) {
    if (changerequest instanceof ModelChangeRequest) {
      Class<?> type = ((ModelChangeRequest) changerequest).getType();
     if (LinkCreateBendpointCommand.class.equals(type) && getModel().equals(changerequest.getSource())) {
        
        refreshVisuals();
        return;
      }
    }
  }

  public void changeFailed(ChangeRequest change, Exception exception) {
  }

  public void childAdded(EditPart child, int index) {
  }

  public void partActivated(EditPart editpart) {
  }

  public void partDeactivated(EditPart editpart) {
  }

  public void removingChild(EditPart child, int index) {
  }

  public void selectedStateChanged(EditPart editpart) {
    updateSelected();
  }

}
