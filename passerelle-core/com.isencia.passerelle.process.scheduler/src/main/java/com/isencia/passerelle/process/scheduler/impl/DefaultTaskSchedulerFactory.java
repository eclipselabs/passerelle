package com.isencia.passerelle.process.scheduler.impl;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.util.PreferenceUtils;
import com.isencia.passerelle.process.scheduler.TaskSchedulerFactory;
import com.isencia.passerelle.process.scheduler.activator.Activator;
import com.isencia.passerelle.process.scheduler.congestionmanagement.CongestionManagementTaskScheduler;

/**
 * Creates task schedulers from preference entries
 * 
 * @author erwin
 *
 */
public class DefaultTaskSchedulerFactory implements TaskSchedulerFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskSchedulerFactory.class);

	// default is unlimited
	public static final String DEFAULT_MAX_WAITING_COUNT = "-1";
	
	// Max 10 threads per scheduler, by default
	public static final String DEFAULT_MAX_RESOURCE_COUNT = "10";
	
	private String defaultMaxResourceCount;
	private String defaultMaxWaitingCount;
	
	public DefaultTaskSchedulerFactory() {
		
		// The 'schedulers' node contains the default values + a child node per scheduler
		IEclipsePreferences schedulersNode = (IEclipsePreferences)PreferenceUtils.getConfigNode().node(Activator.getInstance().getBundleSymbolicName()).node(SCHEDULER_PROP_SCHEDULERS_NODE);

		// Read the defaults
		defaultMaxResourceCount = schedulersNode.get(SCHEDULER_PROP_RESOURCE_COUNT, DEFAULT_MAX_RESOURCE_COUNT);
		defaultMaxWaitingCount = schedulersNode.get(SCHEDULER_PROP_MAX_WAITING_COUNT, DEFAULT_MAX_WAITING_COUNT);
		
		DefaultScheduler defaultScheduler = new DefaultScheduler(
        "DEFAULT", 
        Integer.parseInt(defaultMaxResourceCount), 
        Integer.parseInt(defaultMaxWaitingCount));
    defaultScheduler.setTaskClassifierStrategy(new ByConsumerTaskClassifierStrategy());
    ((DefaultTaskSchedulerRegistry)Activator.getInstance().getSchedulerRegistry()).setDefaultScheduler(defaultScheduler);

		// Create scheduler per child node
		try {
			for (String schedulerName : schedulersNode.childrenNames()) {
				createSchedulerFromPrefNode(schedulersNode.node(schedulerName));
			}
		} catch (BackingStoreException e) {
			LOGGER.error(ErrorCode.REQUEST_INIT_ERROR + ": Cannot read preferences (" + e.getMessage() + ")");
		}
		
		// Listen for changes on both the defaults and the children
		schedulersNode.addPreferenceChangeListener(new SchedulerDefaultsChangeListener());
		schedulersNode.addNodeChangeListener(new SchedulerAdditionListener());
	}
	
	public String getDefaultMaxResourceCount() {
    return defaultMaxResourceCount;
  }

  public String getDefaultMaxWaitingCount() {
    return defaultMaxWaitingCount;
  }

  private void createSchedulerFromPrefNode(Preferences node) {
		Properties props = new Properties();
		props.setProperty(SCHEDULER_PROP_RESOURCE_COUNT, node.get(SCHEDULER_PROP_RESOURCE_COUNT, defaultMaxResourceCount));
		props.setProperty(SCHEDULER_PROP_MAX_WAITING_COUNT, node.get(SCHEDULER_PROP_MAX_WAITING_COUNT, defaultMaxWaitingCount));

		createCongestionManagementScheduler(node.name(), props);
	}

	public CongestionManagementTaskScheduler createCongestionManagementScheduler(
			String schedulerName, Properties props) {

		// Return existing one if it already exists
		if (Activator.getInstance().getSchedulerRegistry().getRegisteredSchedulerNames().contains(schedulerName)) {
			return (CongestionManagementTaskScheduler)Activator.getInstance().getSchedulerRegistry().getScheduler(schedulerName);
		}

		int resourceCount = 0;
		int maxWaitCount = -1;
		
		String resourceCountStr = props.getProperty(TaskSchedulerFactory.SCHEDULER_PROP_RESOURCE_COUNT);
		String maxWaitCountStr = props.getProperty(TaskSchedulerFactory.SCHEDULER_PROP_MAX_WAITING_COUNT);
		
		try {
			resourceCount = Integer.parseInt(resourceCountStr);
			if (StringUtils.isNotBlank(maxWaitCountStr) && StringUtils.isNumericSpace(maxWaitCountStr)) {
				maxWaitCount = Integer.parseInt(maxWaitCountStr);
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException(props.toString(), ex);
		}

		CongestionManagementTaskScheduler scheduler = new DefaultScheduler(schedulerName, resourceCount, maxWaitCount);
		scheduler.setTaskClassifierStrategy(new ByConsumerTaskClassifierStrategy());

		LOGGER.info("Scheduler " + schedulerName + " created with " + resourceCountStr + " resources with a max waiting count of " + maxWaitCountStr);

		Activator.getInstance().getSchedulerRegistry().registerScheduler(schedulerName, scheduler);
		
		return scheduler;
	}

	private class SchedulerAdditionListener implements INodeChangeListener {

		public void added(NodeChangeEvent event) {
			createSchedulerFromPrefNode(event.getChild());
		}

		public void removed(NodeChangeEvent event) {
			// ignore, not allowed to disable/remove a scheduler while application is active
			// removed schedulers will only be really removed by restarting
			LOGGER.warn("Scheduler " + event.getChild().name() + " preferences removed from DB.\nChange will only be effective after restart");
		}
	}
	
	private class SchedulerDefaultsChangeListener implements IPreferenceChangeListener {

		public void preferenceChange(PreferenceChangeEvent event) {
			String key = event.getKey();

			if (key == null) {
				return;
			}

			String oldValue = (String) event.getOldValue();
			String newValue = (String) event.getNewValue();

			if(SCHEDULER_PROP_RESOURCE_COUNT.equals(key)) {
				if (newValue == null) {
					// reset to default
					defaultMaxResourceCount = DEFAULT_MAX_RESOURCE_COUNT;
				} else if (!newValue.equals(oldValue)) {
					// set new value
					defaultMaxResourceCount = newValue;
				}
				LOGGER.info("Scheduler preference update - setting default resource count to "+defaultMaxResourceCount);
			} else if(SCHEDULER_PROP_MAX_WAITING_COUNT.equals(key)) {
				if (newValue == null) {
					// reset to default
					defaultMaxWaitingCount = DEFAULT_MAX_WAITING_COUNT;
				} else if (!newValue.equals(oldValue)) {
					// set new value
					defaultMaxWaitingCount = newValue;
				}
				LOGGER.info("Scheduler preference update - setting default max waiting count to "+defaultMaxWaitingCount);
			}
		}		
	}
}
