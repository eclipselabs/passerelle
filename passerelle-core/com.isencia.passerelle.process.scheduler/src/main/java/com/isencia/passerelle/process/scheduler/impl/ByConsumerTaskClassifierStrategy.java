/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.common.util.PreferenceUtils;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.scheduler.TaskSchedulerFactory;
import com.isencia.passerelle.process.scheduler.activator.Activator;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClass;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClassifierStrategy;

/**
 * The default {@link TaskClassifierStrategy} used for configuring the Scheduler.
 *
 * The configuration is done from the database and is kept in synch with the database.
 * This strategy is based on a Relative Capacity Agreement and allows to have 'preferred' clients
 *
 * @author Davy De Durpel
 * @author "puidir"
 * 
 */
public class ByConsumerTaskClassifierStrategy implements TaskClassifierStrategy, IPreferenceChangeListener {

	private final static Logger LOGGER = LoggerFactory.getLogger(ByConsumerTaskClassifierStrategy.class);
	
	private final Map<String, TaskClass> taskClassMap = new HashMap<String, TaskClass>();
	private final static String DEFAULT_CLASS = "DEFAULT";

	public ByConsumerTaskClassifierStrategy() {
		
		// Load preferences per request
		Preferences pluginNode = PreferenceUtils.getConfigNode().node(Activator.getInstance().getBundleSymbolicName());
		IEclipsePreferences rcaNode = (IEclipsePreferences)pluginNode.node(TaskSchedulerFactory.SCHEDULER_PROP_RCA_NODE);
		
		String[] preferenceKeys = null;
		
		try {
			preferenceKeys = rcaNode.keys();
		} catch (BackingStoreException ex) {
			LOGGER.error(ErrorCode.REQUEST_INIT_ERROR + ": Cannot read preferences (" + ex.getMessage() + ")");
		}
		
		taskClassMap.put(DEFAULT_CLASS, new TaskClass(DEFAULT_CLASS, 10));
		
		if (preferenceKeys == null) {
			// there are no request classes defined so we'll always take a default one
			LOGGER.warn(ErrorCode.REQUEST_INIT_ERROR + ": No RCA preferences defined! Every task will be scheduled through the default scheduler");
		} else {
			for (String key : preferenceKeys) {
				taskClassMap.put(key, new TaskClass(key, rcaNode.getInt(key, 10)));
			}
		}
		
		rcaNode.addPreferenceChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.congestionmanagement.LifeCycleEntityClassifierStrategy#getClassForEntity(com.isencia.passerelle.diagnosis.LifeCycleEntity)
	 */
	public TaskClass getClassForTask(Context context) {

		if (context.getRequest() instanceof Task) {

			TaskClass taskClass = taskClassMap.get(((Task)context.getRequest()).getInitiator());

			if (taskClass == null) {
				return taskClassMap.get(DEFAULT_CLASS);
			} else {
				return taskClass;
			}
		}
		
		return taskClassMap.get(DEFAULT_CLASS);
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();

		if (key == null) {
			return;
		}

		String oldValue = (String)event.getOldValue();
		String newValue = (String)event.getNewValue();

		if (newValue == null) {
			taskClassMap.remove(key);
		} else if (!newValue.equals(oldValue)) {
			taskClassMap.put(key, new TaskClass(key, Integer.parseInt(newValue)));
		}
	}

}
