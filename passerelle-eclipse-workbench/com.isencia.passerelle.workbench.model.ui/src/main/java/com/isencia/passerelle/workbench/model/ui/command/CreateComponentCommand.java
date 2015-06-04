package com.isencia.passerelle.workbench.model.ui.command;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.ui.internal.util.ErrorUtil;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.editor.common.utils.EditorUtils;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateComponentCommand extends org.eclipse.gef.commands.Command {

  public static final String DEFAULT_OUTPUT_PORT = "OutputPort";

  public static final String DEFAULT_INPUT_PORT = "InputPort";

  private static Logger logger = LoggerFactory.getLogger(CreateComponentCommand.class);

  private Class<? extends NamedObj> clazz;

  private String name;

  private Flow flow;

  private NamedObj parent;

  private NamedObj child;

  private IEditorPart editor;
  
  private TypedIORelation relation;

  public Flow getFlow() {
    return flow;
  }

  public void setFlow(Flow flow) {
    this.flow = flow;
  }

  private double[] location;

  public NamedObj getChild() {
    return child;
  }

  public CreateComponentCommand(IEditorPart editor, NamedObj parent) {
    super("CreateComponent");
    this.editor = editor;
    this.parent = parent;
  }

  public CreateComponentCommand(Class<? extends NamedObj> clazz, String name, NamedObj parent, double[] location,TypedIORelation relation) {
    this.parent = parent;
    this.clazz = clazz;
    this.name = name;
    this.location = location;
    this.relation= relation;
  }

  public Logger getLogger() {
    return logger;
  }

  public boolean canExecute() {
    if (("com.isencia.passerelle.actor.general.InputIOPort".equals(clazz) || "com.isencia.passerelle.actor.general.OutputIOPort".equals(clazz)) && parent != null
        && parent.getContainer() == null) {
      return false;
    }
    return (clazz != null && parent != null);
  }

  public void execute() {
    try {
      doExecute();
    } catch (Exception e) {
      logger.error("Unable to create component", e);
      EclipseUtils.logError(e, "Unable to create component", IStatus.ERROR);
      ErrorUtil.displayErrorDialog(e.getMessage());
    }
  }

  public void doExecute() throws Exception {
    if (clazz != null && Director.class.isAssignableFrom(clazz)) {
      if (parent instanceof CompositeActor && ((CompositeActor) parent).getDirector() != null) {
        throw new Exception("Multiple directors are not allowed, please remove first the current director");

      }
    }
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
      @Override
      protected void _execute() throws Exception {
        try {
          CompositeEntity parentModel = (CompositeEntity) parent;
          
          String componentName = EditorUtils.findUniqueName(parentModel, clazz, name, name);

          if (Vertex.class.isAssignableFrom(clazz)) {
            if (relation == null){
              relation = new TypedIORelation(parentModel, componentName);
            }
            child = new Vertex(relation, "Vertex");
          } else if (Flow.class.isAssignableFrom(clazz)) {
            if (flow != null) {
              child = (NamedObj) flow.instantiate(parentModel, componentName);
              ((CompositeActor) child).setClassName(flow.getName());
            }
          } else {
            Class constructorClazz = CompositeEntity.class;
            if (ComponentPort.class.isAssignableFrom(clazz)) {
              constructorClazz = ComponentEntity.class;
            } else if (!Entity.class.isAssignableFrom(clazz) && !Director.class.isAssignableFrom(clazz)) {
              constructorClazz = NamedObj.class;
            }
            Constructor constructor = clazz.getConstructor(constructorClazz, String.class);

            child = (NamedObj) constructor.newInstance(parentModel, componentName);
            if (child instanceof TypedIOPort) {
              boolean isInput = name.equalsIgnoreCase("INPUT") || clazz.getName().toLowerCase().endsWith(".input");
              ((TypedIOPort) child).setInput(isInput);
              ((TypedIOPort) child).setOutput(!isInput);
            }
          }

          createDefaultValues(child);

          if (location != null) {
            ModelUtils.setLocation(child, location);
          }
          setChild(child);

        } catch (Exception e) {

          throw e;
        }

      }

    });
  }

  public void redo() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
      @Override
      protected void _execute() throws Exception {
        if (child instanceof CompositeActor && editor != null && editor instanceof IPasserelleMultiPageEditor)
          ((IPasserelleMultiPageEditor)editor).selectPage((CompositeActor) parent);
        if (child instanceof NamedObj) {
          EditorUtils.setContainer(child, parent);

        }

      }
    });
  }

  public void setClazz(Class<? extends NamedObj> clazz) {
    this.clazz = clazz;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setParent(NamedObj newParent) {
    parent = newParent;
  }

  public void undo() {
    // Perform Change in a ChangeRequest so that all Listeners are notified
    parent.requestChange(new ModelChangeRequest(this.getClass(), parent, "create") {
      @Override
      protected void _execute() throws Exception {
        if (child instanceof CompositeActor && editor != null && editor instanceof IPasserelleMultiPageEditor)
          ((IPasserelleMultiPageEditor)editor).selectPage((CompositeActor) parent);
        if (child instanceof NamedObj) {
          EditorUtils.setContainer(child, null);
        }
      }
    });
  }

  public double[] getLocation() {
    return location;
  }

  public void setLocation(double[] location) {
    this.location = location;
  }

  private Map<Object, Object> defaultValueMap;

  /**
   * This method can be used to default configurable parameter values, when the class is created.
   * 
   * @param clazz
   * @param filePath
   */
  public void addConfigurableParameterValue(final Object clazzOrString, final Object value) {

    if (defaultValueMap == null)
      defaultValueMap = new LinkedHashMap<Object, Object>(3);
    defaultValueMap.put(clazzOrString, value);
  }

  private void createDefaultValues(NamedObj child) throws Exception {

    if (defaultValueMap == null)
      return;
    if (child instanceof Actor) {
      final Actor actor = (Actor) child;

      for (Object key : defaultValueMap.keySet()) {
        Parameter param = null;
        if (key instanceof Class) {
          final Collection<? extends Parameter> params = actor.getConfigurableParameter((Class<? extends Parameter>) key);
          param = (params != null && !params.isEmpty() && params.size() == 1) ? params.iterator().next() : null;
        } else if (key instanceof String) {
          param = actor.getConfigurableParameter((String) key);
        }
        if (param == null)
          continue;

        final Object value = defaultValueMap.get(key);
        if (value instanceof Boolean) {
          param.setToken(new BooleanToken((Boolean) value));
        } else if (value instanceof String) {
          param.setExpression((String) value);
        }
      }
    }

  }
}
