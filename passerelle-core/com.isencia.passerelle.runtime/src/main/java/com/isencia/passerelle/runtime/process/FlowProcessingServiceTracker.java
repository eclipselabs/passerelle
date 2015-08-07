package com.isencia.passerelle.runtime.process;


public class FlowProcessingServiceTracker {
  private static FlowProcessingService SERVICE = null;
  
  public static FlowProcessingService getService() {
    return(SERVICE);
  }
  
  public static void setService(FlowProcessingService service) {
    SERVICE = service;
  }
}
