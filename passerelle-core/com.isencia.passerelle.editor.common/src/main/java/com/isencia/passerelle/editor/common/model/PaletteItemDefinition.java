package com.isencia.passerelle.editor.common.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.editor.common.utils.EditorUtils;

public class PaletteItemDefinition implements Serializable, Comparable<PaletteItemDefinition> {

  private static final long serialVersionUID = -7540471811877075040L;

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PaletteItemDefinition)) {
      return false;
    }
    PaletteItemDefinition def = (PaletteItemDefinition) obj;
    return def.getClazz().equals(getClazz()) && def.getId().equals(getId());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getClazz()).append(id).hashCode();
  }

  public PaletteItemDefinition(Object icon, PaletteGroup group, String id, String name, String color, Class clazz, String bundleId, int priority) {
    this.group = group;
    this.id = id;
    this.icon = icon;
    this.name = name;
    this.priority = priority;
    this.bundleId = bundleId;
    if (clazz != null) {
      this.clazz = clazz.getName();
      this.deprecated = clazz.getAnnotation(Deprecated.class) != null;
    }
    if (group != null)
      group.addPaletteItem(this);
    if (color != null && !color.contains("rgb")) {
      StringBuffer sb = new StringBuffer("rgb(");
      sb.append(color);
      sb.append(")");
      this.color = sb.toString();
    } else {
      this.color = color;
    }

  }

  private boolean deprecated;

  public boolean isDeprecated() {
    if (deprecatedMessage != null) {
      return true;
    }
    return deprecated;
  }

  public void setDeprecated(boolean deprecated) {
    this.deprecated = deprecated;
  }

  private String deprecatedMessage;

  public String getDeprecatedMessage() {
    return deprecatedMessage;
  }

  public void setDeprecatedMessage(String deprecated) {
    this.deprecatedMessage = deprecated;
  }

  private String bundleId;

  public String getBundleId() {
    return bundleId;
  }

  private Object icon;

  public Object getIcon() {
    return icon;
  }

  public void setIcon(Object icon) {
    this.icon = icon;
  }

  private String helpUrl;

  public String getHelpUrl() {
    return helpUrl;
  }

  public void setHelpUrl(String helpUrl) {
    this.helpUrl = helpUrl;
  }

  private PaletteGroup group;

  public PaletteGroup getGroup() {
    return group;
  }

  public PaletteGroup getTopGroup() {
    return getTopGroup(group);
  }

  private PaletteGroup getTopGroup(PaletteGroup group) {
    if (group == null) {
      return null;
    }
    if (group.getParent() == null)
      return group;
    return getTopGroup(group.getParent());
  }

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

  public Class getClazz() {

    return EditorUtils.loadClass(clazz);
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
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
  private String color;
  private String clazz;
  private String width = "100";

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public int compareTo(PaletteItemDefinition arg0) {
    if (this.priority != arg0.getPriority()) {
      return -(new Integer(this.priority).compareTo(arg0.getPriority()));
    }

    if (this.name == null) {
      return 0;
    }
    return this.name.compareTo(arg0.getName());
  }

  @Override
  public String toString() {
    return getId();
  }
}
