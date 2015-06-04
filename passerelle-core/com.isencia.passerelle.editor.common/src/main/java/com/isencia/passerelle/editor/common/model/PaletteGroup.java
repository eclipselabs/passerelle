package com.isencia.passerelle.editor.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class PaletteGroup implements Serializable, Comparable<PaletteGroup> {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PaletteGroup)) {
      return false;
    }
    PaletteGroup group = (PaletteGroup) obj;
    return id.equals(group.getId());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  private Map<String, PaletteItemDefinition> paletteItemMap = new HashMap<String, PaletteItemDefinition>();
  private SortedSet<PaletteItemDefinition> paletteItems = new TreeSet<PaletteItemDefinition>();
  private List<PaletteGroup> children = new ArrayList<PaletteGroup>();
  private boolean secure;

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public boolean isVisible() {
    return true;
  }

  public Collection<PaletteGroup> getChildren() {
    Collections.sort(children);
    return children;
  }

  public void addChild(PaletteGroup child) {
    if (children == null) {
      children = new ArrayList<PaletteGroup>();
    }
    children.add(child);
  }

  public void removePaletteItem(PaletteItemDefinition child) {

    if (paletteItems != null) {
      paletteItems.remove(child);
    }

  }

  /**
   * Retrieve all the leaf names under this group
   * 
   * @param deep
   *          Whether to nest into subgroups or not
   * @return A list of all leaf names
   */
  public List<String> getLeafNames(boolean deep) {
    List<String> leafNames = new ArrayList<String>();
    getLeafNames(leafNames, deep, this);
    return leafNames;
  }

  /**
   * Recursively retrieve all the leaf names under the given group
   * 
   * @param leafNames
   *          Names collected thus far
   * @param deep
   *          Whether to nest into subgroups or not
   * @param group
   *          The group to collect leave names off
   */
  private void getLeafNames(List<String> leafNames, boolean deep, PaletteGroup group) {
    for (PaletteItemDefinition paletteItem : group.getPaletteItems()) {
      leafNames.add(paletteItem.getName());
    }

    if (deep) {
      for (PaletteGroup subGroup : group.getChildren()) {
        getLeafNames(leafNames, deep, subGroup);
      }
    }
  }

  private PaletteGroup parent;

  public PaletteGroup getParent() {
    return parent;
  }

  private String parentId;

  public void init(PaletteBuilder builder) {
    if (parentId == null) {
      return;
    }
    if (parent == null) {
      parent = builder.getPaletteGroup(parentId);
      parent.addChild(this);
    }

  }

  public void setParent(PaletteGroup parent) {
    this.parent = parent;
  }

  public SortedSet<PaletteItemDefinition> getPaletteItems() {
    return paletteItems;
  }

  public boolean hasPaletteItems() {
    if (paletteItems == null) {
      return false;
    }
    return paletteItems.size() > 0;
  }

  public boolean hasChildren() {
    if (children == null) {
      return false;
    }
    return children.size() > 0;
  }

  private boolean expanded;

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
  }

  private int priority = 0;

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  private String id;
  private String name;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PaletteGroup(String id, String name, String parentId) {
    super();
    this.id = id;
    this.name = name;
    this.parentId = parentId;
  }

  public void addPaletteItem(PaletteItemDefinition item) {
    paletteItemMap.put(item.getId(), item);
    paletteItems.add(item);
  }

  public PaletteItemDefinition getPaletteItem(String id) {
    return paletteItemMap.get(id);
  }

  private Object icon;

  public Object getIcon() {
    return icon;
  }

  public void setIcon(Object icon) {
    this.icon = icon;
  }

  public int compareTo(PaletteGroup arg0) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;
    if (this.getParent() == null) {
      if (arg0.getParent() != null) {
        return BEFORE;
      }
    }
    if (this.getParent() != null) {
      if (arg0.getParent() == null) {
        return AFTER;
      }
    }
    if (this.priority != arg0.getPriority()) {
      return -(new Integer(this.priority).compareTo(arg0.getPriority()));
    }

    return this.name.compareTo(arg0.getName());

  }
}
