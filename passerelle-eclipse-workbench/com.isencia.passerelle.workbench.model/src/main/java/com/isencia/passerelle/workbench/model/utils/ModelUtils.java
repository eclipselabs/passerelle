package com.isencia.passerelle.workbench.model.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.digester.substitution.MultiVariableExpander;
import org.apache.commons.digester.substitution.VariableSubstitutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.eclipse.resources.util.ResourceUtils;
import com.isencia.passerelle.editor.common.utils.EditorUtils;

public class ModelUtils {

  public static Logger logger = LoggerFactory.getLogger(ModelUtils.class);
  private static PreferenceStore store;

  public static PreferenceStore getFavouritesStore() throws Exception {
    if (store == null) {
      store = new PreferenceStore();
      final IProject pass = getPasserelleProject();
      store.setFilename(pass.getLocation().toOSString() + "/favorites.properties");
      try {
        store.load();
      } catch (IOException e) {
        return store;
      }
    }
    return store;
  }

  public static enum ConnectionType {
    SOURCE, TARGET
  };

  public static boolean isClassDefinition(Object model) {
    if (model instanceof CompositeActor) {
      return ((CompositeActor) model).isClassDefinition();
    }
    return false;
  }

  public static List<IOPort> getPorts(Relation relation, NamedObj obj) {
    List<IOPort> ports = new ArrayList<IOPort>();
    for (Object o : relation.linkedPortList()) {
      if (((Port) o).getContainer().equals(obj)) {
        ports.add((IOPort) o);

      }
    }
    return ports;
  }

  public static Set<Relation> getConnectedRelations(Nameable model, ConnectionType connectionType, boolean fullList) {

    Set<Relation> connections = new HashSet<Relation>();
    if (model.getContainer() == null || !(model.getContainer() instanceof CompositeEntity))
      return Collections.EMPTY_SET;
    CompositeEntity composite = (CompositeEntity) model.getContainer();
    List<Relation> relationList = new ArrayList<Relation>();
    relationList.addAll(composite.relationList());
    if (model instanceof TypedIOPort) {
      if (fullList && composite != null && composite.getContainer() != null) {
        relationList.addAll(((CompositeEntity) composite.getContainer()).relationList());
      }
    }
    if (relationList == null || relationList.size() == 0)
      return Collections.EMPTY_SET;

    for (Relation relation : relationList) {
      List linkedObjectsList = relation.linkedObjectsList();
      if (linkedObjectsList == null || linkedObjectsList.size() == 0)
        continue;
      for (Object o : linkedObjectsList) {
        if (o instanceof Port) {
          Port port = (Port) o;
          if (port.getContainer().equals(model) || (model instanceof IOPort && (port.equals(((IOPort) model))))) {
            if (connectionType.equals(ConnectionType.SOURCE)) {
              if (port instanceof IOPort && (!(model instanceof IOPort) && ((IOPort) port).isOutput()) || ((model instanceof IOPort) && ((IOPort) port).isInput()))
                connections.add(relation);
            } else {
              if (port instanceof IOPort && (!(model instanceof IOPort) && ((IOPort) port).isInput()) || ((model instanceof IOPort) && ((IOPort) port).isOutput()))
                connections.add(relation);
            }
          }
        }
      }

    }
    return connections;
  }

  public static List<Relation> getRelations(NamedObj model) {
    ArrayList<Relation> relations = new ArrayList<Relation>();
    if (model == null || !(model instanceof CompositeEntity))
      return Collections.EMPTY_LIST;
    List<Relation> relationList = ((CompositeEntity) model).relationList();
    return relationList;
  }

  @SuppressWarnings("unchecked")
  public static double[] getLocation(NamedObj model) {
    if (model instanceof Locatable) {
      Locatable locationAttribute = (Locatable) model;
      return locationAttribute.getLocation();
    }
    List<Attribute> attributes = model.attributeList(Locatable.class);
    if (attributes == null || attributes.size() == 0) {
      return new double[] { 0.0D, 0.0D };
    }
    Locatable locationAttribute = (Locatable) attributes.get(0);
    return locationAttribute.getLocation();
  }

  @SuppressWarnings("unchecked")
  public static void setLocation(NamedObj model, double[] location) {
    if (model instanceof Locatable) {
      try {
        ((Locatable) model).setLocation(location);
        NamedObj cont = model.getContainer();
        cont.attributeChanged((Attribute) model);
      } catch (IllegalActionException e) {
        // TODO Auto-generated catch block
        logger.error("Unable to change location of component", e);
      }

    }
    List<Attribute> attributes = model.attributeList(Locatable.class);
    if (attributes == null)
      return;
    if (attributes.size() > 0) {
      Locatable locationAttribute = (Locatable) attributes.get(0);
      try {
        locationAttribute.setLocation(location);
        model.attributeChanged(attributes.get(0));
      } catch (IllegalActionException e) {
        logger.error("Unable to change location of component", e);
      }
    } else {
      try {
        new Location(model, "_location").setLocation(location);
      } catch (IllegalActionException e) {
        logger.error("Unable to change location of component", e);
      } catch (NameDuplicationException e) {
        logger.error("Duplicate name encountered during change location of component", e);
      }
    }
  }

  public static boolean isPortOfActor(IOPort port, Actor actor) {
    for (Object o : actor.inputPortList()) {
      if (o == port) {
        return true;
      }
    }
    for (Object o : actor.outputPortList()) {
      if (o == port) {
        return true;
      }
    }
    return false;
  }

  public static String findUniqueActorName(CompositeEntity parentModel, String name) {
    String newName = name;
    if (parentModel == null)
      return newName;
    List entityList = parentModel.entityList();
    if (entityList == null || entityList.size() == 0)
      return newName;

    ComponentEntity entity = parentModel.getEntity(newName);
    int i = 1;
    while (entity != null) {
      newName = name + "(" + i++ + ")";
      entity = parentModel.getEntity(newName);
    }

    return newName;
  }

  /**
   * Attempts to clean up names in the event that they are not compatible
   * 
   * @param newName
   * @return
   */
  public static String getLegalName(final String name) {
    String newName = name;
    // newName = newName.replace(' ', '_');
    if (name.indexOf('.') > -1) {
      newName = newName.substring(0, newName.lastIndexOf('.'));
    }
    newName = newName.replace('.', '_');
    return newName;
  }

  public static boolean isNameLegal(String text) {
    if (text == null)
      return false;
    if ("".equals(text))
      return false;
    if (text.indexOf('.') > -1)
      return false;
    return true;
  }

  public static String findUniqueName(CompositeEntity parentModel, Class clazz, String startName, String actorName) {
    return EditorUtils.findUniqueName(parentModel, clazz, startName, actorName);
  }

  /**
   * Currently does not do much as toplevel.getEntity(...) does this search.
   * 
   * @param toplevel
   * @param actorName
   * @return
   */
  public static ComponentEntity findEntityByName(CompositeActor toplevel, String actorName) {

    ComponentEntity entity = toplevel.getEntity(actorName);
    if (entity != null)
      return entity;

    return null;
  }

  public static IFile getProjectFile(final String modelPath) {

	    return ResourceUtils.getProjectFile(modelPath);

  }

  /**
   * Attempts to find the project from the top CompositeActor by using the workspace name.
   * 
   * @param actor
   * @return
   * @throws Exception
   */
  public static IProject getProject(final NamedObj actor) throws Exception {

	  return ResourceUtils.getProject(actor);
  }

  /**
   * substutes variables in the string as determined from the actor.
   * 
   * @param filePath
   * @param dataExportTransformer
   * @throws Exception
   */
  public static String substitute(final String sub, final NamedObj actor) throws Exception {

    final Map<String, Object> variables = new HashMap<String, Object>(3);
    variables.put("project_name", getProject(actor).getName());
    variables.put("actor_name", actor.getName());

    MultiVariableExpander expander = new MultiVariableExpander();
    expander.addSource("$", variables);
    // Create a substitutor with the expander
    VariableSubstitutor substitutor = new VariableSubstitutor(expander);

    return substitutor.substitute(sub);
  }

  /**
   * We will initialize file contents with a sample text.
   */

  public static InputStream getEmptyWorkflowStream(final String fileName) {
    String contents = "<?xml version=\"1.0\" standalone=\"no\"?> \r\n"
        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\"> \r\n" + "<entity name=\""
        + fileName.substring(0, fileName.length() - 5) + "\" class=\"ptolemy.actor.TypedCompositeActor\"> \r\n"
        + "   <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"7.0.1.4\" /> \r\n"
        + "   <property name=\"_workbenchVersion\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"" + System.getProperty("passerelle.workbench.version")
        + "\" /> \r\n" + "   <property name=\"Director\" class=\"com.isencia.passerelle.domain.cap.Director\" > \r\n"
        + "      <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{20, 20}\" /> \r\n" + "   </property> \r\n" + "</entity>";
    return new ByteArrayInputStream(contents.getBytes());
  }

  public static InputStream getEmptyCompositeStream(String name) throws UnsupportedEncodingException {
    String momlTemplate = "<?xml version=\"1.0\" standalone=\"no\"?> \r\n"
        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\"> \r\n"
        + "<class  name=\"$modelName\" extends=\"ptolemy.actor.TypedCompositeActor\"> \r\n </class >";
    momlTemplate = momlTemplate.replace("$modelName", name);

    return new ByteArrayInputStream(momlTemplate.getBytes("UTF-8"));
  }

  public static IProject getPasserelleProject() throws Exception {

    return ResourceUtils.getPasserelleProject();
  }

  /**
   * @param modelPath
   * @return
   */
  public static Workspace getWorkspace(final String modelPath) {

    IFile projFile = ModelUtils.getProjectFile(modelPath);
    if (projFile != null) {
      logger.debug("Running project file {}", projFile);
      return new Workspace(projFile.getProject().getName());
    }

    final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();

    // We must tell the composite actor the containing project name
    String relPath = modelPath.substring(workspacePath.length());
    projFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
    if (projFile != null) {
      logger.debug("Running project file {}", projFile);
      return new Workspace(projFile.getProject().getName());
    }
    String fileSep = System.getProperty("file.separator");
    if (relPath.startsWith(fileSep)) relPath = relPath.substring(1);
    final String projectName = relPath.substring(0, relPath.indexOf(fileSep));

    logger.debug("Using project {}", projectName);
    return new Workspace(projectName);

  }
}
