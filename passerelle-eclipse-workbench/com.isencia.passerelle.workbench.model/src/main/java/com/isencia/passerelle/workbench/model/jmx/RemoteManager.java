package com.isencia.passerelle.workbench.model.jmx;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.StandardMBean;

import ptolemy.actor.Manager;

/**
 * Service run by the workflow to allow the RCP workbench
 * to interfact with the service.
 * 
 * Other classes assume the location of this class and it cannot be refactored without
 * searching for the string com.isencia.passerelle.workbench.model.jmx
 * 
 * @author gerring
 *
 */
public class RemoteManager extends StandardMBean implements RemoteManagerMBean {

	protected static final String STOP_CODE        = "ptolemy.actor.Manager.stop";
	protected static final String PAUSE_CODE       = "ptolemy.actor.Manager.pause";
	protected static final String PAUSE_BREAK_CODE = "ptolemy.actor.Manager.pauseOnBreakpoint";
	
	public RemoteManager(final Manager manager) throws NotCompliantMBeanException {
		super(RemoteManagerMBean.class);
		this.manager = manager;
	}

	private Manager manager;
	private NotificationBroadcasterSupport generalBroadcaster;

	protected Manager getManager() {
		return manager;
	}

	protected void setManager(Manager manager) {
		this.manager = manager;
	}
	

	public void stop() {
		if (manager!=null) {
			manager.stop();
			sendNotification(STOP_CODE);
		}
	}
	
	protected void sendNotification(String code) {
		this.sendNotification(code, null);
	}
	
	protected void sendNotification(String code, Object userObject) {
		if (generalBroadcaster!= null) {
			final Notification notification = new Notification(code, this, -1);
			notification.setUserData(userObject);
			generalBroadcaster.sendNotification(notification);
		}	
	}


	public void pause() {
		if (manager!=null) {
			manager.pause();
			sendNotification(PAUSE_CODE);
		}
	}


	public void pauseOnBreakpoint(String breakpointMessage) {
		if (manager!=null) {
		    this.manager.pauseOnBreakpoint(breakpointMessage);
		    sendNotification(PAUSE_BREAK_CODE, breakpointMessage);
		}
	}


	public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
		
		if (generalBroadcaster == null)  generalBroadcaster = new NotificationBroadcasterSupport();
		
		generalBroadcaster.addNotificationListener(listener, filter, handback);
	}


	public MBeanNotificationInfo[] getNotificationInfo() {
		return new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(
						new String[] { STOP_CODE, PAUSE_CODE, PAUSE_BREAK_CODE }, // notif. types
						Notification.class.getName(), // notif. class
						"User Notifications."     // description
				)
		};
	}


	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
		
		if (generalBroadcaster == null) throw new ListenerNotFoundException("No notification listeners registered");
		
		generalBroadcaster.removeNotificationListener(listener);
	}

}
