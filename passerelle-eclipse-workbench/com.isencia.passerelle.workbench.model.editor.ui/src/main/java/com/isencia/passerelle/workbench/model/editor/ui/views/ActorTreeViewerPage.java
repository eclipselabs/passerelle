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

import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.GridData;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import com.isencia.passerelle.editor.common.model.PaletteGroup;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.palette.DefaultDragSupportBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.DragSupportBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

/**
 * This class represents the tree view page of the data view
 */
public class ActorTreeViewerPage extends ActorPalettePage implements IAdaptable {

  private ActionRegistry actionRegistry;
  private FilteredTree tree;
  private DragSupportBuilder dragSupportBuilder;

  public ActorTreeViewerPage(ActionRegistry actionRegistry, DragSupportBuilder dragSupportBuilder) {
    super();
    this.actionRegistry = actionRegistry;
    if (dragSupportBuilder != null) {
      this.dragSupportBuilder = dragSupportBuilder;
    } else {
      this.dragSupportBuilder = new DefaultDragSupportBuilder();
    }
  }

  public ActionRegistry getActionRegistry() {
    return actionRegistry;
  }

  /**
   * Creates the tree view
   * 
   * @param parent
   *          the parent
   */
  protected TreeViewer createTreeViewer(Composite parent) {

    PatternFilter filter = new PatternFilter();
    this.tree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    TreeViewer treeViewer = tree.getViewer();
    return treeViewer;
  }

  public Control getControl() {
    return tree;
  }

  /**
   * Initializes the data view page.
   */
  protected void initPage() {
    createContextMenus();
    getTreeViewer().getTree().addTreeListener(new TreeListener() {

      public void treeExpanded(TreeEvent arg0) {
        // TODO Auto-generated method stub

      }

      public void treeCollapsed(TreeEvent arg0) {
        // TODO Auto-generated method stub

      }
    });
    getTreeViewer().getTree().addMouseTrackListener(new MouseTrackAdapter() {

      public void mouseHover(MouseEvent event) {
        Widget widget = event.widget;
        if (widget == getTreeViewer().getTree()) {
          Point pt = new Point(event.x, event.y);
          TreeItem item = getTreeViewer().getTree().getItem(pt);
          getTreeViewer().getTree().setToolTipText(getTooltip(item));
        }
      }
    });
    PaletteBuilder builder = PaletteBuilder.getInstance();

    final List<PaletteGroup> grps = builder.getRootPaletteGroups();
    for (PaletteGroup grp : grps) {
      if (grp.isExpanded()) {
        getTreeViewer().expandToLevel(grp, 1);
      }
      for (PaletteGroup childGrp : grp.getChildren()) {
        if (childGrp.isExpanded()) {
          getTreeViewer().expandToLevel(childGrp, 1);
        }
      }
    }
  }

  private void createContextMenus() {
    MenuManager menuManager = new ActorTreeMenuProvider(getTreeViewer());

    Menu menu = menuManager.createContextMenu(getTreeViewer().getControl());

    getTreeViewer().getControl().setMenu(menu);
    getSite().registerContextMenu("#Pop up", menuManager, //$NON-NLS-1$
        getSite().getSelectionProvider());//$NON-NLS-1$ 
  }

  private String getTooltip(TreeItem item) {
    if (item != null) {
      Object object = item.getData();
      StringBuffer tooltip = new StringBuffer();
      if (object instanceof PaletteItemDefinition) {
        return ((PaletteItemDefinition) object).getName();
      }
      if (object instanceof PaletteGroup) {
        return ((PaletteGroup) object).getName();
      }
      return "";
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * Configures the tree viewer.
   */
  protected void configTreeViewer() {
    ActorTreeProvider provider = new ActorTreeProvider();

    getTreeViewer().setContentProvider(provider);
    getTreeViewer().setLabelProvider(provider);

    initRoot();

    // add inline renaming support

    // Adds drag and drop support
    dragSupportBuilder.addDragSupport(getTreeViewer());

  }

  /**
   * Initializes the root of the view
   */
  private void initRoot() {
    PaletteBuilder builder = PaletteBuilder.getInstance();
    getTreeViewer().setInput(builder.getRootPaletteGroups().toArray());
  }

  /**
   * The <code>Page</code> implementation of this <code>IPage</code> method disposes of this page's control (if it has one and it has not already been
   * disposed). Disposes the visitor of the element
   */
  public void dispose() {
    super.dispose();
  }

  public void refresh() {
    getTreeViewer().refresh();
  }

  public Object getAdapter(Class type) {
    if (type == String.class) {
      return "Pallette";
    }
    return null;
  }

}