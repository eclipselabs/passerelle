package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

/**
 * Provides support for Container EditParts.
 */
abstract public class ContainerEditPart extends AbstractBaseEditPart {
  private final static Logger LOGGER = LoggerFactory.getLogger(ContainerEditPart.class);

  private PasserelleModelMultiPageEditor editor;

  private boolean showChildren = true;
  // This actor will be used as offset. It's not possible to have multiple editors with different model
  private CompositeActor actor;

  public CompositeActor getCompositeActor() {
    return actor;
  }

  public ContainerEditPart(PasserelleModelMultiPageEditor editor, CompositeActor actor) {
    super();
    this.editor = editor;
    this.actor = actor;
  }

  public ContainerEditPart(PasserelleModelMultiPageEditor editor, boolean showChildren) {
    super();
    this.editor = editor;
    this.showChildren = showChildren;
  }

  /**
   * Installs the desired EditPolicies for this.
   */
  protected void createEditPolicies() {
  }

  /**
   * Returns the model of this as a CompositeActor.
   * 
   * @return CompositeActor of this.
   */
  protected CompositeActor getModelDiagram(CompositeActor actor) {
    return actor != null ? actor : (CompositeActor) getModel();
  }

  /**
   * Returns the children of this through the model.
   * 
   * @return Children of this as a List.
   */
  @SuppressWarnings("unchecked")
  protected List<NamedObj> getModelChildren() {
    if (!showChildren) {
      return Collections.emptyList();
    }
    CompositeActor modelDiagram = getModelDiagram(actor);
    if (editor != null) {
      try {
        CompositeActor selectedActor = editor.getSelectedContainer();
        if (selectedActor != null && !containsActor(selectedActor, actor)) {
          modelDiagram = selectedActor;
        }
      } catch (Exception e) {
        LOGGER.error("Error getting container model object", e);
      }
    }
    List<NamedObj> children = new ArrayList<NamedObj>();
    LinkHolder linkHolder = getLinkHolder();
    if (linkHolder != null) {
      linkHolder.generateLinks(modelDiagram);
    }
    List<NamedObj> entities = modelDiagram.entityList();
    if (entities != null) {
      children.addAll(entities);
    }
    if (modelDiagram.getContainer() == null && modelDiagram.getDirector() != null) {
      children.add(modelDiagram.getDirector());
    }
    children.addAll(modelDiagram.attributeList(Parameter.class));
    children.addAll(modelDiagram.attributeList(TextAttribute.class));
    children.addAll(modelDiagram.attributeList(IOPort.class));
    children.addAll(modelDiagram.inputPortList());
    children.addAll(modelDiagram.outputPortList());
    List<Relation> relations = modelDiagram.relationList();
    for (Relation relation : relations) {
      children.addAll(getVertexModelChildren(relation));
    }
    return children;
  }

  public LinkHolder getLinkHolder() {
    DiagramEditPart diagram =  (this instanceof DiagramEditPart) ? (DiagramEditPart) this: getDiagram();
    return diagram.getMultiPageEditorPart();
  }

  @SuppressWarnings("unchecked")
  protected List<NamedObj> getVertexModelChildren(Relation relation) {
    List<NamedObj> children = new ArrayList<NamedObj>();
    children.addAll(relation.attributeList(Vertex.class));
    return children;
  }

  public boolean containsActor(CompositeActor parent, CompositeActor child) {
    @SuppressWarnings("unchecked")
    List<Entity> entities = parent.entityList();
    for (Entity el : entities) {
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
