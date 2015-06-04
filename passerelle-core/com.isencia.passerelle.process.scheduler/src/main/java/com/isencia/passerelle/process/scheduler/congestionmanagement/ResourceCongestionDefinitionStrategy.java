package com.isencia.passerelle.process.scheduler.congestionmanagement;

/**
 * Implementations must determine when the system should 
 * consider that resources are becoming congested.
 * 
 * @author erwin
 *
 */
public interface ResourceCongestionDefinitionStrategy {

	/**
	 * Determines whether the resources are congested or not,
	 * based on the ratio of total number of resources,
	 * and the currently free resources.
	 * <br>
	 * REMARK : should we make this interface more generic?
	 * I.e. that it's not even fixed that the strategy should be
	 * based on such resource counts???
	 * 
	 * @param resourceCount
	 * @param freeResourceCount
	 * @return
	 */
	boolean resourcesAreCongested(int resourceCount, int freeResourceCount);
}
