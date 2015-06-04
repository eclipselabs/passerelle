/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.util.swing.components;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel containing two JLists and buttons to exchange elements between the
 * lists. The source list is on the left and destination list in on the right.
 */
public class JListDataExchangePanel extends JPanel {
  private MutableListModel srcModel;
  private MutableListModel destModel;
  private JList srcList, destList;
  AbstractAction addElementAction = new AddElementAction(">");
  AbstractAction addElementsAction = new AddElementsAction(">>");
  AbstractAction removeElementAction = new RemoveElementAction("<");
  AbstractAction removeElementsAction = new RemoveElementsAction("<<");
  private JButton addElement = new JButton(addElementAction);
  private JButton addElements = new JButton(addElementsAction);
  private JButton removeElement = new JButton(removeElementAction);
  private JButton removeElements = new JButton(removeElementsAction);

  public JListDataExchangePanel(MutableListModel srcModel, MutableListModel destModel, String sourceLabel, String destLabel) {
    this.srcModel = srcModel;
    this.destModel = destModel;
    balanceModels();
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridbag);

    srcList = new JList(srcModel);
    destList = new JList(destModel);

    JPanel srcPanel = new JPanel(new BorderLayout());
    srcPanel.add(new JLabel(sourceLabel), BorderLayout.NORTH);
    srcPanel.add(new JScrollPane(srcList), BorderLayout.CENTER);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.4;
    c.gridx = 0;
    c.gridy = 0;
    gridbag.setConstraints(srcPanel, c);
    add(srcPanel);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new GridLayout(4, 1));
    btnPanel.add(addElement);
    btnPanel.add(addElements);
    btnPanel.add(removeElement);
    btnPanel.add(removeElements);
    GridBagConstraints c2 = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.2;
    c.gridx = 1;
    c.gridy = 0;
    gridbag.setConstraints(btnPanel, c2);
    add(btnPanel);

    JPanel destPanel = new JPanel(new BorderLayout());
    destPanel.add(new JLabel(destLabel), BorderLayout.NORTH);
    destPanel.add(new JScrollPane(destList), BorderLayout.CENTER);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.4;
    c.gridx = 2;
    c.gridy = 0;
    gridbag.setConstraints(destPanel, c);
    add(destPanel);
  }

  public JList getSrcList() {
    return srcList;
  }

  public JList getDestList() {
    return destList;
  }

  protected void balanceModels() {
    // source shouldn't contain any of dest elements
    // thus, remove if any
    for (int i = 0; i < destModel.getSize(); i++) {
      srcModel.removeElements(new Object[] { destModel.getElementAt(i) });
    }
  }

  public void setListModels(MutableListModel srcModel, MutableListModel destModel) {
    this.srcModel = srcModel;
    this.destModel = destModel;
    balanceModels();
    srcList.setModel(srcModel);
    destList.setModel(destModel);
  }

  protected void updateActions() {
    if (srcModel.getSize() > 0) {
      enableAddActions();
    } else {
      disableAddActions();
    }
    if (destModel.getSize() > 0) {
      enableRemoveActions();
    } else {
      disableRemoveActions();
    }
  }

  protected void setAddActionsEnabled(boolean enable) {
    addElementAction.setEnabled(enable);
    addElementsAction.setEnabled(enable);
  }

  protected void enableAddActions() {
    setAddActionsEnabled(true);
  }

  protected void disableAddActions() {
    setAddActionsEnabled(false);
  }

  protected void setRemoveActionsEnabled(boolean enable) {
    removeElementAction.setEnabled(enable);
    removeElementsAction.setEnabled(enable);
  }

  protected void disableRemoveActions() {
    setRemoveActionsEnabled(false);
  }

  protected void enableRemoveActions() {
    setRemoveActionsEnabled(true);
  }

  class AddElementAction extends AbstractAction {
    public AddElementAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Object[] el = srcList.getSelectedValues();
      destModel.addElements(el);
      srcModel.removeElements(el);
      if (srcModel.getSize() == 0) {
        disableAddActions();
      }
      if (destModel.getSize() > 0) {
        enableRemoveActions();
      }
    }
  };

  class AddElementsAction extends AbstractAction {
    public AddElementsAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Object[] el = new Object[srcModel.getSize()];
      for (int i = 0; i < el.length; i++) {
        el[i] = srcModel.getElementAt(i);
      }
      destModel.addElements(el);
      srcModel.removeElements(el);
      if (srcModel.getSize() == 0) {
        disableAddActions();
      }
      if (destModel.getSize() > 0) {
        enableRemoveActions();
      }
    }
  };

  class RemoveElementAction extends AbstractAction {
    public RemoveElementAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Object[] el = destList.getSelectedValues();
      srcModel.addElements(el);
      destModel.removeElements(el);
      if (destModel.getSize() == 0) {
        disableRemoveActions();
      }
      if (srcModel.getSize() > 0) {
        enableAddActions();
      }
    }
  };

  class RemoveElementsAction extends AbstractAction {
    public RemoveElementsAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Object[] el = new Object[destModel.getSize()];
      for (int i = 0; i < el.length; i++) {
        el[i] = destModel.getElementAt(i);
      }
      srcModel.addElements(el);
      destModel.removeElements(el);
      if (destModel.getSize() == 0) {
        disableRemoveActions();
      }
      if (srcModel.getSize() > 0) {
        enableAddActions();
      }
    }
  };

  public static void main(String[] args) {
    SortedSet so1 = new TreeSet();
    so1.add("first");
    so1.add("second");
    so1.add("third");
    SortedSet so2 = new TreeSet();
    SortedMutableListModel s1 = new SortedMutableListModel(so1);
    SortedMutableListModel s2 = new SortedMutableListModel(so2);
    JListDataExchangePanel dataEx = new JListDataExchangePanel(s1, s2, "source", "dest");
    JFrame frame = new JFrame();
    frame.getContentPane().add(dataEx);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Returns the destModel.
   * 
   * @return MutableListModel
   */
  public MutableListModel getDestModel() {
    return destModel;
  }

  /**
   * Returns the srcModel.
   * 
   * @return MutableListModel
   */
  public MutableListModel getSrcModel() {
    return srcModel;
  }

}
