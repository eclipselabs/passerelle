/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.actor.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.model.util.CollectingMomlParsingErrorHandler;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.model.util.CollectingMomlParsingErrorHandler.ErrorItem;
import com.isencia.passerelle.util.EnvironmentUtils;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

/**
 * A class that groups all services related to maintaining/modifying/... the actor library that is available in the IDE. REMARK : current implementation assumes
 * that a separate instance is created for separate threads that need to check/modify libraries (if this would ever occur).
 * 
 * @author erwin
 */
public class LibraryManager {

  public final static String USER_LIBRARY_NAME = "UserLibrary";
  public final static String ACTOR_LIBRARY_NAME = "actor library";

  private static final int ACTORS_LIBRARY_PREFIX_LENGTH = (".configuration." + ACTOR_LIBRARY_NAME + ".").length();
  private static final String SOURCE_PATH_LIB_ATTR_NAME = "_sourcePath";

  private final static Logger logger = LoggerFactory.getLogger(LibraryManager.class);

  private SortedMap<String, EntityLibrary> libraryMap;
  private SortedMap<String, EntityLibrary> userLibraryMap = new TreeMap<String, EntityLibrary>();
  private Configuration configuration;

  /**
   * Initializes potentially lots of cached info about the libraries contained in the given configuration.
   * 
   * @param configuration
   */
  public LibraryManager(Configuration configuration) {
    this.configuration = configuration;
    refreshManagerCache(configuration);
  }

  /**
   * UPdates the cached info
   * 
   * @param configuration
   */
  public void refreshManagerCache(Configuration configuration) {
    if (configuration != this.configuration) {
      if (configuration != null) {
        refreshActorLibraryMap(configuration);
        refreshUserLibraryMap(configuration);
      } else {
        if (libraryMap != null) {
          libraryMap.clear();
          libraryMap = null;
        }
        if (userLibraryMap != null) {
          userLibraryMap.clear();
          userLibraryMap = null;
        }
      }
      this.configuration = configuration;
    }
  }

  private void refreshUserLibraryMap(Configuration configuration) {
    if (userLibraryMap == null) {
      userLibraryMap = new TreeMap<String, EntityLibrary>();
    } else {
      userLibraryMap.clear();
    }
    CompositeEntity topLibrary = (CompositeEntity) configuration.getEntity("actor library");
    if (topLibrary != null) {
      EntityLibrary userLibrary = (EntityLibrary) topLibrary.getEntity(USER_LIBRARY_NAME);
      if (userLibrary != null) {
        // during start-up the userlibrary may not yet be in the configuration...
        userLibraryMap.put(USER_LIBRARY_NAME, userLibrary);
        List<EntityLibrary> libraries = userLibrary.entityList(EntityLibrary.class);
        CollectingMomlParsingErrorHandler errorHandler = new CollectingMomlParsingErrorHandler();

        // no filter needed on sourcepath attr or whatever
        // userlibrary sublibraries are maintained in the 1 single UserLibrary.xml file
        List<EntityLibrary> deepLibraries = getDeepLibrariesWithAttributes(libraries, errorHandler);
        for (EntityLibrary lib : deepLibraries) {
          String name = lib.getFullName().substring(ACTORS_LIBRARY_PREFIX_LENGTH);
          userLibraryMap.put(name, lib);
        }

        if (errorHandler.hasErrors()) {
          for (ErrorItem errorItem : errorHandler) {
            logger.error("Error populating library " + userLibrary.getFullName() + "\n\t" + errorItem.exception.getMessage());
          }

          Component parentWindow = GraphicalMessageHandler.getContext();
          Object[] options = { "Ok" };
          String[] messageArray = new String[] { "Some Library entries could not be constructed.", "Please consult the Passerelle log files for more details." };

          // Show the MODAL dialog
          int selected = JOptionPane.showOptionDialog(parentWindow, messageArray, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
              options[0]);
        }
      }
    }
  }

  private void refreshActorLibraryMap(Configuration configuration) {
    if (libraryMap == null) {
      libraryMap = new TreeMap<String, EntityLibrary>();
    } else {
      libraryMap.clear();
    }
    CompositeEntity topLibrary = (CompositeEntity) configuration.getEntity("actor library");
    if (topLibrary != null) {
      List<EntityLibrary> libraries = topLibrary.entityList(EntityLibrary.class);
      CollectingMomlParsingErrorHandler errorHandler = new CollectingMomlParsingErrorHandler();

      List<EntityLibrary> deepLibraries = getDeepLibrariesWithAttributes(libraries, errorHandler, SOURCE_PATH_LIB_ATTR_NAME);
      for (EntityLibrary lib : deepLibraries) {
        String name = lib.getFullName().substring(ACTORS_LIBRARY_PREFIX_LENGTH);
        libraryMap.put(name, lib);
      }

      if (errorHandler.hasErrors()) {
        for (ErrorItem errorItem : errorHandler) {
          logger.error("Error populating library " + topLibrary.getFullName() + "\n\t" + errorItem.exception.getMessage());
        }

        Component parentWindow = GraphicalMessageHandler.getContext();
        Object[] options = { "Ok" };
        String[] messageArray = new String[] { "Some Library entries could not be constructed.", "Please consult the Passerelle log files for more details." };

        // Show the MODAL dialog
        int selected = JOptionPane.showOptionDialog(parentWindow, messageArray, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
            options[0]);
      }

    }
  }

  /**
   * @return the names of all known actor libraries
   */
  public String[] getActorLibraryNames() {
    if (libraryMap == null) refreshActorLibraryMap(configuration);
    if (libraryMap == null) {
      // means the refresh failed
      return new String[0];
    } else {
      Set<String> libraryNames = libraryMap.keySet();
      if (userLibraryMap == null) refreshUserLibraryMap(configuration);
      if (userLibraryMap != null) libraryNames.addAll(userLibraryMap.keySet());
      return libraryNames.toArray(new String[0]);
    }
  }

  /**
   * @return the names of the user library and its sublibraries
   */
  public String[] getUserLibraryNames() {
    if (userLibraryMap == null || userLibraryMap.isEmpty()) refreshUserLibraryMap(configuration);
    if (userLibraryMap == null) {
      // means the refresh failed
      return new String[0];
    } else {
      Set<String> libraryNames = userLibraryMap.keySet();
      return libraryNames.toArray(new String[0]);
    }
  }

  public void addSubLibrary(final EntityLibrary library, String folderName) throws NameDuplicationException, IllegalActionException {
    EntityLibrary subLibrary = new EntityLibrary(library, folderName);
    StringWriter buffer = new StringWriter();
    try {
      subLibrary.exportMoML(buffer, 1);
    } catch (IOException e) {
      // ignore, will never happen for a StringWriter
    }
    subLibrary.setName(folderName);
    ChangeRequest request = new MoMLChangeRequest(this, library, buffer.toString()) {
      @Override
      public NamedObj getLocality() {
        return library;
      }
    };
    request.addChangeListener(new EntityLibraryChangedListener(this));
    library.requestChange(request);

  }

  public void renameLibrary(final EntityLibrary library, String newName) {
    String oldName = library.getName();
    StringBuffer moml = new StringBuffer("<");
    String elementName = library.getElementName();
    moml.append(elementName);
    moml.append(" name=\"");
    moml.append(oldName);
    moml.append("\">");
    if (!oldName.equals(newName)) {
      moml.append("<rename name=\"");
      moml.append(newName);
      moml.append("\"/>");
    }

    moml.append("</");
    moml.append(elementName);
    moml.append(">");

    NamedObj parent = library.getContainer();
    MoMLChangeRequest request = new MoMLChangeRequest(this, // originator
        parent, // context
        moml.toString(), // MoML code
        null) /* base */{
      @Override
      public NamedObj getLocality() {
        return library;
      }
    };

    request.addChangeListener(new EntityLibraryChangedListener(this));
    request.setUndoable(true);
    parent.requestChange(request);
  }

  public void saveChangedEntityLibrary(EntityLibrary library) {
    FileWriter w = null;
    FileParameter libSourceFileParameter = (FileParameter) library.getAttribute(SOURCE_PATH_LIB_ATTR_NAME);
    if (libSourceFileParameter != null) {
      try {
        URL libURL = libSourceFileParameter.asURL();
        w = new FileWriter(libURL.getFile());
        library.exportMoML(w, 0);
        if (logger.isDebugEnabled()) logger.debug("Saved modified library ");
      } catch (Exception e) {
        logger.error("Failed to save update in " + libSourceFileParameter, e);
        MessageHandler.error("Failed to save update in " + libSourceFileParameter, e);
      } finally {
        if (w != null) try {
          w.close();
        } catch (IOException e1) {
        }
      }
    } else if (library.getContainer() instanceof EntityLibrary) {
      // try to save the container (i.e. hope this library can be saved in
      // a moml of the container)
      saveChangedEntityLibrary((EntityLibrary) library.getContainer());
    } else {
      MessageHandler.error("Library update not supported for library " + library.getName() + "\n" + SOURCE_PATH_LIB_ATTR_NAME
          + " attribute missing in library cfg file.");
    }
  }

  public void saveEntityInUserLibrary(String libraryName, Entity entity) throws Exception {
    EntityLibrary library = (EntityLibrary) userLibraryMap.get(libraryName);
    if (library == null) {
      MessageHandler.error("Save In Library failed: " + "Could not find library with name \"" + libraryName + "\".");
      return;
    }
    saveEntityInLibrary(library, entity);
  }

  public void saveEntityInLibrary(EntityLibrary library, Entity entity) throws Exception {
    // Check whether there is already something existing in the
    // library with this name.
    if (library.getEntity(entity.getName()) != null) {
      throw new Exception("An object with name " + entity.getName() + " already exists in the library " + library.getName());
    }

    Entity entityAsClass = exportEntityToClassFile(entity);
    Entity instance = (Entity) entityAsClass.instantiate(library, entity.getName());
    instance.setClassName(entity.getName());

    StringWriter buffer = new StringWriter();
    try {
      instance.exportMoML(buffer, 1);
    } catch (IOException e) {
      // ignore, will never happen for a StringWriter
    }

    ChangeRequest request = new MoMLChangeRequest(instance, library, buffer.toString());
    request.addChangeListener(new EntityLibraryChangedListener(this));
    library.requestChange(request);
  }

  public void deleteEntityFromLibrary(final EntityLibrary library, final Entity entity) {
    // Check whether there is already something existing in the
    // library with this name.
    if (library.getEntity(entity.getName()) == null) {
      MessageHandler.error("Delete from Library failed: An object with name " + entity.getName() + " does not exist in the library " + library.getName());
      return;
    }

    ChangeRequest request = new MoMLChangeRequest(this, library, "<deleteEntity name=\"" + entity.getName() + "\"/>\n") {
      @Override
      public NamedObj getLocality() {
        return userLibraryMap.get(USER_LIBRARY_NAME);
      }
    };
    request.addChangeListener(new EntityLibraryChangedListener(this));
    library.requestChange(request);
  }

  /**
   * @param entity
   * @return the newly created class
   * @throws Exception
   */
  public Entity exportEntityToClassFile(Entity entity) throws Exception {
    Entity entityAsClass = (Entity) entity.clone(entity.workspace());
    entityAsClass.setClassDefinition(true);

    if (entityAsClass instanceof CompositeActor) {
      CompositeActor compActor = ((CompositeActor) entityAsClass);
      Director d = compActor.getDirector();
      if (d != null) {
        // remove the director from the class definition
        d.setContainer(null);
      }

      Attribute ctrlFact = compActor.getAttribute("_controllerFactory");
      if (ctrlFact == null) {
        new PasserelleActorControllerFactory(compActor, "_controllerFactory");
      } else if (!(ctrlFact instanceof PasserelleActorControllerFactory)) {
        ctrlFact.setContainer(null);
        new PasserelleActorControllerFactory(compActor, "_controllerFactory");
      }
      Attribute editorFact = compActor.getAttribute("_editorFactory");
      if (editorFact == null) {
        new PasserelleEditorFactory(compActor, "_editorFactory");
      } else if (!(editorFact instanceof PasserelleEditorFactory)) {
        editorFact.setContainer(null);
        new PasserelleEditorFactory(compActor, "_editorFactory");
      }
      Attribute editorPaneFact = compActor.getAttribute("_editorPaneFactory");
      if (editorPaneFact == null) {
        new PasserelleEditorPaneFactory(compActor, "_editorPaneFactory");
      } else if (!(editorPaneFact instanceof PasserelleEditorPaneFactory)) {
        editorPaneFact.setContainer(null);
        new PasserelleEditorPaneFactory(compActor, "_editorPaneFactory");
      }
    }
    File file = new File(EnvironmentUtils.getUserFolder() + File.separator + entityAsClass.getName() + ".moml");
    if (_confirmFile(file)) {
      logger.debug("Exporting actor {} to {}", entityAsClass.getFullName(), file.getPath());

      // Make sure the entity name saved matches the file name.
      String name = entityAsClass.getName();
      String filename = file.getName();
      int period = filename.indexOf(".");
      if (period > 0) {
        name = filename.substring(0, period);
      } else {
        name = filename;
      }

      FileWriter fileWriter = new FileWriter(file);
      try {
        if (entityAsClass.getContainer() != null) {
          // in this case the exportMoML below does not add the xml
          // header itself
          // if the entity is a top-level one,without container the
          // exportMoML does add it
          fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" + "<!DOCTYPE " + entityAsClass.getElementName() + " PUBLIC "
              + "\"-//UC Berkeley//DTD MoML 1//EN\"\n" + "    \"http://ptolemy.eecs.berkeley.edu" + "/xml/dtd/MoML_1.dtd\">\n");
        }
        entityAsClass.exportMoML(fileWriter, 0, name);
      } finally {
        fileWriter.close();
      }
    }

    return entityAsClass;
  }

  /**
   * @param entity
   * @return true if export was effectively done, false if the user aborted the operation
   * @throws IOException
   */
  public boolean exportEntityToFile(Entity entity) throws IOException {
    JFileChooser fileDialog = new JFileChooser();
    fileDialog.setDialogTitle("Save actor as...");
    fileDialog.setCurrentDirectory(EnvironmentUtils.getUserRelevantDirectory());
    fileDialog.setSelectedFile(new File(fileDialog.getCurrentDirectory(), entity.getName() + ".xml"));

    // Show the dialog.
    int returnVal = fileDialog.showSaveDialog(null);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fileDialog.getSelectedFile();
      if (!_confirmFile(file)) {
        return false;
      }
      // Record the selected directory.
      EnvironmentUtils.setLastSelectedDirectory(fileDialog.getCurrentDirectory());

      logger.debug("Exporting actor {} to {}", entity.getFullName(), file.getPath());

      FileWriter fileWriter = new FileWriter(file);

      // Make sure the entity name saved matches the file name.
      String name = entity.getName();
      String filename = file.getName();
      int period = filename.indexOf(".");
      if (period > 0) {
        name = filename.substring(0, period);
      } else {
        name = filename;
      }
      try {
        if (entity.getContainer() != null) {
          // in this case the exportMoML below does not add the xml
          // header itself
          // if the entity is a top-level one,without container the
          // exportMoML does add it
          fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n" + "<!DOCTYPE " + entity.getElementName() + " PUBLIC "
              + "\"-//UC Berkeley//DTD MoML 1//EN\"\n" + "    \"http://ptolemy.eecs.berkeley.edu" + "/xml/dtd/MoML_1.dtd\">\n");
        }
        entity.exportMoML(fileWriter, 0, name);
      } finally {
        fileWriter.close();
      }
      return true;
    } else {
      return false;
    }
  }

  private boolean _confirmFile(File file) {
    if (file.exists()) {
      // Ask for confirmation before overwriting a file.
      String query = "Overwrite " + file.getName() + "?";
      // Show a MODAL dialog
      int selected = JOptionPane.showOptionDialog(null, query, "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

      if (selected == 1) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param libraries the parent libraries in which we should look for sublibraries
   * @param attrNames filter, i.e. the method will only return the libraries that have attributes defined with the given names
   * @return
   */
  private List<EntityLibrary> getDeepLibrariesWithAttributes(List<EntityLibrary> libraries, CollectingMomlParsingErrorHandler errorHandler, String... attrNames) {
    List<EntityLibrary> deepLibraries = new ArrayList<EntityLibrary>();
    for (EntityLibrary library : libraries) {
      // libraries are lazy loaders, so we need to call populate()
      // explicitly here!
      ErrorHandler prevErrHandler = MoMLParser.getErrorHandler();
      try {
        // ensure no error pop-ups with annoying stack traces
        // are happening while populating the libraries
        // remark that as a result, a library containing entity
        // definitions
        // that fail to load, will after this saving operation no longer
        // contain these failed ones!
        MoMLParser.setErrorHandler(errorHandler);
        library.populate();
      } catch (Exception e) {
        // catch any problems during populating
        logger.error("Error populating library " + library.getFullName() + " : " + e.getMessage());
      } finally {
        MoMLParser.setErrorHandler(prevErrHandler);
      }
      List<EntityLibrary> sublibs = library.entityList(EntityLibrary.class);
      if (sublibs != null && sublibs.size() > 0) {
        deepLibraries.addAll(getDeepLibrariesWithAttributes(sublibs, errorHandler, attrNames));
      }
      // also show intermediate levels if they have the right attributes
      boolean accepted = true;
      for (String attrName : attrNames) {
        if (library.getAttribute(attrName) == null) {
          accepted = false;
          break;
        }
      }
      if (accepted) {
        deepLibraries.add(library);
      }
    }

    return deepLibraries;
  }

  public static class EntityLibraryChangedListener implements ChangeListener {
    private LibraryManager libraryManager;

    public EntityLibraryChangedListener(LibraryManager libraryManager) {
      this.libraryManager = libraryManager;
    }

    public void changeExecuted(ChangeRequest change) {
      EntityLibrary library = (EntityLibrary) ((MoMLChangeRequest) change).getContext();

      libraryManager.saveChangedEntityLibrary(library);
    }

    public void changeFailed(ChangeRequest change, Exception exception) {
      // TODO Auto-generated method stub

    }
  }
}
