package com.isencia.passerelle.workbench.model.jmx;

import javax.management.NotificationBroadcaster;


/**
 * The interface which is deployed 
 * for managing the workflow.
 */
public interface RemoteManagerMBean extends NotificationBroadcaster {
	

	/**
	 * Stops the running manager
	 */
	public void stop();
	
	/**
	 * pauses the running manager
	 */
	public void pause();
	
	/**
	 * Pauses at the next break point and shows the message
	 * when paused.
	 * 
	 * @param breakpointMessage
	 */
	public void pauseOnBreakpoint(String breakpointMessage);
}
