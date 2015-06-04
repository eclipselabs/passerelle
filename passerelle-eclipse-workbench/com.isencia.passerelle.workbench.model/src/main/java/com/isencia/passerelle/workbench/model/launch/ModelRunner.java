package com.isencia.passerelle.workbench.model.launch;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import com.isencia.constants.IPropertyNames;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ModelRunner implements IApplication {

  private static Logger logger = LoggerFactory.getLogger(ModelRunner.class);

  private static ModelRunner currentInstance;

  public static ModelRunner getRunningInstance() {
    return currentInstance;
  }

  private Manager manager;

  public Object start(IApplicationContext applicationContextMightBeNull) throws Exception {

    String model = System.getProperty("model");
    runModel(model, "true".equals(System.getProperty("com.isencia.jmx.service.terminate")));
    return IApplication.EXIT_OK;
  }

  public void stop() {
    if (manager != null) {
      try {
        manager.stop();
      } catch (Throwable ne) {
        logger.error("Cannot stop manager for model.", ne);
      }
      manager = null;
    }
  }

  /**
   * Sometimes can be called
   * 
   * @param modelPath
   */
  public void runModel(String modelPath, final boolean doSystemExit) throws Exception {

    if (!Platform.isRunning())
      throw new Exception("ModelRunner is designed to be used with an eclipse application!");

    final List<Exception> exceptions = new ArrayList<Exception>(1);
    final long start = System.currentTimeMillis();
    try {

      // TODO Check that path works when model is run... Edna actors currently use
      // workspace to get resources.
      // When run from command line may need to set variable for workspace.
      String workspacePath = System.getProperty("com.isencia.jmx.service.workspace");
      if (workspacePath == null)
        workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();

      System.setProperty("eclipse.workspace.home", workspacePath);
      System.setProperty(IPropertyNames.APP_HOME, workspacePath);
      logger.info("Workspace folder set to: " + workspacePath);

      Reader reader = null;
      RemoteManagerAgent modelAgent = null;
      CompositeActor compositeActor = null;
      try {
        currentInstance = this;
        if (modelPath == null) {
          throw new IllegalArgumentException("No model specified", null);
        } else {
          logger.info("Running model : " + modelPath);

          // The manager JMX service is used to control the workflow from
          // the RCP workspace. This starts the registry on a port and has two
          // JMX objects in the registry, one for calling method on the workbench
          // from actors and one for giving access to controlling the workflow.
          // If this has been set up the property "com.isencia.jmx.service.port"
          // will have been set to the free port being used. Otherwise the workflow
          // service will not be added to the registry.
          if (System.getProperty("com.isencia.jmx.service.port") != null) {
            logger.debug("The jmx port is set to : '" + System.getProperty("com.isencia.jmx.service.port") + "'");
            modelAgent = new RemoteManagerAgent(manager);
            modelAgent.start();
          }

          notifyModelChangeStart();
          // to support launch from new graphiti-based editor
          modelPath = modelPath.replace(".pdml", ".moml");
          
          reader = new FileReader(modelPath);

          // In debug mode the same model can be run in the
          // same VM several times. We purge before running for this reason.
          MoMLParser.purgeModelRecord(modelPath);
          MoMLParser.purgeAllModelRecords();

          final Workspace workspace = ModelUtils.getWorkspace(modelPath);
          compositeActor = FlowManager.readMoml(reader, workspace, null, null);
          if (System.getProperty("com.isencia.require.file.source") != null) {
            compositeActor.setSource(modelPath);
          }

          this.manager = new Manager(compositeActor.workspace(), getUniqueName());
          manager.setPersistent(false); // Important for test decks to pass.

          compositeActor.setManager(manager);

          // Errors
          final PasserelleDirector director = (PasserelleDirector) compositeActor.getDirector();
          director.getAdapter(null).addErrorCollector(new ErrorCollector() {
            public void acceptError(PasserelleException e) {
              exceptions.add(e);
              manager.stop();
            }
          });

          manager.execute(); // Blocks until done

          // Well almost
          if (manager != null)
            while (manager.isExitingAfterWrapup()) {
              logger.info("Waiting for manager to wrap up.");
              Thread.sleep(100);
            }
        }

      } catch (IOException e) {
        logger.info("No model found to run");
      } finally {

        notifyModelChangeEnd(0);

        if (modelAgent != null) {
          modelAgent.stop();
          logger.info("Closed model agent");
        }
        if (reader != null) {
          reader.close();
          logger.info("Closed reader");
        }

        manager = null;
        currentInstance = null;

        System.gc();

      }
    } finally {

      // Required or test decks which run many momls in
      // one VM will fail horribly.
      MoMLParser.purgeModelRecord(modelPath);
      MoMLParser.purgeAllModelRecords();
      logger.info("End model : " + modelPath);
      final long end = System.currentTimeMillis();
      // Did not like the DateFormat version, there may be something better than this.
      final long time = end - start;
      logger.info("Model completed in " + (time / (60 * 1000)) + "m " + ((time / 1000) % 60) + "s " + (time % 1000) + "ms");

      if (doSystemExit) {
        // We have to do this in case daemons are started.
        // We must exit this vm once the model is finished.
        logger.info("Passerelle shut down.");
        System.exit(1);
      }

      if (!exceptions.isEmpty()) {
        throw exceptions.get(0);
      }
    }
  }

  /**
   * Ensures that the manager that runs with the actors has a unique name for every run. This is one way that an actor can know which runner they are dealing
   * with and clear caches if required.
   * 
   * @return
   */
  private String getUniqueName() {
    return "Model_" + System.currentTimeMillis();
  }

  private void notifyModelChangeStart() {

    try {
      final IConfigurationElement[] ele = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.engine.model.listener");
      for (IConfigurationElement i : ele) {
        final IModelListener l = (IModelListener) i.createExecutableExtension("modelListener");
        l.executionStarted();
      }

    } catch (Exception ne) {
      logger.error("Cannot notify model listeners");
    }
  }

  private void notifyModelChangeEnd(final int returnCode) {

    try {
      final IConfigurationElement[] ele = Platform.getExtensionRegistry().getConfigurationElementsFor("com.isencia.passerelle.engine.model.listener");
      for (IConfigurationElement i : ele) {
        final IModelListener l = (IModelListener) i.createExecutableExtension("modelListener");
        l.executionTerminated(returnCode);
      }

    } catch (Exception ne) {
      logger.error("Cannot notify model listeners");
    }
  }

  public static void main(String[] args) throws Throwable {
    String model = null;
    // The model is specified with argument -model moml_file
    if (args == null)
      return;

    for (int i = 0; i < args.length; i++) {
      if (i > 0 && "-model".equals(args[i - 1])) {
        model = args[i];
        break;
      }
    }

    final ModelRunner runner = new ModelRunner();
    runner.runModel(model, true);
  }

}
