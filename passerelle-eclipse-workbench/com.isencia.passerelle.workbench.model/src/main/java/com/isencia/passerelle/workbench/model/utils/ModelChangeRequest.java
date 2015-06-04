package com.isencia.passerelle.workbench.model.utils;

import com.isencia.passerelle.editor.common.model.Link;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

public abstract class ModelChangeRequest extends ChangeRequest {

  private final Class<?> type;
  private NamedObj child;
  private NamedObj container;

  public Link getLink() {
    return link;
  }

  private Link link;

  /**
   * @param link
   *          the link to set
   */
  public void setLink(Link link) {
    this.link = link;
  }

  public NamedObj getChild() {
    return child;
  }

  public String getChildType() {
    if (child != null) {
      return child.getClass().getName();
    }
    return null;
  }

  public ModelChangeRequest(Class<?> type, Object source, String description, NamedObj child) {
    super(source, description);
    this.type = type;
    this.child = child;
    if (child != null)
      this.container = child.getContainer();
  }

  public NamedObj getContainer() {
    if (child != null && child.getContainer() != null) {
      return child.getContainer();
    }
    return container;
  }

  public void setChild(NamedObj child) {
    this.child = child;
  }

  public ModelChangeRequest(Class<?> type, Object source, String description) {
    super(source, description);
    this.type = type;

  }

  public Class<?> getType() {
    return type;
  }

}
