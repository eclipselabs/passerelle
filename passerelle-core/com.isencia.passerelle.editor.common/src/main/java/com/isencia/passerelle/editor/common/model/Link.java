package com.isencia.passerelle.editor.common.model;

import ptolemy.kernel.ComponentRelation;

public interface Link {

  public abstract String getTitle();

  public abstract Object getHead();

  public abstract ComponentRelation getRelation();

  public abstract Object getTail();

  public abstract void setHead(Object head);

  public abstract void setRelation(ComponentRelation relation);

  public abstract void setTail(Object tail);


}