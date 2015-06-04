package com.isencia.passerelle.editor.common.model;

import com.isencia.passerelle.editor.common.activator.Activator;
import com.isencia.passerelle.model.Flow;

public class SubModelPaletteItemDefinition extends PaletteItemDefinition {

  private static final long serialVersionUID = -4103093742276825906L;

  private transient Flow flow;

  public Flow getFlow() {
    if (flow == null) {
      try {
        flow = (Flow) Activator.getDefault().getActorOrientedClassProvider().getActorOrientedClass(getId(), null);
      } catch (ClassNotFoundException e) {

      }
    }
    return flow;
  }

  public SubModelPaletteItemDefinition(Object icon, PaletteGroup group, String id, String name, String color) {
    super(icon, group, id, name, color, null, null, 0);

  }

  public Class getClazz() {

    return Flow.class;
  }

}
