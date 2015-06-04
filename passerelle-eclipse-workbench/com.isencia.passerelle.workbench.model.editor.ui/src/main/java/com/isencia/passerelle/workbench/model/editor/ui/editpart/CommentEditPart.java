package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CommentFigure;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class CommentEditPart extends AbstractBaseEditPart {

  protected void onChangePropertyResource(Object source) {

    final String nameChanged = ((NamedObj) source).getContainer().getName();
    final String thisName = ((TextAttribute) getModel()).getName();

    if (!nameChanged.equals(thisName))
      return;

    if (source instanceof TextAttribute || source instanceof StringAttribute) {

      String label = getText(source);

      // Execute the dummy command force a dirty state
      ((CommentFigure) getFigure()).setText(label);
      getFigure().repaint();
    }
  }

  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentNodeDeletePolicy(getDiagram()));
  }

  /**
   * Returns a newly created Figure to represent this.
   * 
   * @return Figure of this.
   */
  protected IFigure createFigure() {

    final Object model = getModel();
    final String label = getText(model);

    ImageDescriptor imageDescriptor = PaletteBuilder.getInstance().getIcon(TextAttribute.class);

    return new CommentFigure(label, createImage(imageDescriptor));
  }

  public CommentFigure getCommentFigure() {
    return (CommentFigure) getFigure();
  }

  protected TextAttribute getTextAttribute() {
    return (TextAttribute) getModel();
  }

  public void setSelected(int i) {
    super.setSelected(i);
    super.refreshVisuals();
  }

  protected IPropertySource getPropertySource() {

    return null;
  }

  public String getText(Object source) {
    if (source instanceof StringAttribute) {
      return ((StringAttribute) source).getExpression();
    }
    if (source instanceof TextAttribute) {
      Attribute attribute = ((TextAttribute) source).getAttribute("text");
      if (attribute instanceof StringAttribute)
        return ((StringAttribute) attribute).getExpression();
    }
    return "";
  }

}
