/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.workbench.model.editor.graphiti.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.graphiti.features.impl.IIndependenceSolver;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

/**
 * A Graphiti "independence solver" provides a (key,object) registry for so-called "business objects". In our case the "business objects" are Passerelle/Ptolemy
 * model elements like actors, ports, parameters etc.
 * <p>
 * A simplistic implementation tried to use the model element's full name as key. This was easy and fast, but breaks when elements get renamed. Graphiti
 * maintains local copies of the keys in its graphical model elements. These become stale after e.g. an actor rename in the Passerelle model, after which
 * invocations of <code>getBusinessObjectForKey</code>
 * </p>
 * <p>
 * The current implementation assigns unmodifiable UUIDs to Passerelle model elements, as <code>StringAttributes</code>. This ensures immutable keys, but
 * implies that upon loading an existing Passerelle model in the editor, the complete model element hierarchy must be visited to register all elements with
 * their UUID in the boMap.
 * </p>
 * 
 * @author erwin
 */
public class PasserelleIndependenceSolver implements IIndependenceSolver {
  public static final String PASS_UUID_ATTR = "__PASS_UUID";
  private CompositeActor topLevel;

  private Map<String, Object> boMap = new HashMap<String, Object>();

  @Override
  public String getKeyForBusinessObject(Object bo) {
    if (bo instanceof NamedObj) {
      NamedObj no = (NamedObj) bo;
      String uuid = getRegisteredUUID(no);
      if (topLevel == null) {
        setTopLevel((CompositeActor) no.toplevel());
      }
      return uuid;
    } else {
      return null;
    }
  }

  private String getRegisteredUUID(NamedObj no) {
    StringAttribute uuidAttr = (StringAttribute) no.getAttribute(PASS_UUID_ATTR);
    if (uuidAttr == null) {
      String uuid = UUID.randomUUID().toString();
      try {
        uuidAttr = new StringAttribute(no, PASS_UUID_ATTR);
        uuidAttr.setExpression(uuid);
      } catch (Exception e) {
        // TODO check exception handling
        throw new RuntimeException(e);
      }
    }
    String uuid = uuidAttr.getExpression();
    if (!boMap.containsKey(uuid)) {
      boMap.put(uuid, no);
    }
    return uuid;
  }

  @Override
  public Object getBusinessObjectForKey(String key) {
    return boMap.get(key);
  }

  public void setTopLevel(CompositeActor topLevel) {
    this.topLevel = topLevel;
    // now we need to visit all children to register them in the boMap...
    Director director = topLevel.getDirector();
    if (director != null) {
      registerDirector(director);
    }
    registerAttributes(topLevel);
    registerEntities(topLevel);
    getRegisteredUUID(topLevel);
  }

  private void registerDirector(Director director) {
    registerAttributes(director);
    getRegisteredUUID(director);
  }

  private void registerEntities(CompositeEntity entityHolder) {
    @SuppressWarnings("unchecked")
    List<Entity> entities = entityHolder.entityList();
    for (Entity e : entities) {
      registerAttributes(e);
      registerPorts(e);
      if (e instanceof CompositeEntity) {
        CompositeEntity ce = (CompositeEntity) e;
        registerEntities(ce);
        registerRelations(ce);
      }
      getRegisteredUUID(e);
    }
  }

  private void registerPorts(Entity portHolder) {
    @SuppressWarnings("unchecked")
    List<Port> ports = portHolder.portList();
    for (Port p : ports) {
      registerAttributes(p);
      getRegisteredUUID(p);
    }
  }

  private void registerRelations(CompositeEntity relationHolder) {
    @SuppressWarnings("unchecked")
    List<Relation> relations = relationHolder.relationList();
    for (Relation r : relations) {
      registerAttributes(r);
      getRegisteredUUID(r);
    }
  }

  private void registerAttributes(NamedObj attrHolder) {
    @SuppressWarnings("unchecked")
    List<Attribute> attrs = attrHolder.attributeList();
    for (Attribute a : attrs) {
      if (!PASS_UUID_ATTR.equals(a.getName())) {
        registerAttributes(a);
        getRegisteredUUID(a);
      }
    }
  }

  public boolean removeBusinessObject(Object bo) {
    if (bo instanceof NamedObj) {
      String uuid = getRegisteredUUID((NamedObj) bo);
      return boMap.remove(uuid) != null;
    }
    return false;
  }
}
