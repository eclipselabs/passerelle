package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;

import ptolemy.actor.Director;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.DirectorFigure;

public class DirectorEditPart extends AbstractBaseEditPart {

  public final static ImageDescriptor IMAGE_DESCRIPTOR_DIRECTOR = Activator.getImageDescriptor("icons/director.gif");

  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentNodeDeletePolicy(getDiagram()));
  }

  /**
   * Returns a newly created Figure to represent this.
   * 
   * @return Figure of this.
   */
  protected IFigure createFigure() {
    Director directorModel = getDirectorModel();

    return new DirectorFigure(directorModel.getDisplayName() != null ? directorModel.getDisplayName() : "Director", getModel().getClass(), createImage(IMAGE_DESCRIPTOR_DIRECTOR));
  }

  /**
   * Returns the Figure of this as a DirectorFigure.
   * 
   * @return DirectorFigure of this.
   */
  public DirectorFigure getDirectorFigure() {
    return (DirectorFigure) getFigure();
  }

  /**
   * Returns the model of this as a Director.
   * 
   * @return Model of this as an Director.
   */
  public Director getDirectorModel() {
    return (Director) getModel();
  }

  public void setSelected(int i) {
    super.setSelected(i);
    super.refreshVisuals();
  }

}
