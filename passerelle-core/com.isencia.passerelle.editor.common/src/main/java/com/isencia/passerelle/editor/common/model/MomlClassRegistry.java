package com.isencia.passerelle.editor.common.model;

import java.util.Collection;
import java.util.Collections;

public class MomlClassRegistry {

  private static IMomlClassService service;

  public static void setService(IMomlClassService service) {
    MomlClassRegistry.service = service;
  }

  public static Collection<String> getAllActorClasses() throws Exception {
    if (service == null) {
      throw new Exception("Moml class service defined");
    }
    if (service != null) {
      return service.getAllActorClasses();
    }
    return Collections.EMPTY_LIST;
  }

}
