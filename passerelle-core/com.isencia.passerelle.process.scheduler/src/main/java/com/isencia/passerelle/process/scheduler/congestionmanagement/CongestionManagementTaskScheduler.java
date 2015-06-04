/**
 * 
 */
package com.isencia.passerelle.process.scheduler.congestionmanagement;

import com.isencia.passerelle.process.scheduler.TaskScheduler;

/**
 * <p>
 * A RequestScheduler that does capacity assignment
 * across a number of request classes.
 * </p>
 * <p>
 * In order to achieve this, it relies on 2 strategies:
 * <ul>
 * <li> a <code>TaskClassifierStrategy</code> to classify incoming tasks
 * <li> a <code>ResourceCongestionDefinitionStrategy</code> to determine whether
 * the desired resources are getting congested and we need to do some more-or-less
 * intelligent request prioritization.
 * </ul>
 * </p>
 *
 * @author erwin
 *
 */
public interface CongestionManagementTaskScheduler extends TaskScheduler {

	/**
	 *
	 * @param taskClassMapper the one with which this TaskScheduler must be configured
	 */
	void setTaskClassifierStrategy(TaskClassifierStrategy taskClassMapper);
	/**
	 *
	 * @return the currently configured RequestClassifierStrategy
	 */
	TaskClassifierStrategy getTaskClassifierStrategy();

	/**
	 *
	 * @param congestionStrategy the one with which this RequestScheduler must be configured
	 */
	void setResourceCongestionDefinitionStrategy(ResourceCongestionDefinitionStrategy congestionStrategy);

	/**
	 *
	 * @return the currently configured ResourceCongestionDefinitionStrategy
	 */
	ResourceCongestionDefinitionStrategy getResourceCongestionDefinitionStrategy();
}
