package com.isencia.passerelle.editor.common.model;

import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentRelation;

public interface LinkHolder {
  void generateLinks(CompositeActor modelDiagram);
  void removeLink(Link link);
  Set<Link> getLinks(Object o); 
  Link generateLink(ComponentRelation relation, Object source, Object target);
  void registerLink(Link link);
}
