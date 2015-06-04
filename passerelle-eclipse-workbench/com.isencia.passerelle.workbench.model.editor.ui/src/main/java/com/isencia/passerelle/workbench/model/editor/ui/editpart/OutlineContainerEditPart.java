package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.ImageDescriptor;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

/**
 * Tree EditPart for the Container.
 */
public class OutlineContainerEditPart extends OutlineEditPart {
  private EditPart context;
  public final static ImageDescriptor IMAGE_DESCRIPTOR_OUTLINE = Activator.getImageDescriptor("icons/compound.gif");
  private PasserelleModelMultiPageEditor editor;

  /**
   * Constructor, which initializes this using the model given as input.
   */
  public OutlineContainerEditPart(EditPart context, Object model, PasserelleModelMultiPageEditor editor) {
    super(model);
    this.context = context;
    this.editor = editor;
  }

  /**
   * Creates and installs pertinent EditPolicies.
   */
  protected void createEditPolicies() {
    super.createEditPolicies();
  }

  /**
   * Returns the model of this as a CompositeActor.
   * 
   * @return Model of this.
   */
  protected CompositeActor getModelDiagram(CompositeActor actor) {
    if (actor == null)
      return (CompositeActor) getModel();
    return actor;
  }

  @Override
  protected void refreshVisuals() {
    super.refreshVisuals();
    setWidgetImage(IMAGE_DESCRIPTOR_OUTLINE.createImage());
  }

  /**
   * Returns the children of this from the model, as this is capable enough of holding EditParts.
   * 
   * @return List of children.
   */
  protected List getModelChildren() {
    ArrayList children = new ArrayList();

    CompositeActor actor = getModelDiagram((CompositeActor)getModel());
    if (editor != null) {
      PasserelleModelEditor page = editor.getEditor();
      CompositeActor selectedActor = editor.getSelectedContainer();
      if (selectedActor != null && !containsActor(selectedActor, actor))
        actor = selectedActor;
    }

    children.addAll(actor.attributeList(Parameter.class));
    children.addAll(actor.attributeList(TextAttribute.class));
    children.addAll(actor.attributeList(IOPort.class));
    children.addAll(actor.inputPortList());
    children.addAll(actor.outputPortList());
    List entities = actor.entityList();
    if (entities != null)
      children.addAll(entities);
    // Only show children 1 level deep
    boolean showChildren = !(context != null && context.getParent() != null);
    if (!showChildren)
      return children;

    if (actor.isOpaque())
      children.add(actor.getDirector());

    return children;
  }

  public boolean containsActor(CompositeActor parent, CompositeActor child) {
    Enumeration entities = parent.getEntities();
    while (entities.hasMoreElements()) {
      Object el = entities.nextElement();
      if (el == child) {
        return true;
      }
      if (el instanceof CompositeActor) {
        if (containsActor((CompositeActor) el, child)) {
          return true;
        }
      }
    }
    return false;
  }
}
