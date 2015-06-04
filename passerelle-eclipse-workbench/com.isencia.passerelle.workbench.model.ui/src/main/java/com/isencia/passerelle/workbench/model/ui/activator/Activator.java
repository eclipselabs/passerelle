package com.isencia.passerelle.workbench.model.ui.activator;

import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.isencia.passerelle.workbench.model.ui";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	public FrameworkLog getFrameWorkLog(){
		FrameworkLog log = _frameworkLogTracker == null ? null : (FrameworkLog) _frameworkLogTracker.getService();
		return log;
	}
	private ServiceTracker _frameworkLogTracker;
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		_frameworkLogTracker = new ServiceTracker(context, FrameworkLog.class.getName(), null);
		_frameworkLogTracker.open();
		if (System.getProperty("passerelle.workbench.version")==null) {
			System.setProperty("passerelle.workbench.version", getBundle().getVersion().toString());
		}
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		//super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
