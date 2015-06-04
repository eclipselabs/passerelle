package com.isencia.passerelle.process.scheduler.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.scheduler.TaskSchedulerFactory;
import com.isencia.passerelle.process.scheduler.TaskSchedulerRegistry;
import com.isencia.passerelle.process.scheduler.impl.DefaultTaskSchedulerFactory;
import com.isencia.passerelle.process.scheduler.impl.DefaultTaskSchedulerRegistry;

public class Activator implements BundleActivator {
	private final static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private static Activator instance;
	private BundleContext bundleContext;
	private String bundleSymbolicName;

	private ServiceRegistration schedulerFactoryServiceRegistration;
	private ServiceRegistration schedulerRegistryServiceRegistration;

	private TaskSchedulerRegistry defaultSchedulerRegistry;
	private TaskSchedulerFactory defaultSchedulerFactory;

	public static Activator getInstance() {
		return instance;
	}

	public String getBundleSymbolicName() {
		return bundleSymbolicName;
	}

	public TaskSchedulerRegistry getSchedulerRegistry() {
		return defaultSchedulerRegistry;
	}

	public TaskSchedulerFactory getSchedulerFactory() {
		return defaultSchedulerFactory;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		try {
			instance = this;
			this.bundleContext = bundleContext;
			bundleSymbolicName = bundleContext.getBundle().getSymbolicName();

			defaultSchedulerRegistry = new DefaultTaskSchedulerRegistry();
			schedulerRegistryServiceRegistration = bundleContext.registerService(TaskSchedulerRegistry.class.getName(), defaultSchedulerRegistry, null);
			
			defaultSchedulerFactory = new DefaultTaskSchedulerFactory();
			schedulerFactoryServiceRegistration = bundleContext.registerService(TaskSchedulerFactory.class.getName(), defaultSchedulerFactory, null);

		} catch (Exception ex) {
			LOGGER.error(ErrorCode.BUNDLE_START_FAILED + " - " + bundleContext.getBundle().getSymbolicName(), ex);
			throw ex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		instance = null;

		if (schedulerRegistryServiceRegistration != null) {
			schedulerRegistryServiceRegistration.unregister();
			schedulerRegistryServiceRegistration = null;
			defaultSchedulerRegistry = null;
		}

		if (schedulerFactoryServiceRegistration != null) {
			schedulerFactoryServiceRegistration.unregister();
			schedulerFactoryServiceRegistration = null;
			defaultSchedulerFactory = null;
		}
	}

}
