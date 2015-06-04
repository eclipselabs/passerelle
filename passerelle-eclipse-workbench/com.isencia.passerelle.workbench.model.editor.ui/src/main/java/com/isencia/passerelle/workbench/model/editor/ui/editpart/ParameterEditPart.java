package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import ptolemy.data.expr.Parameter;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ParameterFigure;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class ParameterEditPart extends AbstractBaseEditPart {

//  protected void onChangePropertyResource(Object source) {
//
//    final String nameChanged = ((NamedObj) source).getContainer().getName();
//    final String thisName = ((TextAttribute) getModel()).getName();
//
//    if (!nameChanged.equals(thisName))
//      return;
//
//    if (source instanceof TextAttribute || source instanceof StringAttribute) {
//
//      String label = getText(source);
//
//      // Execute the dummy command force a dirty state
//      ((CommentFigure) getFigure()).setText(label);
//      getFigure().repaint();
//    }
//  }

  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentNodeDeletePolicy(getDiagram()));
  }

  /**
   * Returns a newly created Figure to represent this.
   * 
   * @return Figure of this.
   */
  protected IFigure createFigure() {

    final Parameter model = getParameter();
    final String expression = getText(model);

    ImageDescriptor imageDescriptor = PaletteBuilder.getInstance().getIcon(model.getClass());

    return new ParameterFigure(model.getDisplayName()!=null?model.getDisplayName():model.getName(), 
        expression, model.getClass(), createImage(imageDescriptor));
  }

  public ParameterFigure getParameterFigure() {
    return (ParameterFigure) getFigure();
  }

  protected Parameter getParameter() {
    return (Parameter) getModel();
  }

  public void setSelected(int i) {
    super.setSelected(i);
    super.refreshVisuals();
  }

  protected IPropertySource getPropertySource() {
    return null;
  }

  public String getText(Object source) {
    if (source instanceof Parameter) {
      return ((Parameter) source).getExpression();
    }
    return "";
  }

}
