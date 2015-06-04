package com.isencia.passerelle.process.service;

public class ProcessManagerServiceTracker {
	private static ProcessManagerService SERVICE = null;
	
	public static ProcessManagerService getService() {
		return(SERVICE);
	}
	
	public static void setService(ProcessManagerService service) {
		SERVICE = service;
	}
}
