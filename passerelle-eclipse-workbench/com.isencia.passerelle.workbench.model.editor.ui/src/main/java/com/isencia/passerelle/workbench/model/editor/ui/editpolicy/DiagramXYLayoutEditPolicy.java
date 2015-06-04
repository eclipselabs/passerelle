package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import java.util.List;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.slf4j.Logger;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;

public class DiagramXYLayoutEditPolicy extends org.eclipse.gef.editpolicies.XYLayoutEditPolicy {

  private SetConstraintCommand getSetConstraintCommand() {
    return new SetConstraintCommand();
  }

  private static Logger logger;
  private PasserelleModelMultiPageEditor editor;

  public DiagramXYLayoutEditPolicy(XYLayout layout, PasserelleModelMultiPageEditor editor) {
    super();
    setXyLayout(layout);
    this.editor = editor;
  }

  Logger getLogger() {
    return logger;
  }

  protected Command chainGuideAttachmentCommand(Request request, NamedObj model, Command cmd, boolean horizontal) {
    Command result = cmd;
    return result;
  }

  protected Command chainGuideDetachmentCommand(Request request, NamedObj model, Command cmd, boolean horizontal) {
    Command result = cmd;
    return result;
  }

  protected Command createAddCommand(Request request, EditPart childEditPart, Object constraint) {
    if (getLogger().isDebugEnabled())
      getLogger().debug("createAddCommand for editPart : " + childEditPart);
    return null;
  }

  /**
   * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
   *      java.lang.Object)
   */
  protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
    SetConstraintCommand locationCommand = getSetConstraintCommand();
    locationCommand.setModel((NamedObj) child.getModel());
    Rectangle rectangle = (Rectangle) constraint;
    Point location = rectangle.getLocation();
    locationCommand.setLocation(new double[] { location.x, location.y });
    return locationCommand;
  }

  /**
   * Create Command that will be executed after a move or resize
   */
  protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
    SetConstraintCommand cmd = getSetConstraintCommand();
    cmd.setModel((NamedObj) child.getModel());
    Rectangle rectangle = (Rectangle) constraint;
    Point location = rectangle.getLocation();
    cmd.setLocation(new double[] { location.x, location.y });
    Command result = cmd;

    return result;
  }

  protected EditPolicy createChildEditPolicy(EditPart child) {
    return new DiagramNonResizableEditPolicy();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createSizeOnDropFeedback
   * (org.eclipse.gef.requests.CreateRequest)
   */
  protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
    IFigure figure;

    if (getLogger().isDebugEnabled())
      getLogger().debug("createSizeOnDropFeedback");

    // TODO Check if we shouldn't return a more meaningful figure during DND
    figure = new RectangleFigure();
    ((RectangleFigure) figure).setXOR(true);
    ((RectangleFigure) figure).setFill(true);
    figure.setBackgroundColor(ColorConstants.cyan);
    figure.setForegroundColor(ColorConstants.white);
    // }

    addFeedback(figure);
    // No Figure for the moment
    return null;
  }

  /*
   * protected LogicGuide findGuideAt(int pos, boolean horizontal) { RulerProvider provider =
   * ((RulerProvider)getHost().getViewer().getProperty( horizontal ? RulerProvider.PROPERTY_VERTICAL_RULER :
   * RulerProvider.PROPERTY_HORIZONTAL_RULER)); return (LogicGuide)provider.getGuideAt(pos); }
   */
  protected Command getAddCommand(Request generic) {
    if (getLogger().isDebugEnabled())
      getLogger().debug("getAddCommand");
    ChangeBoundsRequest request = (ChangeBoundsRequest) generic;
    List<?> editParts = request.getEditParts();
    CompoundCommand command = new CompoundCommand();
    command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
    GraphicalEditPart childPart;
    Rectangle r;
    Object constraint;

    for (int i = 0; i < editParts.size(); i++) {
      childPart = (GraphicalEditPart) editParts.get(i);
      r = childPart.getFigure().getBounds().getCopy();
      // convert r to absolute from childpart figure
      childPart.getFigure().translateToAbsolute(r);
      r = request.getTransformedRectangle(r);
      // convert this figure to relative
      getLayoutContainer().translateToRelative(r);
      getLayoutContainer().translateFromParent(r);
      r.translate(getLayoutOrigin().getNegated());
      constraint = getConstraintFor(r);
      command.add(createAddCommand(generic, childPart, translateToModelConstraint(constraint)));
    }
    return command.unwrap();
  }

  /**
   * Override to return the <code>Command</code> to perform an {@link RequestConstants#REQ_CLONE CLONE}. By default,
   * <code>null</code> is returned.
   * 
   * @param request
   *          the Clone Request
   * @return A command to perform the Clone.
   */
  protected Command getCloneCommand(ChangeBoundsRequest request) {
    if (getLogger().isDebugEnabled())
      getLogger().debug("getCloneCommand");

    return null;
  }

  protected Command getCreateCommand(CreateRequest request) {
    try {
            
      CreateComponentCommand create = new CreateComponentCommand(editor, editor.getSelectedContainer());

      if (request.getNewObject() instanceof SubModelPaletteItemDefinition) {
        create.setFlow(((SubModelPaletteItemDefinition) request.getNewObject()).getFlow());
        create.setName(((SubModelPaletteItemDefinition) request.getNewObject()).getName());
      } else {
        create.setName((String) request.getNewObject());
      }
      Class<? extends NamedObj> clazz = (Class<? extends NamedObj>) request.getNewObjectType();
      create.setClazz(clazz);

      Rectangle constraint = (Rectangle) getConstraintFor(request);
      create.setLocation(new double[] { constraint.getLocation().preciseX(), constraint.getLocation().preciseY() });
      create.setLabel("new");
      return create;
    } catch (Exception e) {
      getLogger().error("Error creating CreateComponentCommand", e);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreationFeedbackOffset
   * (org.eclipse.gef.requests.CreateRequest)
   */
  protected Insets getCreationFeedbackOffset(CreateRequest request) {
    // No Insets
    return new Insets();
  }

  /**
   * Returns the layer used for displaying feedback.
   * 
   * @return the feedback layer
   */
  protected IFigure getFeedbackLayer() {
    return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
  }

}
