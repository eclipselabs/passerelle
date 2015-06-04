package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import javax.management.MBeanServerConnection;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;

public class StopAction extends ExecutionAction implements IEditorActionDelegate, ModelChangeListener {

  private static final Logger logger = LoggerFactory.getLogger(StopAction.class);

  public StopAction() {
    super();
    setId(getClass().getName());
    setText("Stop the workflow if it is running.");
    setImageDescriptor(Activator.getImageDescriptor("/icons/stop_workflow.gif"));
    ExecutionAction.addModelChangeListener(this);
  }

  @Override
  public void run() {
    run(this);
  }

  public void run(IAction action) {

    try {
      if (System.getProperty("eclipse.debug.session") != null) {
        if (ModelRunner.getRunningInstance()!=null) ModelRunner.getRunningInstance().stop();

      } else {
        final MBeanServerConnection client = RemoteManagerAgent.getServerConnection(2000);
        if (client != null) {
          addRefreshListener(client);
          client.invoke(RemoteManagerAgent.REMOTE_MANAGER, "stop", null, null);

        }
        // Designed to kill the eclipse launch, something of a hack. The stop method
        final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch iLaunch : launches) {
          if (iLaunch.getLaunchConfiguration() instanceof WorkflowLaunchConfiguration) {
            iLaunch.terminate();
          }
        }
      }
      fireStopListeners();
      updateActionsAvailable(1000);

    } catch (Exception e) {
      logger.error("Cannot read configuration", e);
      updateActionsAvailable(500);
    }

  }

  public boolean isEnabled() {
    if (System.getProperty("eclipse.debug.session") != null) {
      return ModelRunner.getRunningInstance() != null;
    }
    try {
      final MBeanServerConnection client = RemoteManagerAgent.getServerConnection(100);
      return client.getObjectInstance(RemoteManagerAgent.REMOTE_MANAGER) != null;
    } catch (Throwable e) {
      return false;
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {

  }

  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    // TODO Auto-generated method stub

  }

  public void executionStarted(ModelChangeEvent evt) {
    updateActionsAvailable(100);
  }

  public void executionTerminated(ModelChangeEvent evt) {
    updateActionsAvailable(500);
  }

}
