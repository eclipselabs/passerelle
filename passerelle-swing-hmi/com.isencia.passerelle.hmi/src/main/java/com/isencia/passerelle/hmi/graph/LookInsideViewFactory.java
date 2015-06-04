/*
 * (c) Copyright 2001-2005, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.infonode.tabbedpanel.TabDragEvent;
import net.infonode.tabbedpanel.TabEvent;
import net.infonode.tabbedpanel.TabListener;
import net.infonode.tabbedpanel.TabRemovedEvent;
import net.infonode.tabbedpanel.TabStateChangedEvent;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.titledtab.TitledTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.actor.gui.graph.EditorGraphController.ViewFactory;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;

/**
 * A helper class that reacts to call-backs from Passerelle actor look-inside
 * actions, to open a new view in the IDE. For Netbeans, the views are
 * implemented as ModelGraphTopComponent.
 * 
 * @author erwin.de.ley@isencia.be
 */
public class LookInsideViewFactory implements ViewFactory {

  private final class LookInsideTabListener implements TabListener {

    public void tabSelected(final TabStateChangedEvent event) {

    }

    public void tabRemoved(final TabRemovedEvent event) {
      final TitledTab pan = (TitledTab) event.getTab();
      if (openTabs.containsKey(pan.getText())) {
        openTabs.remove(pan.getText());
      }
      if (pan.getText().equals(parentTab.getText())) {
        // closing parent, so closing childs
        parentTabPanel.removeTabListener(listener);
        for (final Map.Entry<String, TitledTab> element : openTabs.entrySet()) {
          parentTabPanel.removeTab(element.getValue());
        }
        openTabs.clear();
      }
    }

    public void tabMoved(final TabEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabHighlighted(final TabStateChangedEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabDropped(final TabDragEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabDragged(final TabDragEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabDragAborted(final TabEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabDeselected(final TabStateChangedEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabDehighlighted(final TabStateChangedEvent event) {
      // TODO Auto-generated method stub

    }

    public void tabAdded(final TabEvent event) {
      // TODO Auto-generated method stub

    }
  }

  private final static Logger logger = LoggerFactory.getLogger(LookInsideViewFactory.class);

  private final HMIBase hmi;
  private final TabbedPanel parentTabPanel;
  private final PtolemyEffigy graphPanelEffigy;
  final TitledTab parentTab;
  private final Map<String, ModelGraphPanel> loadedModel = new HashMap<String, ModelGraphPanel>();
  private final LookInsideTabListener listener;

  private final Map<String, TitledTab> openTabs = new HashMap<String, TitledTab>();

  /**
	 *
	 */
  public LookInsideViewFactory(final HMIBase hmi, final TabbedPanel parentTabPanel, final TitledTab parentTab, final PtolemyEffigy graphPanelEffigy) {
    this.hmi = hmi;
    this.parentTabPanel = parentTabPanel;
    this.parentTab = parentTab;
    this.graphPanelEffigy = graphPanelEffigy;
    listener = new LookInsideTabListener();
    this.parentTabPanel.addTabListener(listener);
  }

  public void renameView(String oldName, String newName) {
    Set<String> tabKeys = new HashSet<String>(openTabs.keySet());

    for (String tabName : tabKeys) {
      if (tabName.startsWith(oldName)) {
        String newTabName = tabName.replace(oldName, newName);
        TitledTab tabToRename = openTabs.get(tabName);
        tabToRename.setText(newTabName);
        tabToRename.validate();
        openTabs.remove(tabName);
        openTabs.put(newTabName, tabToRename);
      }
    }
  }

  public void openView(final PtolemyEffigy effigy) {
    if (logger.isTraceEnabled()) {
      logger.trace("entry - effigy :" + effigy);
    }

    String fullName = null;
    if (effigy.getModel() != null) {
      fullName = effigy.getModel().getFullName().substring(1);
    } else {
      fullName = effigy.getFullName().substring(1);
    }
    try {

      hmi.disableChangeImpacts();

      ModelGraphPanel panel;

      if (loadedModel.containsKey(fullName)) {
        panel = loadedModel.get(fullName);
      } else {
        panel = new ModelGraphPanel(effigy.getModel(), graphPanelEffigy);
        loadedModel.put(fullName, panel);
        panel.registerViewFactory(this);
      }

      // if tab for this ModelGraphPanel is already open just focus on it

      if (openTabs.containsKey(fullName)) {
        parentTabPanel.setSelectedTab(openTabs.get(fullName));
      } else {
        // new EditionActions().addActions(panel);
        final TitledTab tab = new TitledTab(fullName, null, panel, null);
        tab.setName(parentTab.getName());
        parentTabPanel.addTab(tab);
        openTabs.put(fullName, tab);
        parentTabPanel.setSelectedTab(tab);
        parentTabPanel.validate();
      }

    } catch (final Throwable t) {
      logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
    } finally {
      hmi.enableChangeImpacts();
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit");
    }
  }

}
