package com.isencia.passerelle.process.model.factory;

public class ProcessFactoryTracker {
	private static ProcessFactory SERVICE = null;
	
	public static ProcessFactory getService() {
		return(SERVICE);
	}
	
	public static void setService(ProcessFactory service) {
		SERVICE = service;
	}
}
