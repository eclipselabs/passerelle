package com.isencia.passerelle.process.scheduler;

import java.util.Properties;

import com.isencia.passerelle.process.scheduler.congestionmanagement.CongestionManagementTaskScheduler;

/**
 * A factory to create TaskScheduler instances.
 * <br><br>
 * The main "reason d'etre" for this factory is to be able to combine Java 5 generics
 * with OSGi services. We would like to be able to obtain Scheduler instances
 * for dedicated request types. This only seems possible via a factory, registered
 * as an OSGi service, that offers a generic-ized scheduler construction method...
 *
 * @author id806607
 *
 */
public interface TaskSchedulerFactory {

	/**
	 * Root node for all Relative Capacity Agreements that will define the Task Classifier Strategies.
	 */
	String SCHEDULER_PROP_RCA_NODE = "RCA";

	/**
	 * Root node for all Schedulers that will be started together with this plugin
	 */
	String SCHEDULER_PROP_SCHEDULERS_NODE = "schedulers";

	/**
	 * Property to define the nr of resources that must be maintained for a
	 * capacity management based scheduler.
	 * <br>
	 * I.e. if this property is given,  a scheduler able to manage constrained resources
	 * will be returned.
	 */
	String SCHEDULER_PROP_RESOURCE_COUNT = "nr.resources";
	
	/**
	 * Property to define the max amount of requests that can be kept waiting in the scheduler.
	 * When the max is already reached, any new requests delivered are refused by the scheduler.
	 * <br>
	 * When this property is undefined or negative, the scheduler keeps on accepting all requests
	 * without limit.
	 */
	String SCHEDULER_PROP_MAX_WAITING_COUNT = "max.wait.count";

	/**
	 * Assumes that it's about scheduling requests for constrained resources,
	 * i.e. SCHEDULER_PROP_RESOURCE_COUNT will be set, and returns
	 * the matching scheduler interface type.
	 *
	 * @param <T>
	 * @param schedulerName
	 * @param props
	 * @return
	 * @throws IllegalArgumentException
	 */
	CongestionManagementTaskScheduler createCongestionManagementScheduler(String schedulerName, Properties props);

}
