package com.isencia.passerelle.process.scheduler.impl;

import com.isencia.passerelle.process.scheduler.congestionmanagement.ResourceCongestionDefinitionStrategy;

/**
 * States that resources are becoming congested
 * if at most 1 resource is still free, i.e. if :
 * <code>
 * freeResourceCount<2
 * </code>
 * @author erwin
 *
 */
public class DefaultCongestionDefinitionStrategy implements ResourceCongestionDefinitionStrategy {

	public boolean resourcesAreCongested(int resourceCount, int freeResourceCount) {
		return freeResourceCount < 2;
	}

}
