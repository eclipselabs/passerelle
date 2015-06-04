package com.isencia.passerelle.project.repository.api;

public class RepositoryServiceTracker {
	private static RepositoryService SERVICE = null;
	
	public static RepositoryService getService() {
		return(SERVICE);
	}
	
	public static void setService(RepositoryService service) {
		SERVICE = service;
	}
}
