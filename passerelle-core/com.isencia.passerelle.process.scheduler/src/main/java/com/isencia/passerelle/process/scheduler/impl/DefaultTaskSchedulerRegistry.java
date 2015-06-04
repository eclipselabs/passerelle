/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.isencia.passerelle.process.scheduler.TaskScheduler;
import com.isencia.passerelle.process.scheduler.TaskSchedulerRegistry;
import com.isencia.passerelle.process.scheduler.congestionmanagement.CongestionManagementTaskScheduler;

/**
 * @author puidir
 *
 */
public class DefaultTaskSchedulerRegistry implements TaskSchedulerRegistry {

	private final Map<String, TaskScheduler> registeredSchedulers = new HashMap<String, TaskScheduler>();
	
	// TODO: left out engineMonitoringService
	
	private CongestionManagementTaskScheduler defaultScheduler;
	
	public DefaultTaskSchedulerRegistry() {
	}
	
	public TaskScheduler getScheduler(String schedulerName) {
		return registeredSchedulers.get(schedulerName);
	}

	public Set<String> getRegisteredSchedulerNames() {
		return Collections.unmodifiableSet(registeredSchedulers.keySet());
	}

	public void registerScheduler(String schedulerName, TaskScheduler scheduler) {
		registeredSchedulers.put(schedulerName, scheduler);
		// TODO: left out: add resource usage reporter on the engine monitoring service
	}

  public TaskScheduler getDefaultScheduler() {
    return defaultScheduler;
  }

  public void setDefaultScheduler(CongestionManagementTaskScheduler defaultScheduler) {
    this.defaultScheduler = defaultScheduler;
  }
	// TODO: left out: engine monitoring service setter
}
