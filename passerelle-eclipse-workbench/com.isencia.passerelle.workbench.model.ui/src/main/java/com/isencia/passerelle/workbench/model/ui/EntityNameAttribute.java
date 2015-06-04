package com.isencia.passerelle.workbench.model.ui;

import ptolemy.kernel.util.NamedObj;

/**
 * A GeneralAttribute used to return an entity's name.
 * 
 * As an entity's name may change during an editor session, 
 * we must not store its name once at the beginning, 
 * but should read it "live" every time.
 * @author erwin
 *
 */
public class EntityNameAttribute extends GeneralAttribute {

  private NamedObj entity;
  
  public EntityNameAttribute(ATTRIBUTE_TYPE type, NamedObj entity) {
    super(type, entity.getName());
    this.entity = entity;
  }
  
  @Override
  public String getValue() {
    return entity.getName();
  }
}
