/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package com.isencia.passerelle.workbench.model.editor.ui.views;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.isencia.passerelle.editor.common.model.PaletteGroup;
import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CreateSubModelAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteSubmodelAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.EditSubmodelAction;

/**
 * This class provides the context menu for the single selection and multiple selection
 * 
 * 
 */
public class ActorTreeMenuProvider extends ContextMenuProvider {

  /**
   * constructor
   * 
   * @param viewer
   *          the viewer
   * @param registry
   *          the registry
   */
  public ActorTreeMenuProvider(ISelectionProvider viewer) {
    super(viewer);
  }

  /**
   * Builds the context menu. Single selection menu and multiple selection menu are created while selecting just single
   * element or multiple elements
   * 
   * 
   * @param menu
   *          the menu
   */
  public void buildContextMenu(IMenuManager menu) {

    menu.add(new Separator(GEFActionConstants.GROUP_EDIT));

    // IAction action = getActionRegistry().getAction(
    // ActionFactory.DELETE.getId());
    // if (action != null && action.isEnabled())
    TreeViewer tree = (TreeViewer) getViewer();
    TreeSelection treeSelection = (TreeSelection) tree.getSelection();
    Object element = treeSelection.getFirstElement();
    if (element instanceof SubModelPaletteItemDefinition) {
      createCompositeMenu(menu, element);
    } else if (element instanceof PaletteGroup) {
      if (((PaletteGroup) element).getId().equals("com.isencia.passerelle.actor.actorgroup.submodels")) {
        createCompositeMenu(menu, element);
      }
    }

  }

  private void createCompositeMenu(IMenuManager menu, Object actionOrGroup) {

    CreateSubModelAction emptySubModelAction = new CreateSubModelAction();
    emptySubModelAction.setText("Create new submodel");
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, emptySubModelAction);

    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new EditSubmodelAction(actionOrGroup));
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new DeleteSubmodelAction(actionOrGroup));
  }

}
