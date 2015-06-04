package com.isencia.passerelle.process.service;

public class RequestProcessingBrokerTracker {
	private static RequestProcessingBroker<?> SERVICE = null;
	
	public static RequestProcessingBroker<?> getService() {
		return(SERVICE);
	}
	
	public static void setService(RequestProcessingBroker<?> service) {
		SERVICE = service;
	}
}
