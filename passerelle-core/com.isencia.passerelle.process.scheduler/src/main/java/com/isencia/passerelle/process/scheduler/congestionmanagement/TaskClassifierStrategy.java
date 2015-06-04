/**
 * 
 */
package com.isencia.passerelle.process.scheduler.congestionmanagement;

import com.isencia.passerelle.process.model.Context;

/**
 * @author "puidir"
 *
 */
public interface TaskClassifierStrategy {

	/**
	 * Determines the TaskClass that must be used for the given task
	 *
	 * @param task some Task
	 * @return the TaskClass that must be used for the given task
	 */
	TaskClass getClassForTask(Context context);
}
