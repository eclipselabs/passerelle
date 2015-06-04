/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

import com.isencia.passerelle.process.scheduler.ResourceToken;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClass;

/**
 * @author "puidir"
 *
 */
public class SchedulerAwareResourceToken implements ResourceToken {

	private static final long serialVersionUID = 1L;

	private DefaultScheduler scheduler;
	private TaskClass entityClass;
	
	protected SchedulerAwareResourceToken(DefaultScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	protected void lock(TaskClass entityClass) {
		this.entityClass = entityClass;
	}
	
	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.ResourceToken#release()
	 */
	public void release() {
		scheduler.releaseResourceToken(this, entityClass);
		entityClass = null;
	}

}
