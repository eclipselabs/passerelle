/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import com.isencia.passerelle.process.model.Task;


/**
 * A ResourcePoolSelectionStrategy can be used to dynamically select a different
 * resourcePoolName, based on the task being pooled and the request for which it is pooled
 * (through Task.getParentRequest()).
 * 
 * Access to both the flow request and the task allows
 * segmentation of resource pools over both sequence types and task types.
 * 
 * The presence of the task allows runtime configuration of resource pool segmentation 
 * through e.g. an actor parameter.
 *
 * The presence of the flow request allows segmentation over all previous tasks and task results.
 * 
 * With a proper monitoring service, automated resource pool load balancing becomes possible.
 * 
 * @author puidir
 *
 */
public interface ResourceSelectionStrategy {

	String getResourcePoolName(final Task task, String defaultName);
	
}
