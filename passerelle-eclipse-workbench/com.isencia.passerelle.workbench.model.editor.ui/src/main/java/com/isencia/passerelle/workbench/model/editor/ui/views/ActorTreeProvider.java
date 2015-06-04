/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.editor.common.model.PaletteGroup;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;

/**
 * The provider class used by views
 */

public class ActorTreeProvider implements ITreeContentProvider, ILabelProvider {

  public Object[] getChildren(Object parentElement) {

    if (parentElement instanceof PaletteGroup) {
      SortedSet<PaletteItemDefinition> paletteItems = ((PaletteGroup) parentElement).getPaletteItems();
      Collection<PaletteGroup> groups = ((PaletteGroup) parentElement).getChildren();
      List allItems = new ArrayList();
      allItems.addAll(paletteItems);
      allItems.addAll(groups);
      return allItems.toArray();
    }
    return new Object[] {};
  }

  public Object getParent(Object element) {
    if (element instanceof PaletteItemDefinition) {
      ((PaletteItemDefinition) element).getGroup();
    }
    if (element instanceof PaletteGroup) {
      ((PaletteGroup) element).getParent();
    }
    return null;
  }

  public boolean hasChildren(Object element) {
    // TODO Auto-generated method stub
    return element instanceof PaletteGroup && (((PaletteGroup) element).hasPaletteItems() || ((PaletteGroup) element).hasChildren());
  }

  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof Object[]) {
      return (Object[]) inputElement;

    }
    return getChildren(inputElement);
  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // TODO Auto-generated method stub

  }

  public Image getImage(Object element) {
    if (element instanceof PaletteItemDefinition) {
      ImageDescriptor icon = (ImageDescriptor) ((PaletteItemDefinition) element).getIcon();
      if (icon == null) {
        return ActorEditPart.IMAGE_DESCRIPTOR_ACTOR.createImage();
      }
      return icon.createImage();
    }
    if (element instanceof PaletteGroup) {

      if (((PaletteGroup) element).getIcon() != null) {
        return ((ImageDescriptor) ((PaletteGroup) element).getIcon()).createImage();
      } else {
        Activator.getImageDescriptor("icons/folder.gif").createImage();
      }
    }
    return null;
  }

  public String getText(Object element) {
    if (element instanceof PaletteItemDefinition) {
      return ((PaletteItemDefinition) element).getName();
    }
    if (element instanceof PaletteGroup) {
      return ((PaletteGroup) element).getName();
    }
    return null;
  }

  public void addListener(ILabelProviderListener listener) {

  }

  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  public void removeListener(ILabelProviderListener listener) {
  }

}