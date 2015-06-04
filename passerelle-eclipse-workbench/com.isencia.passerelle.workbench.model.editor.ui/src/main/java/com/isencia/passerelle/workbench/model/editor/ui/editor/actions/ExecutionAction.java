package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public abstract class ExecutionAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutionAction.class);
	
	private List<ExecuteActionListener> executeActionListeners;

	protected void addRefreshListener() throws Exception {
		addRefreshListener(null);
	}
	/**
	 * Might try to add listener
	 * @throws Exception
	 */
	protected void addRefreshListener(MBeanServerConnection client) throws Exception {
		try {
			if (client==null) client = RemoteManagerAgent.getServerConnection(5000);
			logger.debug("Client connected = "+client);
			if (!client.isRegistered(RemoteManagerAgent.REMOTE_MANAGER)) return;
			logger.debug("Adding notification listener");
			client.addNotificationListener(RemoteManagerAgent.REMOTE_MANAGER, createRefreshListener(), null, this);
			logger.debug("Added notification listener");
		} catch (Exception e) {
			logger.error("Cannot add listener", e);
		}
	}

	private NotificationListener createRefreshListener() {
		return new NotificationListener() {		

			public void handleNotification(Notification notification, Object handback) {
				updateActionsAvailable(1000);
			}
		};
	}
	
	private boolean buttonUpdateAllowed = true;
	
	protected void updateActionsAvailable(final long delay) {
		
		if (!buttonUpdateAllowed) return;
		
		final Job updateActionBars = new Job("Update action bars") {
			@Override
			public IStatus run(IProgressMonitor mon) {
				final IEditorPart editor = EclipseUtils.getPage().getActiveEditor();
				if (editor!=null) {
					final SubActionBars2 bars = (SubActionBars2)editor.getEditorSite().getActionBars();
					logger.debug("Doing refresh of toolbar actions.");
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

						public void run() {
							// We try twice to refresh the buttons
							try {
								buttonUpdateAllowed=false;
								bars.deactivate();
								bars.activate(true);
								fireButtonRefreshListeners();
							} finally {
								buttonUpdateAllowed=true;
							}
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		updateActionBars.setUser(false);
		updateActionBars.setSystem(true);
		updateActionBars.schedule(delay);
	}
	
	public void addExecuteActionListener(final ExecuteActionListener l) {
		
		if (executeActionListeners==null) executeActionListeners = new ArrayList<ExecuteActionListener>(3);
		executeActionListeners.add(l);
	}
	
	protected void fireButtonRefreshListeners() {
		if (executeActionListeners==null) return;
		final ExecuteActionEvent evt = new ExecuteActionEvent(this, isEnabled());
		for (ExecuteActionListener l : executeActionListeners) {
			l.buttonRefreshRequested(evt);
		}
	}
	
	protected void fireRunListeners() {
		if (executeActionListeners==null) return;
		final ExecuteActionEvent evt = new ExecuteActionEvent(this, false);
		for (ExecuteActionListener l : executeActionListeners) {
			l.executionRequested(evt);
		}
	}
	
	protected void fireStopListeners() {
		if (executeActionListeners==null) return;
		final ExecuteActionEvent evt = new ExecuteActionEvent(this, false);
		for (ExecuteActionListener l : executeActionListeners) {
			l.stopRequested(evt);
		}
	}

	public void removeExecuteActionListener(ExecuteActionListener l) {
		if (executeActionListeners==null) return;
		executeActionListeners.remove(l);
	}
	
	/**
	 * Memory leak but there should not be too many of them.
	 */
	private static Set<ModelChangeListener> modelChangeListeners;

	/**
	 * Notifies all the static listeners of termination then
	 * clears the list. You can only listen until termination not
	 * forever or a memory leak would occur.
	 * 
	 * @param returnCode
	 */
	public static void notifyExecutionTerminated(final int returnCode) {
		if (modelChangeListeners==null) return;
		for (ModelChangeListener l : modelChangeListeners) {
		    l.executionTerminated(new ModelChangeEvent(modelChangeListeners, returnCode));
		}
	}
	public static void notifyExecutionStarted() {
		if (modelChangeListeners==null) return;
		for (ModelChangeListener l : modelChangeListeners) {
		    l.executionStarted(new ModelChangeEvent(modelChangeListeners));
		}
	}
	
	public static void addModelChangeListener(final ModelChangeListener l) {
		if (modelChangeListeners==null) modelChangeListeners = new HashSet<ModelChangeListener>(3);
		modelChangeListeners.add(l);
	}

}
