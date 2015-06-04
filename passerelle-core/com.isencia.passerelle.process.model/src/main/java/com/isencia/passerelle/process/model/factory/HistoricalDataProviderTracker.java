package com.isencia.passerelle.process.model.factory;

public class HistoricalDataProviderTracker {
	private static HistoricalDataProvider SERVICE = null;
	
	public static HistoricalDataProvider getService() {
		return(SERVICE);
	}
	
	public static void setService(HistoricalDataProvider service) {
		SERVICE = service;
	}
}
