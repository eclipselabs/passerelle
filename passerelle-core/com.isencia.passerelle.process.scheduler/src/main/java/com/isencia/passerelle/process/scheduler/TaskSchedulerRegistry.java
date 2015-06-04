/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import java.util.Set;

/**
 * @author puidir
 *
 */
public interface TaskSchedulerRegistry {
	
	TaskScheduler getScheduler(String schedulerName);
	
	TaskScheduler getDefaultScheduler();
	
	Set<String> getRegisteredSchedulerNames();

	void registerScheduler(String schedulerName, TaskScheduler scheduler);
}
