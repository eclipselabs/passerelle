/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import com.isencia.constants.IPropertyNames;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.ext.FiringEventListener;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.ext.impl.DefaultExecutionTracer;
import com.isencia.passerelle.hmi.action.ActorLibraryReloader;
import com.isencia.passerelle.hmi.action.AskTheUserErrorControlStrategy;
import com.isencia.passerelle.hmi.action.CloseAction;
import com.isencia.passerelle.hmi.action.CloseAllAction;
import com.isencia.passerelle.hmi.action.ModelCreator;
import com.isencia.passerelle.hmi.action.ModelDebugStepper;
import com.isencia.passerelle.hmi.action.ModelDebugger;
import com.isencia.passerelle.hmi.action.ModelExecutor;
import com.isencia.passerelle.hmi.action.ModelPrinter;
import com.isencia.passerelle.hmi.action.ModelResumer;
import com.isencia.passerelle.hmi.action.ModelStopper;
import com.isencia.passerelle.hmi.action.ModelSuspender;
import com.isencia.passerelle.hmi.action.OpenAction;
import com.isencia.passerelle.hmi.action.SaveAction;
import com.isencia.passerelle.hmi.action.SaveAllAction;
import com.isencia.passerelle.hmi.action.SaveAsAction;
import com.isencia.passerelle.hmi.binding.ParameterChangeListener;
import com.isencia.passerelle.hmi.definition.FieldMapping;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.definition.ModelBundle;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.trace.HMIExecutionTracer;
import com.isencia.passerelle.hmi.trace.TraceDialog;
import com.isencia.passerelle.hmi.trace.TraceVisualizer;
import com.isencia.passerelle.hmi.util.DynamicStepExecutionControlStrategy;
import com.isencia.passerelle.model.ExecutionTraceRecord;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowHandle;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.passerelle.util.ExecutionTracerService;

public abstract class HMIBase implements ChangeListener {

  private final static Logger logger = LoggerFactory.getLogger(HMIBase.class);

  private static final int RECENTMODELS_LIMIT_MINVALUE = 3;

  public static final String HMI_APPLICATIONNAME_PROPNAME = "hmi.applicationName";
  public static final String HMI_APPLICATIONNAME_DEFAULT = "Passerelle";

  public static final String HMI_INI_FILE_PROPNAME = "hmi.iniFile";
  public static final String HMI_MODEL_URL_PROPNAME = "hmi.modelURL";
  public static final String HMI_RECENTMODELS_FILE_PROPNAME = "hmi.recentModels.file";
  public static final String HMI_RECENTMODELS_LIMIT_PROPNAME = "hmi.recentModels.limit";
  public static final String HMI_RECENTMODELS_PATH_PROPNAME = "hmi.recentModels.path";
  public static final String HMI_RECENTMODELS_FILE_DEFAULT = "hmi_userdef.xml";
  public static final String HMI_DIAGNOSIS_DATA_BASE_DIR_PROPNAME = "hmi.diagnosis.data.base.dir";

  public static String HMI_APPLICATIONNAME = System.getProperty(HMI_APPLICATIONNAME_PROPNAME, HMI_APPLICATIONNAME_DEFAULT);

  public static final String INIFILE_PATH_STRING = System.getProperty(HMI_INI_FILE_PROPNAME, "hmi.ini");
  public static String MODELS_URL_STRING = System.getProperty(HMI_MODEL_URL_PROPNAME);
  public static String HMI_RECENTMODELS_FILE_STRING = null;

  public static String DIAGNOSIS_DATA_BASE_DIR_STRING;
  public static String DIAGNOSIS_ENTITIES_DUMP_PATHNAME;
  public static String DIAGNOSIS_ASSET_REPOS_PATHNAME;
  public static String DIAGNOSIS_SUBMODELS_ASSET_REPOS_PATHNAME;

  public static final String EXECUTION_CONTROL_ATTR_NAME = "_executionControl";
  public static final String USER_TRACER_ATTR_NAME = "_userTracer";

  // accept all models and try to automatically build a UI
  public final static Mode GENERIC = new Mode();
  // validate models if they fit within the list
  // of supported model types
  // and show a custom UI for them
  public final static Mode SPECIFIC = new Mode();

  private final Mode mode;

  private boolean interactiveErrorControl = false;
  private boolean animateModelExecution = false;
  private AnimationEventListener animationEventListener;

  private Configuration config;

  private final Map<URI, Flow> loadedModels = new ConcurrentHashMap<URI, Flow>();
  private final Map<URI, Boolean> modelsChangedStatus = new ConcurrentHashMap<URI, Boolean>();

  // this is constructed when a model is selected
  private Flow currentModel;
  private Model currentModelDef;

  // this represents the path to be used to open/save the model
  private URL modelURL;

  private ExecutionTracer executionTracer;
  private TraceVisualizer traceComponent;
  private RemoteExecutionTracePoller remoteExecutionTracePoller;

  protected final boolean showModelForms;
  protected final boolean showModelGraph;
  protected ModelGraphPanel graphPanel;
  protected PtolemyEffigy graphPanelEffigy;

  protected Map<String, ParameterToWidgetBinder> hmiFields = new HashMap<String, ParameterToWidgetBinder>();
  // this is the hmi config info parsed from some cfg file
  // current implementation uses XMLStreamer files
  private ModelBundle hmiModelsDef;

  private FlowManager flowManager = new FlowManager();

  private Action modelCreatorAction;
  private SaveAction saveAction;
  private SaveAsAction saveAsAction;
  private SaveAllAction saveAllAction;

  private OpenAction openAction;
  private ActorLibraryReloader actorLibReloadAction;
  private ModelPrinter printAction;
  private CloseAction closeAction;
  private CloseAllAction closeAllAction;
  private ModelDebugger modelDebugger;
  private ModelDebugStepper modelDebugStepper;
  // some actions that are shared between menu items and toolbar buttons
  private ModelExecutor modelExecutor;
  private ModelResumer modelResumer;
  private ModelStopper modelStopper;
  private ModelSuspender modelSuspender;

  protected JMenu modelsSubMenu;
  private boolean changeImpactEnabled = true;

  /**
   * @param mode
   * @param hmiModelsDef
   * @param showModelGraph
   */
  // public HMIBase(final Mode mode, final ModelBundle hmiModelsDef, final boolean showModelGraph) {
  // this.mode = mode;
  // this.hmiModelsDef = hmiModelsDef;
  // this.showModelGraph = showModelGraph;
  // }

  /**
   * @param mode
   * @param cfgDefPath
   * @param showModelGraph
   */
  public HMIBase(final Mode mode, final boolean showModelForms, final boolean showModelGraph) {
    this.mode = mode;
    this.showModelForms = showModelForms;
    this.showModelGraph = showModelGraph;
  }

  public void init() {
    // this stuff is used to maintain user prefs like "show parameters" and "show port names"
    StringUtilities.PREFERENCES_DIRECTORY = ".passerelle";
    PtolemyPreferences.PREFERENCES_FILE_NAME = "PasserelleHmiPreferences.xml";

    // this ensures that from the 1st loaded model,
    // the user gets pop-ups for any missing actor classes in the HMI installation,
    // and can still choose to see whatever can be rendered of the model
    MoMLParser.setErrorHandler(new HMIErrorHandler(this));

    File iniPropsFile = new File(INIFILE_PATH_STRING);
    if (iniPropsFile.exists()) {
      InputStream propsInput = null;
      try {
        propsInput = new FileInputStream(iniPropsFile);
        System.getProperties().load(propsInput);
        logger.info("HMI configured from " + iniPropsFile.getCanonicalPath());
      } catch (Exception e) {
        logger.error("Error setting initialisation properties", e);
      } finally {
        if (propsInput != null)
          try {
            propsInput.close();
          } catch (Exception e) {
          }
      }
    } else {
      // do nothing, we assume all cfg settings have been set as system props then
      logger.info("HMI did not find config file");
    }
    HMI_APPLICATIONNAME = System.getProperty(HMI_APPLICATIONNAME_PROPNAME, HMI_APPLICATIONNAME_DEFAULT);
    MODELS_URL_STRING = System.getProperty(HMI_MODEL_URL_PROPNAME);
    String recentModelsProp = System.getProperty(HMI_RECENTMODELS_FILE_PROPNAME, HMI_RECENTMODELS_FILE_DEFAULT);
    String recentModelsPath = System.getProperty(HMI_RECENTMODELS_PATH_PROPNAME);
    if (recentModelsPath != null && recentModelsPath.length() > 0) {
      HMI_RECENTMODELS_FILE_STRING = recentModelsPath + "/" + recentModelsProp;
    } else {
      HMI_RECENTMODELS_FILE_STRING = EnvironmentUtils.getUserFolder() + "/" + recentModelsProp;
    }

    String recentModelsLimitStr = System.getProperty(HMI_RECENTMODELS_LIMIT_PROPNAME, "10");
    int recentModelsLimit = 10;
    try {
      recentModelsLimit = Integer.parseInt(recentModelsLimitStr);
      if (recentModelsLimit < RECENTMODELS_LIMIT_MINVALUE)
        recentModelsLimit = RECENTMODELS_LIMIT_MINVALUE;
    } catch (NumberFormatException e) {
      logger.warn("Invalid number format for " + HMI_RECENTMODELS_LIMIT_PROPNAME);
    }
    // Create a model bundle that contains models from a Passerelle Mgr server,
    // when the HMI refers to a remote model URL. This is used to populate the File > Models menu.
    // For a HMI with a local model URL, this model URL is only used to indicate the "working directory",
    // i.e. used to initially open/create models.
    // The File > Models menu is then built from the list of recently opened models,
    // which is maintained in the field behind HMI_RECENTMODELS_FILE_STRING.
    try {
      if (HMIBase.MODELS_URL_STRING != null && !HMIBase.MODELS_URL_STRING.startsWith("file")) {
        URL modelURL = new URL(HMIBase.MODELS_URL_STRING);
        // a bundle that lists all models of a directory
        hmiModelsDef = new ModelBundle();
        final FieldMapping m1FieldBundle = new FieldMapping();
        Collection<FlowHandle> flowsFromResourceLocation = FlowManager.getFlowsFromResourceLocation(modelURL);
        if (flowsFromResourceLocation != null && !flowsFromResourceLocation.isEmpty()) {
          for (final FlowHandle flowHandle : flowsFromResourceLocation) {
            final Model m1 = new Model(flowHandle.getAuthorativeResourceLocation(), m1FieldBundle);
            hmiModelsDef.setPredefinedModel(flowHandle.getName(), m1);
          }
        }
      } else if (HMIBase.HMI_RECENTMODELS_FILE_STRING == null) {
        hmiModelsDef = new ModelBundle();
      } else {
        File modelURLasFile = new File(HMIBase.HMI_RECENTMODELS_FILE_STRING);
        if (modelURLasFile.isFile())
          hmiModelsDef = ModelBundle.parseModelBundleDefFile(HMIBase.HMI_RECENTMODELS_FILE_STRING);
        else
          hmiModelsDef = new ModelBundle();
      }
    } catch (Exception e) {
      logger.error("Error reading models from " + HMIBase.MODELS_URL_STRING, e);
    }
    hmiModelsDef.setRecentModelsLimit(recentModelsLimit);

    // read actor configuration file for actor palette etc
    // mainly important for graphical editor
    String configurationFile = null;
    try {
      configurationFile = System.getProperty(IPropertyNames.APP_HOME, IPropertyNames.APP_HOME_DEFAULT) + java.io.File.separatorChar
          + System.getProperty("com.isencia.config", "conf") + java.io.File.separatorChar + "passerelle-actor.xml";
      System.out.println("readconfiguration file " + configurationFile);
      logger.debug("readconfiguration file " + configurationFile);
      config = readConfiguration(configurationFile);
      PtolemyPreferences.setDefaultPreferences(config);
    } catch (final Exception e) {
      logger.error("readconfiguration failed ", e);
    }

    traceComponent = createTraceComponent();
    initUI(HMIMessages.getString(HMIMessages.TITLE));
    if (executionTracer == null) {
      executionTracer = new HMIExecutionTracer(traceComponent);
    }
    ExecutionTracerService.registerTracer(executionTracer);
    // also save trace msgs in separate log file
    ExecutionTracerService.registerTracer(new DefaultExecutionTracer());

    // set file-based asset repo for HMI executions of diagnostic seqs
    // DIAGNOSIS_DATA_BASE_DIR_STRING = System.getProperty(HMI_DIAGNOSIS_DATA_BASE_DIR_PROPNAME, "C:/temp");
    // DIAGNOSIS_ENTITIES_DUMP_PATHNAME = DIAGNOSIS_DATA_BASE_DIR_STRING + "/passerelle-diagnosis-results";
    // DIAGNOSIS_ASSET_REPOS_PATHNAME = DIAGNOSIS_DATA_BASE_DIR_STRING + "/passerelle-repository";
    // DIAGNOSIS_SUBMODELS_ASSET_REPOS_PATHNAME = DIAGNOSIS_DATA_BASE_DIR_STRING + "/passerelle-repository-submodels";

    // File diagAssetReposPath = new File(DIAGNOSIS_ASSET_REPOS_PATHNAME);
    // File diagSubmodelReposPath = new File(DIAGNOSIS_SUBMODELS_ASSET_REPOS_PATHNAME);
    // File diagEntitiesDumpPath = new File(DIAGNOSIS_ENTITIES_DUMP_PATHNAME);
    // if (!diagAssetReposPath.exists() || !diagEntitiesDumpPath.exists()) {
    // logger.warn("Paths for diagnostic asset repository and/or result dumps do not exist. :" + "\n\tasset repos :" + DIAGNOSIS_ASSET_REPOS_PATHNAME
    // + "\n\tresults dump :" + DIAGNOSIS_ENTITIES_DUMP_PATHNAME + "\n\tDiagnostic processes will not work!");
    // }
    // ServicesRegistry.getInstance().setRepositoryService(new FileSystemBasedRepositoryService(diagAssetReposPath, diagSubmodelReposPath));
    // ServicesRegistry.getInstance().setRepositoryService(new FileSystemBasedRepositoryService(diagAssetReposPath));
    // ServicesRegistry.getInstance().setDiagnosisEntityFactory(new EntityFactory());
    // ServicesRegistry.getInstance().setDiagnosisEntityManager(new EntityManager(diagEntitiesDumpPath));

    // ServiceRegistry.getInstance().setEntityFactory(new EntityFactoryImpl());
    // EntityManagerImpl entityManager = new EntityManagerImpl();
    // ServiceRegistry.getInstance().setEntityManager(entityManager);
  }

  /**
   * Given the name of a file or a URL, convert it to a URL. This first attempts to do that directly by invoking a URL constructor. If that fails, then it tries
   * to interpret the spec as a file name on the local file system. If that fails, then it tries to interpret the spec as a resource accessible to the
   * classloader, which uses the classpath to find the resource. If that fails, then it throws an exception. The specification can give a file name relative to
   * current working directory, or the directory in which this application is started up.
   * 
   * @param spec
   *          The specification.
   * @exception IOException
   *              If it cannot convert the specification to a URL.
   */
  public static URL specToURL(final String spec) throws IOException {
    try {
      // First argument is null because we are only
      // processing absolute URLs this way. Relative
      // URLs are opened as ordinary files.
      return new URL(null, spec);
    } catch (final MalformedURLException ex) {
      try {
        final File file = new File(spec);
        if (!file.exists()) {
          throw new MalformedURLException();
        }
        return file.getCanonicalFile().toURL();
      } catch (final MalformedURLException ex2) {
        try {
          // Try one last thing, using the classpath.
          // Need a class context, and this is a static method, so...
          // NOTE: There doesn't seem to be any way to convert
          // this a canonical name, so if a model is opened this
          // way, and then later opened as a file, the model
          // directory will think it has two different files.
          final Class refClass = Class.forName("ptolemy.kernel.util.NamedObj");
          final URL inurl = refClass.getClassLoader().getResource(spec);
          if (inurl == null) {
            throw new Exception();
          } else {
            return inurl;
          }
        } catch (final Exception exception) {
          throw new IOException("File not found: " + spec);
        }
      }
    }
  }

  /**
	 *
	 */
  protected void applyFieldValuesToParameters() {
    final Iterator fieldsItr = hmiFields.values().iterator();
    while (fieldsItr.hasNext()) {
      final ParameterToWidgetBinder pwb = (ParameterToWidgetBinder) fieldsItr.next();
      pwb.fillParameterFromWidget();
    }
  }

  /**
   * If it's a structural change, we need to adapt the cfg forms. Any graphical views should be model change listeners already, and will probably be the origin
   * of the change anyway...
   * 
   * @throws URISyntaxException
   */
  public void changeExecuted(final ChangeRequest change) {
    if (changeImpactEnabled && !(change instanceof PrintRequest)) {
      if (change.getDescription() != null) {
        // XXX find a better way to filter changes. When desc does
        // not contain _controllerFactory, ... it means that an
        // entity has only moved
        if (hasChangeImpact(change)) {
          if (showModelForms) {
            showModelForm(null);
          }
          // ico Bossanova or other "wild" derived GUIs, there's not always a current model URL
          if (change.isPersistent() && getModelURL() != null) {
            setChanged(getModelURL());
          }
        }
      }
    }
  }

  protected boolean hasChangeImpact(final ChangeRequest change) {
    boolean hasChangeImpact = false;
    final String[] importantChanges = new String[] { "_controllerFactory", "_editorFactory", "_editorPaneFactory", "deleteEntity", "deleteProperty", "class" };
    for (final String changeType : importantChanges) {
      if (change.getDescription().contains(changeType)) {
        hasChangeImpact = true;
        break;
      }
    }
    return hasChangeImpact;
  }

  public void changeFailed(final ChangeRequest change, final Exception exception) {
    // TODO Auto-generated method stub

  }

  protected abstract void clearModelForms(URI modelURI);

  protected abstract void clearModelGraphs(URI modelURI);

  public void close(URL modelURL) {
    URI modelURI = null;
    try {
      modelURI = modelURL.toURI();
    } catch (URISyntaxException ex) {
      // We can safely ignore this since the modelURL is in compliance with RFC2396
    }

    if (checkUnsavedChanges(modelURI)) {
      clearModelForms(modelURI);
      if (showModelGraph == true) {
        clearModelGraphs(modelURI);
      }
      loadedModels.remove(modelURI);
      modelsChangedStatus.remove(modelURI);
      try {
        MoMLParser.purgeModelRecord(modelURL);
      } catch (final Exception ex) {
        // ignore, just trying to be nice and clean up memory
      }
    }
  }

  public boolean delete(URI modelURI) throws IllegalStateException {
    if (isDeleteAllowed(modelURI)) {
      File f = new File(modelURI);
      boolean deleted = f.delete();
      // if the delete succeeded, we must also remove the file from the list of recently opened models
      getHmiModelsDef().removeRecentModel(modelURI);
      recreateModelsMenu(modelsSubMenu);
      return deleted;
    } else {
      throw new IllegalStateException("Delete not allowed for " + modelURI);
    }
  }

  /**
   * CHecks if the flow loaded OK, to exclude errors when trying to save a "crippled" model i.e. one that only loaded partially when some actors are not on the
   * HMI classpath etc.
   * 
   * @param model
   * @return
   */
  public boolean checkFlowLoadingError(Flow model) {
    boolean result = true;
    if (modelURL != null) {
      // Boolean chgStatus = modelsChangedStatus.get(modelURL);
      if (!model.isLoadedFaultless()) {
        Icon icon = new ImageIcon(getClass().getResource("/com/isencia/passerelle/hmi/resources/ide32.gif"));
        int choice = PopupUtil.showOptionDialog(getDialogHookComponent(), HMIMessages.getString("warning.flow.loadingError"), HMIMessages.getString("warning"),
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, new String[] { "Continue", "Cancel" }, "Cancel");
        switch (choice) {
        case 0:
          result = true;
          break;
        default:
          result = false;
          break;
        }
      }
    }
    return result;
  }

  public boolean checkRunningModel() {
    boolean result = true;
    Boolean runningStatus = (StateMachine.MODEL_EXECUTING.equals(StateMachine.getInstance().getCurrentState()));

    if (Boolean.TRUE.equals(runningStatus)) {
      Icon icon = new ImageIcon(getClass().getResource("/com/isencia/passerelle/hmi/resources/ide32.gif"));
      int choice = PopupUtil.showOptionDialog(getDialogHookComponent(), HMIMessages.getString("warning.running.model"), HMIMessages.getString("warning"),
          JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, new String[] { "Continue", "Cancel" }, "Cancel");
      switch (choice) {
      case 0:
        result = true;
        if (getCurrentModel() != null && getCurrentModel().getManager() != null) {
          ExecutionTracerService.trace(getCurrentModel().getDirector(), "HMI shutting down. Stopping model execution for " + getCurrentModel().getName());
          try {
            getCurrentModel().getManager().stop();
          } catch (final Throwable t) {
            t.printStackTrace();
            logger.error("Error stopping model " + getCurrentModel().getFullName() + " during application shutdown", t);
          }
        }
        break;
      default:
        result = false;
        break;
      }
    }
    return result;
  }

  /**
   * Should be invoked before exiting the application. It checks if there are any unsaved changes... When there are unsaved changes, the user is warned, by
   * default by a pop-up dialog.
   * 
   * @return true if all's OK, i.e. the close/exit can be done, false if it can not be done.
   */
  public boolean checkUnsavedChanges() {
    return checkUnsavedChanges(modelsChangedStatus.keySet());
  }

  protected boolean checkUnsavedChanges(URI modelURL) {
    Collection<URI> modelURLs = new HashSet<URI>();
    modelURLs.add(modelURL);
    return checkUnsavedChanges(modelURLs);
  }

  /**
   * Should be invoked before closing an open model. When there are unsaved changes, the user is warned, by default by a pop-up dialog.
   * 
   * @return true if all's OK, i.e. the close/exit can be done, false if it can not be done.
   */
  protected boolean checkUnsavedChanges(Collection<URI> modelURLs) {
    boolean result = true;
    Boolean chgStatus = Boolean.FALSE;
    StringBuilder changedModels = new StringBuilder();
    for (URI modelURL : modelURLs) {
      Boolean modelChgStatus = modelsChangedStatus.get(modelURL);
      if (Boolean.TRUE.equals(modelChgStatus)) {
        chgStatus = Boolean.TRUE;
        Flow model = loadedModels.get(modelURL);
        if (model != null) {
          changedModels.append("\n    " + model.getName());
        }
      }
    }
    if (Boolean.TRUE.equals(chgStatus)) {
      Icon icon = new ImageIcon(getClass().getResource("/com/isencia/passerelle/hmi/resources/ide32.gif"));
      int choice = PopupUtil.showOptionDialog(getDialogHookComponent(), HMIMessages.getString("warning.unsaved.changes") + changedModels.toString(),
          HMIMessages.getString("warning"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, new String[] { "Continue", "Cancel" }, "Cancel");
      switch (choice) {
      case 0:
        result = true;
        break;
      default:
        result = false;
        break;
      }
    }
    return result;
  }

  public void print() {
    if (currentModel != null) {
      currentModel.requestChange(new PrintRequest(this, "Print"));
    }
  }

  /**
   * Override this method if you need another kind of animation listener in a specific HMI application
   * 
   * @return
   */
  protected AnimationEventListener createAnimationListener() {
    return new AnimationEventListener();
  }

  protected JMenuBar createDefaultMenu() {
    return createDefaultMenu(null, null);
  }

  /**
   * Constructs a default menu.
   * <ul>
   * <li>If the menuItems set is null, all default items are created.
   * <li>If the set is not null, only the menu items whose names are in there are shown
   * </ul>
   * For an overview of the names, check HMIMessages.MENU_...
   * 
   * @param menuItemsToShow
   * @param menuItemsToHide
   * @return
   */
  public JMenuBar createDefaultMenu(final Set<String> menuItemsToShow, final Set<String> menuItemsToHide) {
    final JMenuBar menuBar = new JMenuBar();

    final JMenu fileMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_FILE));
    fileMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_FILE + HMIMessages.KEY).charAt(0));

    if (showThing(HMIMessages.MENU_TEMPLATES, menuItemsToShow, menuItemsToHide)) {
      modelsSubMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_TEMPLATES));
      modelsSubMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_TEMPLATES + HMIMessages.KEY).charAt(0));
      recreateModelsMenu(modelsSubMenu);
      fileMenu.add(modelsSubMenu);

      StateMachine.getInstance().registerActionForState(StateMachine.READY, HMIMessages.MENU_TEMPLATES, modelsSubMenu);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_TEMPLATES, modelsSubMenu);
    }

    if (showModelGraph) {
      if (showThing(HMIMessages.MENU_NEW, menuItemsToShow, menuItemsToHide)) {
        if (modelCreatorAction == null) {
          modelCreatorAction = new ModelCreator(this);
        }
        fileMenu.add(modelCreatorAction);
      }
    }

    if (showThing(HMIMessages.MENU_OPEN, menuItemsToShow, menuItemsToHide)) {
      if (openAction == null) {
        openAction = new OpenAction(this);
      }
      fileMenu.add(openAction);
    }

    if (showThing(HMIMessages.MENU_CLOSE, menuItemsToShow, menuItemsToHide)) {
      if (closeAction == null) {
        closeAction = new CloseAction(this);
      }
      fileMenu.add(closeAction);
    }

    if (showThing(HMIMessages.MENU_CLOSEALL, menuItemsToShow, menuItemsToHide)) {
      if (closeAllAction == null) {
        closeAllAction = new CloseAllAction(this);
      }
      fileMenu.add(closeAllAction);
    }

    if (showThing(HMIMessages.MENU_SAVE, menuItemsToShow, menuItemsToHide)) {
      fileMenu.add(new JSeparator());
      if (saveAction == null) {
        saveAction = new SaveAction(this);
      }
      fileMenu.add(saveAction);
    }

    if (showThing(HMIMessages.MENU_SAVEAS, menuItemsToShow, menuItemsToHide)) {
      if (saveAsAction == null) {
        saveAsAction = new SaveAsAction(this);
      }
      fileMenu.add(saveAsAction);
    }

    if (showThing(HMIMessages.MENU_SAVEALL, menuItemsToShow, menuItemsToHide)) {
      if (saveAllAction == null) {
        saveAllAction = new SaveAllAction(this);
      }
      fileMenu.add(saveAllAction);
    }

    if (showThing(HMIMessages.MENU_ACTORLIBRARY_RELOAD, menuItemsToShow, menuItemsToHide)) {
      fileMenu.add(new JSeparator());
      if (actorLibReloadAction == null) {
        actorLibReloadAction = new ActorLibraryReloader(this);
      }
      fileMenu.add(actorLibReloadAction);
    }

    if (showThing(HMIMessages.MENU_PRINT, menuItemsToShow, menuItemsToHide)) {
      fileMenu.add(new JSeparator());
      if (printAction == null) {
        printAction = new ModelPrinter(this);
      }
      fileMenu.add(printAction);
    }

    if (showThing(HMIMessages.MENU_EXIT, menuItemsToShow, menuItemsToHide)) {
      final JMenuItem exitMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_EXIT), HMIMessages.getString(HMIMessages.MENU_EXIT + HMIMessages.KEY)
          .charAt(0));
      exitMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          checkExitApplication();
        }
      });
      fileMenu.add(new JSeparator());
      fileMenu.add(exitMenuItem);

      StateMachine.getInstance().registerActionForState(StateMachine.READY, HMIMessages.MENU_EXIT, exitMenuItem);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_EXIT, exitMenuItem);
    }

    final JMenu runMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_RUN));
    runMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_RUN + HMIMessages.KEY).charAt(0));

    if (showThing(HMIMessages.MENU_EXECUTE, menuItemsToShow, menuItemsToHide)) {
      if (modelExecutor == null) {
        modelExecutor = new ModelExecutor(this);
      }
      final JMenuItem runExecuteMenuItem = new JMenuItem(modelExecutor);
      runMenu.add(runExecuteMenuItem);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_EXECUTE, modelExecutor);
    }

    if (showThing(HMIMessages.MENU_STOP, menuItemsToShow, menuItemsToHide)) {
      if (modelStopper == null) {
        modelStopper = new ModelStopper(this);
      }
      final JMenuItem stopExecuteMenuItem = new JMenuItem(modelStopper);
      runMenu.add(stopExecuteMenuItem);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING, HMIMessages.MENU_STOP, modelStopper);
    }

    if (showThing(HMIMessages.MENU_INTERACTIVE_ERRORHANDLING, menuItemsToShow, menuItemsToHide)) {
      final JMenuItem interactiveErrorCtrlMenuItem = new JCheckBoxMenuItem(HMIMessages.getString(HMIMessages.MENU_INTERACTIVE_ERRORHANDLING));
      interactiveErrorCtrlMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          interactiveErrorControl = ((JCheckBoxMenuItem) e.getSource()).getState();
        }
      });
      runMenu.add(new JSeparator());
      runMenu.add(interactiveErrorCtrlMenuItem);
    }

    final JMenu graphMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_GRAPH));
    graphMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_GRAPH + HMIMessages.KEY).charAt(0));

    if (showModelGraph && showThing(HMIMessages.MENU_ANIMATE, menuItemsToShow, menuItemsToHide)) {
      final JMenuItem animateGraphViewMenuItem = new JCheckBoxMenuItem(HMIMessages.getString(HMIMessages.MENU_ANIMATE));
      animateGraphViewMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          animateModelExecution = ((JCheckBoxMenuItem) e.getSource()).getState();
        }
      });
      graphMenu.add(new JSeparator());
      graphMenu.add(animateGraphViewMenuItem);
    }

    if (fileMenu.getMenuComponentCount() > 0) {
      menuBar.add(fileMenu);
    }

    if (runMenu.getMenuComponentCount() > 0) {
      menuBar.add(runMenu);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_RUN, runMenu);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING, HMIMessages.MENU_RUN, runMenu);
    }

    if (graphMenu.getMenuComponentCount() > 0) {
      menuBar.add(graphMenu);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_GRAPH, graphMenu);
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING, HMIMessages.MENU_GRAPH, graphMenu);
    }

    final JMenu monitoringMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_MONITORING));
    monitoringMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_MONITORING + HMIMessages.KEY).charAt(0));
    if (showThing(HMIMessages.MENU_TRACING, menuItemsToShow, menuItemsToHide)) {
      final JMenuItem traceMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_TRACING), HMIMessages.getString(
          HMIMessages.MENU_TRACING + HMIMessages.KEY).charAt(0));
      traceMenuItem.addActionListener(new TraceDialogOpener());
      monitoringMenu.add(traceMenuItem);
    }

    if (monitoringMenu.getMenuComponentCount() > 0) {
      menuBar.add(monitoringMenu);
    }

    StateMachine.getInstance().compile();
    StateMachine.getInstance().transitionTo(StateMachine.READY);
    return menuBar;
  }

  /**
   * @param modelsSubMenu
   * @return
   */
  protected JMenu recreateModelsMenu(final JMenu modelsSubMenu) {
    // 29/04/2011 : SPJZ for Bossanova need
    if (modelsSubMenu == null) {
      return null;
    }
    // 29/04/2011 : SPJZ for Bossanova need

    modelsSubMenu.removeAll();
    // we collect all failed menu items
    // and then remove the corresponding model keys from the hmi def
    List<String> errorItems = new ArrayList<String>();
    final Iterator<String> itr = hmiModelsDef.getPredefinedModels().keySet().iterator();
    while (itr.hasNext()) {
      String modelKey = itr.next();
      try {
        final JMenuItem templateMenuItem = new JMenuItem(HMIMessages.getString(modelKey));
        templateMenuItem.setToolTipText(hmiModelsDef.getModel(modelKey).getMomlPath().toString());
        templateMenuItem.addActionListener(new TemplateModelOpener(modelKey));
        modelsSubMenu.add(templateMenuItem);
      } catch (final Exception e) {
        logger.error("Error constructing menu item for " + modelKey, e);
        errorItems.add(modelKey);
      }
    }
    modelsSubMenu.addSeparator();
    int i = 1;
    for (String modelKey : hmiModelsDef.getRecentModelsList()) {
      try {
        final JMenuItem templateMenuItem = new JMenuItem((i) + ". " + HMIMessages.getString(modelKey));
        templateMenuItem.setToolTipText(hmiModelsDef.getModel(modelKey).getMomlPath().toString());
        if (i < 10) {
          templateMenuItem.setMnemonic(Integer.toString(i).charAt(0));
        }
        i++;
        templateMenuItem.addActionListener(new TemplateModelOpener(modelKey));
        modelsSubMenu.add(templateMenuItem);
      } catch (Exception e) {
        logger.error("Error constructing menu item for " + modelKey, e);
        errorItems.add(modelKey);
      }
    }
    modelsSubMenu.validate();

    for (String errorItem : errorItems) {
      hmiModelsDef.removeModel(errorItem);
    }

    return modelsSubMenu;
  }

  public JToolBar createDefaultToolbar() {
    return createToolbar(true);
  }

  public JToolBar createToolbarWithoutSave() {
    return createToolbar(false);
  }

  private JToolBar createToolbar(final boolean withSaveButton) {
    final JToolBar toolBar = new JToolBar();

    if (modelExecutor == null) {
      modelExecutor = new ModelExecutor(this);
    }
    if (modelStopper == null) {
      modelStopper = new ModelStopper(this);
    }
    if (modelDebugger == null) {
      modelDebugger = new ModelDebugger(this);
    }
    if (modelDebugStepper == null) {
      modelDebugStepper = new ModelDebugStepper(this);
    }
    if (modelSuspender == null) {
      modelSuspender = new ModelSuspender(this);
    }
    if (modelResumer == null) {
      modelResumer = new ModelResumer(this);
    }
    // if (saveAction == null) {
    // saveAction = new SaveAction(this);
    // }
    // DBA : add tooltip for each button
    createToolbarButton(modelExecutor, toolBar, HMIMessages.getString(HMIMessages.MENU_EXECUTE));
    createToolbarButton(modelSuspender, toolBar, HMIMessages.getString(HMIMessages.MENU_SUSPEND));
    createToolbarButton(modelResumer, toolBar, HMIMessages.getString(HMIMessages.MENU_RESUME));
    createToolbarButton(modelStopper, toolBar, HMIMessages.getString(HMIMessages.MENU_STOP));
    toolBar.addSeparator();
    createToolbarButton(modelDebugger, toolBar, HMIMessages.getString(HMIMessages.MENU_DEBUG));
    createToolbarButton(modelDebugStepper, toolBar, HMIMessages.getString(HMIMessages.MENU_DEBUG_STEP));
    toolBar.addSeparator();
    toolBar.addSeparator();

    if (withSaveButton) {
      if (saveAction == null) {
        saveAction = new SaveAction(this);
      }
      createToolbarButton(saveAction, toolBar, HMIMessages.getString(HMIMessages.MENU_SAVE));
    }

    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_EXECUTE, modelExecutor);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_DEBUG, modelDebugger);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_DEBUG_STEP, modelDebugStepper);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_STOP, modelStopper);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING, HMIMessages.MENU_STOP, modelStopper);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING_SUSPENDED, HMIMessages.MENU_STOP, modelStopper);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING, HMIMessages.MENU_SUSPEND, modelSuspender);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_EXECUTING_SUSPENDED, HMIMessages.MENU_RESUME, modelResumer);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_DEBUGGING, HMIMessages.MENU_RESUME, modelResumer);

    StateMachine.getInstance().compile();
    StateMachine.getInstance().transitionTo(StateMachine.READY);

    return toolBar;
  }

  /**
   * Override this method if you need another kind of execution listener in a specific HMI application
   * 
   * @return
   */
  protected ModelExecutionListener createExecutionListener() {
    return new ModelExecutionListener();
  }

  /**
   * DBA : add tooltip for each button
   * 
   * @return
   */
  private JButton createToolbarButton(final Action action, final JToolBar toolbar, final String tooltip) {
    final JButton b = new JButton(action);
    b.setBorderPainted(false);
    b.setToolTipText(tooltip);
    b.setText(null);
    toolbar.add(b);
    return b;
  }

  /**
   * Method that can be overridden if execution trace messages need to be displayed in a custom component. Default implementation uses a TraceDialog, that wraps
   * a TracePanel. The component should implement the TraceVisualizer interface.
   * 
   * @return the chosen TraceVisualizer UI implementation
   */
  protected TraceVisualizer createTraceComponent() {
    final TraceDialog traceDialog = new TraceDialog(getDialogHookComponent());
    traceDialog.setVisible(false);

    return traceDialog;
  }

  /**
   * The code here will be executing before application exit.
   */
  public final boolean checkExitApplication() {
    boolean result = true;
    if (!StateMachine.EXITING.equals(StateMachine.getInstance().getCurrentState())) {
      if (logger.isTraceEnabled()) {
        logger.trace("Exit action - entry"); //$NON-NLS-1$
      }

      if ((result = checkRunningModel())) {
        if ((result = checkUnsavedChanges())) {
          logger.info(HMIMessages.getString(HMIMessages.INFO_EXIT));
          // DBA to be validating...
          // Application which use HMIBase needs to call exit
          // treatment before exiting
          doExitApplication();
          if (logger.isTraceEnabled()) {
            logger.trace("Exit action - exit"); //$NON-NLS-1$
          }
          StateMachine.getInstance().transitionTo(StateMachine.EXITING);
          System.exit(0);
        } else {
          if (logger.isTraceEnabled()) {
            logger.trace("Exit action - exit refused due to unsaved changes"); //$NON-NLS-1$
          }
        }
      } else {
        if (logger.isTraceEnabled()) {
          logger.trace("Exit action - exit refused due to running model"); //$NON-NLS-1$
        }
      }
    }
    return result;
  }

  /**
   * Overridable method to put custom logic that must be executed before the application exits.
   */
  protected void doExitApplication() {
    // TODO Auto-generated method stub

  }

  public Flow getCurrentModel() {
    return currentModel;
  }

  public Model getCurrentModelDef() {
    return currentModelDef;
  }

  public Director getDirector() {
    return currentModel.getDirector();
  }

  public ModelBundle getHmiModelsDef() {
    return hmiModelsDef;
  }

  public Map<URI, Flow> getLoadedModels() {
    return loadedModels;
  }

  public Map<URI, Boolean> getModelsChangedStatus() {
    return modelsChangedStatus;
  }

  /**
   * Checks if the user can be allowed to delete the given model file. For the moment this is only used for local model files (i.e. not managed via REST/web
   * services).
   * <p>
   * Current logic in the basic HMI is to make sure the model is not currently loaded, and not part of the predefined models in case the HMI is a custom
   * configured one.
   * </p>
   * 
   * @param modelURI
   * @return
   */
  public boolean isDeleteAllowed(URI modelURI) {
    boolean result = !getLoadedModels().containsKey(modelURI);
    if (result) {
      result = !getHmiModelsDef().containsPredefinedModel(modelURI);
    }
    return result;
  }

  public boolean isChangedModel(URI modelURI) {
    Boolean changeStatus = getModelsChangedStatus().get(modelURI);
    return (changeStatus != null) && changeStatus;
  }

  public boolean isChangedModel(Flow model) throws IOException {
    if (model == null)
      return false;

    URL modelURL = model.getAuthorativeResourceLocation();
    if (modelURL == null) {
      // should not happen, but if it does, we'll try to find it in the loadedModels map...
      for (Entry<URI, Flow> modelEntry : getLoadedModels().entrySet()) {
        if (model.equals(modelEntry.getValue())) {
          try {
            modelURL = modelEntry.getKey().toURL();
          } catch (MalformedURLException e) {
            throw new IOException("Model URL not a valid local file " + modelEntry.getKey());
          }
          break;
        }
      }
    }

    boolean returnValue = false;
    if (modelURL != null) {
      try {
        returnValue = getModelsChangedStatus().get(modelURL.toURI()).booleanValue();
      } catch (URISyntaxException e) {
        // We can safely ignore this since all used URLs are in compliance with RFC2396
      }
    }
    return returnValue;
  }

  public File getLocalFileFromURL(final URI destinationURL) throws IOException {
    File destinationFile = null;
    destinationFile = new File(destinationURL);

    return destinationFile;
  }

  public URL getModelURL() {
    return modelURL;
  }

  public Configuration getPtolemyConfiguration() {
    return config;
  }

  public SaveAction getSaveAction() {
    return saveAction;
  }

  public SaveAsAction getSaveAsAction() {
    return saveAsAction;
  }

  public TraceVisualizer getTraceComponent() {
    return traceComponent;
  }

  /**
   * must return a swing parent component to which all pop-ups, dialogues etc can be hooked.
   * 
   * @return
   */
  public abstract Frame getDialogHookComponent();

  /**
   * Must be implemented to show the general frame of the HMI, without actual forms/fields for specific models in it yet.
   * 
   * @param title
   * @return some component we can use for locating pop-ups etc
   * @deprecated
   */
  protected Component initUI(String title) {
    return null;
  }

  public void launchModel(final Flow model, final ModelExecutor executor, final boolean blocking, final boolean updateState) {
    try {
      final ptolemy.actor.Director director = model.getDirector();
      DirectorAdapter directorAdapter = DirectorUtils.getAdapter(director, null);
      if (interactiveErrorControl) {
        directorAdapter.setErrorControlStrategy(new AskTheUserErrorControlStrategy(this, executor), true);
      } else {
        directorAdapter.setErrorControlStrategy(new DefaultActorErrorControlStrategy(), false);
      }

      // we enforce a suspend/resume control
      // TODO investigate if we should be able to support multiple
      // execution controllers,
      // e.g. when one has been configured already for the director...
      final Attribute tmp = director.getAttribute(HMIBase.EXECUTION_CONTROL_ATTR_NAME);
      if (tmp != null) {
        // remove the previous execution controller
        tmp.setContainer(null);
      }
      if (executor != null) {
        executor.createExecutionControlStrategy(director, HMIBase.EXECUTION_CONTROL_ATTR_NAME);
      }

      directorAdapter.removeAllErrorCollectors();
      final ModelExecutionListener executionListener = createExecutionListener();

      animationEventListener = setModelAnimation(model, animateModelExecution, animationEventListener);

      if (blocking) {
        flowManager.executeBlockingLocally(model, null, executionListener);
      } else {
        flowManager.execute(model, null, executionListener);
      }

      if (updateState && executor != null && executionListener.isExecuting()) {
        StateMachine.getInstance().transitionTo(executor.getSuccessState());
      }
    } catch (final Throwable t) {
      logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
      PopupUtil.showError(getDialogHookComponent(), "error.execution.error", t.getMessage());
      StateMachine.getInstance().transitionTo(executor.getErrorState());
    }
  }

  public void launchModel(final ModelExecutor executor) {
    boolean runRemotely = false;
    if (getCurrentModel() instanceof Flow) {
      Flow currentModel = (Flow) getCurrentModel();
      if (currentModel.getHandle().isRemote()) {
        runRemotely = true;
        try {
          flowManager.execute(currentModel, null);
          StateMachine.getInstance().transitionTo(executor.getSuccessState());
        } catch (Throwable t) {
          logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
          PopupUtil.showError(getDialogHookComponent(), "error.execution.error", t.getMessage());
          StateMachine.getInstance().transitionTo(executor.getErrorState());
        }

        if (remoteExecutionTracePoller == null) {
          remoteExecutionTracePoller = new RemoteExecutionTracePoller();
          new Thread(remoteExecutionTracePoller).start();
        }
      }
    }
    if (!runRemotely) {
      applyFieldValuesToParameters();
      launchModel(getCurrentModel(), executor, false, true);
    }
  }

  /**
   * @param _modelURL
   * @throws Exception
   */
  public Flow loadModel(final URL _modelURL, String modelKey) throws Exception {
    URI mdlURI = _modelURL.toURI();
    Flow currentModelTmp = loadedModels.get(mdlURI);
    if (currentModelTmp == null) {
      currentModelTmp = ModelUtils.loadModel(_modelURL);
      // Soleil Mantis 27166 : double check that someone has not copied/moved/modified model files outsids of HMI
      // which could lead to inconsistent model&file names.
      String expectedModelName = getExpectedModelName(mdlURI);
      if (expectedModelName.equals(currentModelTmp.getName())) {
        loadedModels.put(mdlURI, currentModelTmp);
        setCurrentModel(currentModelTmp, _modelURL, modelKey, showModelGraph);
      } else {
        throw new Exception(HMIMessages.getString(HMIMessages.HMI_ERROR_FILE_OPEN_INFO) + " : " + currentModelTmp.getName() + " - " + expectedModelName);
      }
    } else {
      refreshParamsForm(mdlURI, modelKey);
    }
    return currentModelTmp;
  }

  protected Configuration readConfiguration(final String urlSpec) throws Exception {
    logger.debug("read Passerelle Configuration");

    final URL inurl = specToURL(urlSpec);
    final MoMLParser parser = new MoMLParser();
    final Configuration toplevel = (Configuration) parser.parse(inurl, inurl.openStream());
    // If the toplevel model is a configuration containing a directory,
    // then create an effigy for the configuration itself, and put it
    // in the directory.
    final ComponentEntity directoryEntity = toplevel.getEntity("directory");
    if (directoryEntity instanceof ModelDirectory) {
      final PtolemyEffigy effigy = new PtolemyEffigy((ModelDirectory) directoryEntity, toplevel.getName());
      effigy.setModel(toplevel);
      effigy.identifier.setExpression(inurl.toExternalForm());
    }
    return toplevel;
  }

  protected void refreshParamsForm(final URI _modelURL, String modelKey) throws Exception {
    if (loadedModels.containsKey(_modelURL)) {
      final Flow currentModelTmp = loadedModels.get(_modelURL);
      setCurrentModel(currentModelTmp, _modelURL.toURL(), modelKey, false);
    }

  }

  /**
   * @param mappingKey
   * @param uiWidget
   * @param binder
   * @return
   */
  protected Object registerBinding(final String mappingKey, final Object uiWidget, final ParameterToWidgetBinder binder) {
    final String paramName = getCurrentModelDef().getFieldMapping().getValueForKey(mappingKey);
    try {
      final Parameter p = ModelUtils.getParameter(getCurrentModel(), paramName);
      p.addValueListener(new ParameterChangeListener(binder));
      binder.setBoundComponent(uiWidget);
      binder.setBoundParameter(p);
      hmiFields.put(mappingKey, binder);
      // now make sure the fields contain the param values from the model
      binder.fillWidgetFromParameter();
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return uiWidget;
  }

  public void saveModelAs(final Flow model, final URI destinationURL) throws IOException, IllegalActionException, NameDuplicationException {

    if (checkFlowLoadingError(model)) {
      File destinationFile = null;

      try {
        destinationFile = getLocalFileFromURL(destinationURL);
      } catch (final Exception e) {
        // ignore, means that the URL is unknown or a remote model URL
        // this is handled further below
      }
      applyFieldValuesToParameters();
      if (destinationFile != null) {
        if ((!destinationFile.exists() || destinationFile.canWrite())) {

          BufferedWriter outputWriter = null;
          try {
            outputWriter = new BufferedWriter(new FileWriter(destinationFile));
            // set name of the model with file name
            String newModelName = destinationFile.getName();
            if (newModelName.contains(".moml")) {
              newModelName = newModelName.replace(".moml", "");
            }
            final String oldName = model.getName();
            model.setName(newModelName);
            model.exportMoML(outputWriter);
            model.setName(oldName);
          } finally {
            if (outputWriter != null) {
              outputWriter.flush();
              outputWriter.close();
              setSaved(destinationURL);
            }
          }
        } else {
          throw new IOException("File not writable " + destinationFile);
        }
      } else if (model instanceof Flow) {
        Flow f = (Flow) model;
        if (f.getHandle().isRemote()) {
          try {
            FlowManager.save(f, destinationURL.toURL());
            setSaved(destinationURL);
          } catch (Exception e) {
            throw new IOException("Error saving remote flow", e);
          }
        }
      }
    }
  }

  protected void setCurrentModel(final Flow model, URL _modelURL, String modelKey, final boolean loadGraphPanel) {
    if (model == null) {
      currentModel = null;
      modelURL = null;
      hmiFields.clear();
    } else {
      modelKey = validateModel(model, modelKey);
      // if (modelURL==null || !modelURL.equals(_modelURL)) {
      modelURL = _modelURL;
      currentModelDef = hmiModelsDef.getModel(modelKey);
      if (currentModelDef == null || (_modelURL != null && currentModelDef.getMomlPath().toString().compareTo(_modelURL.toString()) != 0)) {
        currentModelDef = new Model(_modelURL, new FieldMapping());
      }
      hmiModelsDef.addModel(modelKey, currentModelDef);
      recreateModelsMenu(modelsSubMenu);

      if (currentModel != null) {
        currentModel.removeChangeListener(this);
      }
      currentModel = model;
      if (currentModel != null) {
        currentModel.addChangeListener(this);
      }
      // }
      hmiFields.clear();
      if (loadGraphPanel) {
        showModelGraph(modelKey);
        // the above also indirectly will show the cfg forms
      } else if (showModelForms) {
        showModelForm(modelKey);
      }
    }
  }

  public void setHmiModelsDef(final ModelBundle hmiModelsDef) {
    this.hmiModelsDef = hmiModelsDef;
  }

  /**
   * Override this method in HMI subclasses if you want to specify animation behaviour for the model. <br>
   * By default, this uses an AnimationEventListener that will animate the graphical view of the model being executed.
   * 
   * @param model
   * @param animateModelExecution if true, some kind of animation should be provided
   * @param currentListener the current animation listener that was set, e.g. for a previous model execution
   * @return the listener that was effectively set if animation is needed, or null if no animation is set
   */
  protected AnimationEventListener setModelAnimation(final Flow model, final boolean animateModelExecution, AnimationEventListener currentListener) {
    final DirectorAdapter directoradapter = DirectorUtils.getAdapter(model.getDirector(), null);
    if (animateModelExecution) {
      if (currentListener == null) {
        currentListener = createAnimationListener();
      }
      directoradapter.registerFiringEventListener(currentListener);
    } else {
      directoradapter.removeFiringEventListener(animationEventListener);
      currentListener = null;
    }
    return currentListener;
  }

  public void setModelURL(final URL modelURL) {
    this.modelURL = modelURL;
  }

  public void setChanged(final URL modelURL) {
    try {
      modelsChangedStatus.put(modelURL.toURI(), Boolean.TRUE);
    } catch (URISyntaxException e) {
      // We can safely ignore this since all used URLs are in compliance with RFC2396
    }
  }

  public void setSaved(final URI modelURL) {
    modelsChangedStatus.remove(modelURL);
  }

  public void setTraceComponent(final TraceVisualizer traceComponent) {
    this.traceComponent = traceComponent;
  }

  /**
   * Must fill the form/field contents of the HMI corresponding to what's needed to configure models corresponding to the given key.
   * 
   * @param modelKey
   */
  protected abstract void showModelForm(String modelKey);

  protected void showModelGraph(final String modelKey) {
    try {
      final JFrame gf = new JFrame(HMIMessages.getString(HMIMessages.GRAPH_TITLE));
      gf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      if (graphPanelEffigy == null) {
        graphPanelEffigy = new PtolemyEffigy(getPtolemyConfiguration(), modelKey);
      }
      graphPanel = new ModelGraphPanel(currentModel, graphPanelEffigy);

      // TODO BEWARE : animation now happens in the actor
      // thread and blocks for a second!!
      // graphPanel.setAnimationDelay(1000);
      gf.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(final WindowEvent e) {
          try {
            clearModelGraphs(getModelURL().toURI());
          } catch (URISyntaxException ex) {
            // Ignore. Just trying to clean up
          }
        }
      });
      gf.getContentPane().add(graphPanel);
      gf.setLocation(400, 300);
      gf.setSize(700, 400);
      gf.setVisible(true);

    } catch (final Throwable t) {
      logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
    }
  }

  private boolean showThing(final String thingName, final Set<String> thingsToShow, final Set<String> thingsToHide) {
    return (thingsToShow == null || thingsToShow.contains(thingName)) && (thingsToHide == null || !thingsToHide.contains(thingName));
  }

  public void stopModel() {
    Flow currentModel = getCurrentModel();
    if (StateMachine.MODEL_DEBUGGING.equals(StateMachine.getInstance().getCurrentState())) {
      // in stepping mode, we first must go to plain execution mode,
      // to allow the manager.stop() to reach all actors!
      ((DynamicStepExecutionControlStrategy) DirectorUtils.getAdapter(getDirector(), null).getExecutionControlStrategy()).resume();
    }
    try {
      // wait at most 10s for the complete model stop
      flowManager.stopExecution(currentModel, 10000);
    } catch (Throwable t) {
      logger.error("Error stopping execution of model " + currentModel.getDisplayName(), t);
      PopupUtil.showError(getDialogHookComponent(), "Error stopping execution of model " + currentModel.getDisplayName(), t.getMessage());
    } finally {
      StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
    }
  }

  public void suspendModel() {
    try {
      flowManager.pauseExecution(getCurrentModel());
      StateMachine.getInstance().transitionTo(StateMachine.MODEL_EXECUTING_SUSPENDED);
    } catch (final Throwable t) {
      t.printStackTrace();
      logger.error("Error suspending execution of model " + currentModel.getDisplayName(), t);
      PopupUtil.showError(getDialogHookComponent(), "Error suspending execution of model " + currentModel.getDisplayName(), t.getMessage());
    }
  }

  public void resumeModel() {
    try {
      flowManager.resumeExecution(getCurrentModel());
      StateMachine.getInstance().transitionTo(StateMachine.MODEL_EXECUTING);
    } catch (final Throwable t) {
      t.printStackTrace();
      logger.error("Error resuming execution of model " + currentModel.getDisplayName(), t);
      PopupUtil.showError(getDialogHookComponent(), "Error resuming execution of model " + currentModel.getDisplayName(), t.getMessage());
    }
  }

  /**
   * Checks if the given model is compatible with this HMI, based on the HMI configuration definition.
   * 
   * @param currentModelTmp
   * @return
   */
  protected String validateModel(final Flow model, final String modelKey) {
    if (SPECIFIC.equals(mode)) {
      String result = null;
      // now let's check if we support it
      // the model name is used as model type key
      final String _modelkey = model.getName();
      if (modelKey != null && !modelKey.equals(_modelkey)) {
        throw new IllegalArgumentException("Invalid model key in " + model.getName() + ", expecting " + modelKey);
      } else {
        // check if the _modelkey from the model is support by this HMI
        if (hmiModelsDef.getModel(_modelkey) != null) {
          // finally, it's a supported model type
          result = _modelkey;
        }
      }
      if (result == null) {
        throw new IllegalArgumentException("Invalid model for this HMI " + model.getName());
      }

      return result;
    } else {
      if (modelKey != null)
        return modelKey;
      else
        return model.getName();
    }
  }

  private final class PrintRequest extends ChangeRequest {
    private PrintRequest(Object source, String description) {
      super(source, description);
    }

    protected void _execute() throws Exception {
      // Build a set of attributes
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      PrinterJob job = PrinterJob.getPrinterJob();

      job.setPrintable(graphPanel);

      if (job.printDialog(aset)) {
        try {
          job.print(aset);
        } catch (Exception ex) {
          MessageHandler.error("Printing Failed", ex);
        }
      }
    }
  }

  protected class AnimationEventListener implements FiringEventListener {
    public void onEvent(final FiringEvent e) {
      if (graphPanel != null) {
        graphPanel.event(e);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("onEvent() : " + ((Entity) e.getActor()).getName() + ":" + e.getType());
      }
    }
  }

  public static class Mode {
    private Mode() {

    }
  }

  protected class ModelExecutionListener implements ExecutionListener, ErrorCollector {

    private boolean popUpError = true;
    private boolean execDone = false;

    public void acceptError(final PasserelleException e) {
      executionError(null, e);
    }

    public synchronized void executionError(final Manager manager, final Throwable throwable) {
      // logger.error(HMIMessages.getString("error.execution.error"), throwable);
      if (Manager.IDLE.equals(manager.getState())) {
        // There's a big chance that the executionFinished() will not be invoked anymore
        // as Ptolemy only calls it when execution is successfully finished.
        // And since we're still getting error notifs after the Manager went to IDLE
        // we can be pretty sure something dramatic went wrong (e.g. an Actor.preInitialize error).
        // But the HMI needs to be made aware about the execution having been stopped...
        executionFinished(manager);
      }
      if (popUpError) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            PopupUtil.showError(getDialogHookComponent(), "error.execution.error", throwable.getMessage());
            // StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
          }
        });
        popUpError = false;
      }
    }

    public synchronized void executionFinished(final Manager manager) {
      if (!StateMachine.getInstance().getCurrentState().equals(StateMachine.MODEL_OPEN)) {
        StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
        // SwingUtilities.invokeLater(new Runnable() {
        // public void run() {
        // PopupUtil
        // .showInfo(getDialogHookComponent(), "info.execution.finished");
        // }
        // });
      }
      execDone = true;
    }

    public synchronized boolean isExecuting() {
      return !execDone;
    }

    public synchronized void managerStateChanged(final Manager manager) {
      if (manager.getState().equals(Manager.INITIALIZING)) {
        popUpError = true;
        execDone = false;
      }
    }
  }

  /**
   * TODO : determine when/how to invoke the stopPolling()...
   * 
   * @author delerw
   */
  public class RemoteExecutionTracePoller implements Runnable {
    private Map<Flow, Long> lastTraceIds = new HashMap<Flow, Long>();

    private boolean continuePolling = true;

    public void run() {
      while (continuePolling) {
        if (flowManager != null && flowManager.getRemoteFlowExecutionsList() != null) {
          if (flowManager.getRemoteFlowExecutionsList().isEmpty()) {
            stopPolling();
            lastTraceIds.clear();
            HMIBase.this.remoteExecutionTracePoller = null;
            if (!StateMachine.getInstance().getCurrentState().equals(StateMachine.MODEL_OPEN)) {
              StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
            }
            break;
          }
          List<Flow> stoppedFlows = new ArrayList<Flow>();
          for (Flow remoteFlow : flowManager.getRemoteFlowExecutionsList()) {
            if (remoteFlow.getHandle().getExecResourceLocation() == null) {
              // this is the signal that the remote execution is done
              stoppedFlows.add(remoteFlow);
            } else {
              try {
                List<ExecutionTraceRecord> traces = flowManager.getRemoteExecutionTraces(remoteFlow.getHandle());
                for (ExecutionTraceRecord trace : traces) {
                  if (lastTraceIds.get(remoteFlow) == null || trace.getId() > lastTraceIds.get(remoteFlow)) {
                    lastTraceIds.put(remoteFlow, trace.getId());

                    Actor actor = (Actor) remoteFlow.getEntity(trace.getSource());
                    if (actor != null)
                      ExecutionTracerService.trace(actor, trace.getMessage());
                    else if (remoteFlow.getDirector().getName().contains(trace.getSource())) {
                      ExecutionTracerService.trace(remoteFlow.getDirector(), trace.getMessage());
                    }
                  }
                }
              } catch (Exception e) {
                logger.error("Remote execution tracing error", e);
              }
            }
          }
          for (Flow stoppedFlow : stoppedFlows) {
            flowManager.getRemoteFlowExecutionsList().remove(stoppedFlow);
          }
        }
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          logger.error("Remote execution tracing interrupted", e);
        }

        List<Flow> toBeCleanedUp = new ArrayList<Flow>();
        for (Flow f : lastTraceIds.keySet()) {
          if (!flowManager.getRemoteFlowExecutionsList().contains(f)) {
            toBeCleanedUp.add(f);
          }
        }

        for (Flow flow : toBeCleanedUp) {
          lastTraceIds.remove(flow);
        }
      }
    }

    public void stopPolling() {
      continuePolling = false;
    }
  }

  /**
   * Opens a new form for a given model key, i.e. a form containing values from the "template" model file.
   * 
   * @author erwin.de.ley@isencia.be
   */
  protected class TemplateModelOpener implements ActionListener {

    String modelKey;

    public TemplateModelOpener(final String key) {
      modelKey = key;
    }

    public void actionPerformed(final ActionEvent e) {
      try {
        currentModelDef = hmiModelsDef.getModel(modelKey);
        loadModel(currentModelDef.getMomlPath(), modelKey);

        StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);

      } catch (final Exception e1) {
        logger.error("Error opening file", e1);
        PopupUtil.showError(getDialogHookComponent(), "error.file.open", e1.getLocalizedMessage());
        hmiModelsDef.removeModel(modelKey);
        recreateModelsMenu(modelsSubMenu);
      }
    }

  }

  protected class TraceDialogOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      traceComponent.show();
    }
  }

  public void disableChangeImpacts() {
    this.changeImpactEnabled = false;
  }

  public void enableChangeImpacts() {
    this.changeImpactEnabled = true;
  }

  public String getExpectedModelName(URI modelURI) {
    String modelName = modelURI.getPath();
    modelName = modelName.substring(modelName.lastIndexOf('/') + 1);
    int extSeparatorIndex = modelName.lastIndexOf('.');
    if (extSeparatorIndex > 0) {
      return modelName.substring(0, extSeparatorIndex);
    } else {
      return modelName;
    }
  }
}
