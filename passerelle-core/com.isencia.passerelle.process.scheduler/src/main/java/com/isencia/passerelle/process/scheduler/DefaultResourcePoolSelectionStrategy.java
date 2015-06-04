/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import com.isencia.passerelle.process.model.Task;


/**
 * @author puidir
 *
 */
public class DefaultResourcePoolSelectionStrategy implements ResourceSelectionStrategy {

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.ResourcePoolSelectionStrategy#getResourcePoolNameForEntity(com.isencia.passerelle.diagnosis.LifeCycleEntity, java.lang.String)
	 */
	public String getResourcePoolName(final Task task, String defaultName) {
		// The default implementation doesn't do any segmentation of resources
		return defaultName;
	}

}
