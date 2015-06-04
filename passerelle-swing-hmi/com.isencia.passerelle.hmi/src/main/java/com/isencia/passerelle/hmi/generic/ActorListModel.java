/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.generic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import com.isencia.passerelle.hmi.ModelUtils;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.NamedObj;

/**
 * Swing list model supporting the panel where the order of the actors in the
 * generic HMI can be defined.
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class ActorListModel extends AbstractListModel {
  private final List<NamedObj> actors = new ArrayList<NamedObj>();
  private final CompositeActor model;

  public ActorListModel(final CompositeActor model) {
    super();
    this.model = model;
  }

  public int getSize() {
    return actors.size();
  }

  /*
   * Return what should be shown in the List view
   * @see javax.swing.ListModel#getElementAt(int)
   */
  public String getElementAt(final int index) {
    return ModelUtils.getFullNameButWithoutModelName(model, actors.get(index));
  }

  public void clear() {
    actors.clear();
    fireContentsChanged(this, 0, getSize());
  }

  public boolean isEmpty() {
    return actors.isEmpty();
  }

  public NamedObj get(final int i) {
    return actors.get(i);
  }

  public boolean add(final NamedObj a) {
    if (a != null) {
      final boolean added = actors.add(a);
      if (added) {
        fireContentsChanged(this, 0, getSize());
      }
      return added;
    }
    return false;
  }

  public boolean remove(final Actor a) {
    final boolean removed = actors.remove(a);
    if (removed) {
      fireContentsChanged(this, 0, getSize());
    }
    return removed;
  }

  public boolean contains(final Object o) {
    return actors.contains(o);
  }

  public boolean containsName(final String name) {
    for (final NamedObj actor : actors) {
      if (ModelUtils.getFullNameButWithoutModelName(model, actor).equals(name)) {
        return true;
      }
    }
    return false;
  }

  public List<NamedObj> getActorList() {
    return new ArrayList<NamedObj>(actors);
  }

  // public Iterator iterator() {
  // return actors.iterator();
  // }

  public void swap(final int index1, final int index2) {
    final NamedObj temp = actors.get(index1);
    actors.set(index1, actors.get(index2));
    actors.set(index2, temp);
  }
}
