package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.net.URL;
import javax.management.MBeanServerConnection;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

/**
 * This action is run on the workbench and starts the workflow. The workflow notifies with a jmx service whose port is defined by the system property
 * "com.isencia.jmx.service.port" before the action is run. It is variable as the workbench will attempt to find a free port for it to connect to the worklow.
 * 
 * @author gerring
 */
@SuppressWarnings("restriction")
public class RunAction extends ExecutionAction implements IEditorActionDelegate, ModelChangeListener {

  private static final Logger logger = LoggerFactory.getLogger(RunAction.class);

  public RunAction() {
    super();
    setId(getClass().getName());
    setText("Run the workflow from start to end until finished.");
    setImageDescriptor(Activator.getImageDescriptor("/icons/run_workflow.gif"));
    ExecutionAction.addModelChangeListener(this);
  }

  @Override
  public void run() {
    run(this);
  }

  public void run(IAction action) {
    try {
      final IFile config = getModelRunner();
      run(config, true);
    } catch (Exception e) {
      logger.error("Cannot read configuration", e);
    }
  }

  public void run(IFile config, boolean fromEditor) {

    IResource selection = config;
    try {

      if (fromEditor) {
        // We will ensure console view is selected
        final IWorkbenchPage page = EclipseUtils.getPage();
        page.showView("org.eclipse.ui.console.ConsoleView");

        final boolean didSave = EclipseUtils.getPage().saveAllEditors(true);
        if (!didSave)
          return;

        final IEditorPart editor = EclipseUtils.getPage().getActiveEditor();
        if (editor != null)
          EclipseUtils.getPage().activate(editor);

        // Make sure that the current editor is the selected resource.
        selection = getSelectedResource();
        if (selection instanceof IFile)
          EclipseUtils.openEditor((IFile) selection);

        // Select any editor which is PasserelleModelMultiPageEditor.ID
        if (selection == null && editor == null) {
          final IEditorReference[] refs = EclipseUtils.getPage().getEditorReferences();
          for (int i = 0; i < refs.length; i++) {
            if (refs[i].getId().equals(PasserelleModelMultiPageEditor.ID)) {
              EclipseUtils.getPage().activate(refs[i].getEditor(true));
              break;
            }
          }
        }

        // Clear selection.
        if (editor != null && editor instanceof PasserelleModelMultiPageEditor) {
          ((PasserelleModelMultiPageEditor) editor).setActorSelected(null, false, -1);
        }

        // Save the current workspace.
        saveWorkSpace();
      }

      selection = getMomlFileForPdmlFile(selection);
      fireRunListeners();

      if (System.getProperty("eclipse.debug.session") != null) {

        final IResource sel = selection;
        final Job job = new Job("Run workflow debug mode") {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            final ModelRunner runner = new ModelRunner();
            try {
              runner.runModel(sel.getLocation().toOSString(), false);
            } catch (Throwable e) {
              logger.error("Error during workflow execution", e);
            }
            return Status.OK_STATUS;
          }
        };
        job.setUser(false);
        job.setPriority(Job.LONG);
        job.schedule();
        updateActionsAvailable(200);

      } else { // Normally the case in real application
        WorkflowLaunchConfiguration configuration = new WorkflowLaunchConfiguration(config);
        configuration.addSystemProperty("com.isencia.jmx.service.port");
        // TODO Find an alternative way to maintain a "editor/session" key
        configuration.addSystemProperty("com.isencia.require.file.source");
        configuration.addSystemProperty("workflow.logback.configurationFile");
        // DO NOT CHANGE THIS. IF THESE PROPERTIES ARE NOT PASSED TO THE WORKFLOW,
        // IT DOES NOT WORK, THE JMX SERVICE WILL BE BROKEN

        // add actor bundles to launch configuration through discovery
        // of the registered actor class providers
        boolean addedActorBundles = false;
        String configuredBundles = configuration.getAttribute("selected_target_plugins", "");
        StringBuilder pluginsToLaunch = new StringBuilder(configuredBundles);
        ServiceReference<?>[] serviceReferences = Activator.getDefault().getBundleContext().getServiceReferences(ModelElementClassProvider.class.getName(), null);
        for (ServiceReference<?> serviceReference : serviceReferences) {
          Bundle bundle = serviceReference.getBundle();
          String bundleName = bundle.getSymbolicName();
          if (configuredBundles.indexOf(bundleName) < 0) {
            pluginsToLaunch.append("," + bundleName + "@default:default");
            addedActorBundles = true;
          }
        }
        ILaunchConfiguration theOne = configuration;
        if (addedActorBundles) {
          ILaunchConfigurationWorkingCopy cfgWorkingCopy = configuration.getWorkingCopy();
          cfgWorkingCopy.setAttribute("selected_target_plugins", pluginsToLaunch.toString());
          theOne = cfgWorkingCopy.doSave();
        }
        DebugUITools.launch(theOne, ILaunchManager.RUN_MODE);
      }

    } catch (Exception e) {
      logger.error("Cannot read configuration", e);
      updateActionsAvailable(500);
    }

  }

  public void executionStarted(ModelChangeEvent evt) {
    updateActionsAvailable(100);
  }

  public void executionTerminated(ModelChangeEvent evt) {
    updateActionsAvailable(500);
  }

  private void saveWorkSpace() throws CoreException {

    IWorkspace ws = ResourcesPlugin.getWorkspace();
    ws.save(true, new NullProgressMonitor());

  }

  public boolean isEnabled() {
    if (System.getProperty("eclipse.debug.session") != null) {
      return ModelRunner.getRunningInstance() == null;
    }
    try {
      final MBeanServerConnection server = RemoteManagerAgent.getServerConnection(100);
      return server.getObjectInstance(RemoteManagerAgent.REMOTE_MANAGER) == null;
    } catch (Throwable e) {
      return true;
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {

  }

  public IFile getModelRunner() throws Exception {

    // if the bundle is not ready then there is no image
    Bundle bundle = Activator.getDefault().getBundle();

    // look for the image (this will check both the plugin and fragment folders
    URL fullPathString = BundleUtility.find(bundle, "ModelRunner.txt");
    final IResource sel = getSelectedResource();
    final IFile file = sel.getProject().getFile("WorkflowConfiguration.launch");
    if (file.exists())
      return file;
    file.create(fullPathString.openStream(), true, null);
    file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    return file;
  }

  private IResource getSelectedResource() {
    final ISelection sel = EclipseUtils.getPage().getSelection();
    if (sel != null && sel instanceof IStructuredSelection) {
      final IStructuredSelection str = (IStructuredSelection) sel;
      final Object res = str.getFirstElement();
      if (res instanceof IFile) {
        final IFile file = (IFile) res;
        if (file.getName().toLowerCase().endsWith(".moml") || file.getName().toLowerCase().endsWith(".pdml"))
          return file;
      }
    }
    final IFile file = EclipseUtils.getIFile(EclipseUtils.getPage().getActiveEditor().getEditorInput());
    if (file.getName().toLowerCase().endsWith(".moml") || file.getName().toLowerCase().endsWith(".pdml")) {
      return file;
    }
    return null;
  }

  public static IResource getMomlFileForPdmlFile(IResource file) {
    if (file.getName().toLowerCase().endsWith(".moml")) {
      // the joker is giving us the moml file i.o. a pdml file!
      // just give it back then
      return file;
    } else {
      IFile momlFile = null;
      IContainer fileContainer = file.getParent();
      String diagramFileName = file.getName();
      String momlFileName = diagramFileName.substring(0, diagramFileName.lastIndexOf(".")) + ".moml";
      if (fileContainer instanceof IFolder) {
        momlFile = ((IFolder) fileContainer).getFile(momlFileName);
      } else if (fileContainer instanceof IProject) {
        momlFile = ((IProject) fileContainer).getFile(momlFileName);
      } else {
        throw new RuntimeException("Unable to get MOML file : Diagram file " + file + " not in a IFolder nor in a IProject");
      }
      return momlFile;
    }
  }

  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    // TODO Auto-generated method stub

  }
}
