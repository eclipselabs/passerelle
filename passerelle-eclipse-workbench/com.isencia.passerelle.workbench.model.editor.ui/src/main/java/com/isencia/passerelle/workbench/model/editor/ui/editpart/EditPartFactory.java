package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.actor.Source;
import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;

public class EditPartFactory implements org.eclipse.gef.EditPartFactory {
  Map<Object, EditPart> componentsMap = new HashMap<Object, EditPart>();
  Map<EditPart, Object> modelObjMap = new HashMap<EditPart, Object>();
 
  public Link getLink(EditPart id) {
    Object mo = getModelObj(id);
    if (mo instanceof LinkWithBendPoints) {
      return (Link) mo;
    }
    return null;
  }
  

  public Object getModelObj(EditPart id) {
    return modelObjMap.get(id);
  }

  private Set<AbstractBaseEditPart> parts = new HashSet<AbstractBaseEditPart>();

  public Set<AbstractBaseEditPart> getParts() {
    return parts;
  }

  public MultiPageEditorPart getParent() {
    return parent;
  }

  private static Logger logger = LoggerFactory.getLogger(EditPartFactory.class);

  protected PasserelleModelMultiPageEditor parent;
  private CompositeActor actor;

  public EditPartFactory(PasserelleModelMultiPageEditor parent) {
    super();
    this.parent = parent;
  }

  public EditPartFactory(PasserelleModelMultiPageEditor parent, CompositeActor actor) {
    super();
    this.parent = parent;
    this.actor = actor;
  }

  private Logger getLogger() {
    return logger;
  }

  /**
   * Create an EditPart based on the type of the model
   */
  public EditPart createEditPart(EditPart context, Object model) {
    EditPart editPart = createInnerEditPart(context, model);
    if (editPart != null) {
      componentsMap.put(model, editPart);
      modelObjMap.put(editPart, model);
    }
    return editPart;
  }
  public EditPart createInnerEditPart(EditPart context, Object model) {

    EditPart child = null;

    // TODO Check what happens when we have sub-models !!!!
    if (model instanceof Director) {
      child = new DirectorEditPart();
    } else if (model instanceof TextAttribute) {
      child = new CommentEditPart();
    } else if (model instanceof Parameter) {
      child = new ParameterEditPart();
    } else if (model instanceof Vertex) {
      child = new VertexEditPart();
    } else if (model instanceof IOPort) {
      if (((IOPort) model).isInput())
        child = new PortEditPart(true);
      else
        child = new PortEditPart(false);
    } else if (model instanceof LinkWithBendPoints) {
      child = new LinkEditPart();
    } else if (model instanceof TypedCompositeActor) {
      // TODO Check if this is the correct check to make the distinction
      // between this and child Composites. Check also how to go more then
      // 1 level deeper
      if (((TypedCompositeActor) model).getContainer() == null) {
        child = new DiagramEditPart(parent, actor);
      } else {
        TypedCompositeActor composite = (TypedCompositeActor) model;
        String className = composite.getClassName();
        if (className.equals("ptolemy.actor.TypedCompositeActor")) {
          child = new CompositeActorEditPart(!(context != null && context.getParent() != null), parent);

        } else {
          child = new SubModelEditPart(!(context != null && context.getParent() != null), parent);

        }

      }
    } else if (model instanceof TypedAtomicActor) {

      try {
        child = getEditPartFromExtensionPoint(model);
      } catch (Exception e) {
        logger.error("Cannot load editorClass for " + model.getClass().getName(), e);
      }

      if (child == null) {
        try {
          IActorFigureProvider prov = getFigureProviderFromExtensionPoint(model);
          if (prov != null) {
            child = new CustomFigureEditPart(prov);
          }
        } catch (Exception e) {
          logger.error("Cannot load editorClass for " + model.getClass().getName(), e);
        }

        if (child == null) {
          if (model instanceof Source)
            child = new ActorSourceEditPart();
          else if (model instanceof Sink)
            child = new ActorSinkEditPart();
          else
            child = new ActorEditPart();
        }
      }
    }

    if (child != null) {
      child.setModel(model);
    } else {
      getLogger().error("Unable to create EditPart, requested model not supported");
    }
    if (child instanceof AbstractBaseEditPart) {
      parts.add((AbstractBaseEditPart) child);
    }
    return child;
  }

  private Map<String, IConfigurationElement> extensionParts;

  private void createExtensionParts() {
    if (extensionParts == null) {
      extensionParts = new HashMap<String, IConfigurationElement>(7);
      final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.engine.actors");
      for (int i = 0; i < elements.length; i++) {
        final String clazz = elements[i].getAttribute("class");
        final String editClass = elements[i].getAttribute("editorClass");
        final String figProv = elements[i].getAttribute("figureCustomizer");
        if (clazz != null && (editClass != null || figProv != null)) {
          extensionParts.put(clazz, elements[i]);
        }
      }
    }
  }

  private EditPart getEditPartFromExtensionPoint(Object model) throws CoreException {

    createExtensionParts();
    if (extensionParts.containsKey(model.getClass().getName())) {
      if (extensionParts.get(model.getClass().getName()).getAttribute("editorClass") == null)
        return null;
      return (EditPart) extensionParts.get(model.getClass().getName()).createExecutableExtension("editorClass");
    }
    return null;
  }

  private IActorFigureProvider getFigureProviderFromExtensionPoint(Object model) throws CoreException {

    createExtensionParts();
    if (extensionParts.containsKey(model.getClass().getName())) {
      if (extensionParts.get(model.getClass().getName()).getAttribute("figureCustomizer") == null)
        return null;
      return (IActorFigureProvider) extensionParts.get(model.getClass().getName()).createExecutableExtension("figureCustomizer");
    }
    return null;
  }

}
