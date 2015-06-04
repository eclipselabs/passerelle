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
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.isencia.passerelle.hmi.definition.ModelBundle;

/**
 * The pop-up dialog where the recent models can be managed for the Generic HMI.
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public class RecentModelsManagementDialog extends JDialog {

  ModelBundle hmiDef;
  JList recentModelsList;
  RecentModelsListModel recentModelsListModel;

  @SuppressWarnings("unchecked")
  public RecentModelsManagementDialog(final Frame owner, final ModelBundle modelBundle) {
    super(owner, "Recent models", true);

    this.hmiDef = modelBundle;

    getContentPane().setLayout(new BorderLayout());

    final Box titleBox = new Box(BoxLayout.Y_AXIS);
    titleBox.add(Box.createVerticalStrut(10));
    getContentPane().add(titleBox, BorderLayout.NORTH);

    final Box orderedListBox = new Box(BoxLayout.X_AXIS);
    recentModelsListModel = new RecentModelsListModel(modelBundle);

    recentModelsList = new JList(recentModelsListModel);
    final JScrollPane listScrollPane = new JScrollPane(recentModelsList);
    recentModelsList.setVisibleRowCount(10);
    recentModelsList.setFixedCellWidth(120);
    setSize(200, 250);
    orderedListBox.add(Box.createHorizontalStrut(5));
    orderedListBox.add(listScrollPane);
    orderedListBox.add(Box.createHorizontalStrut(5));
    final Box buttonsBox = new Box(BoxLayout.Y_AXIS);
    final JButton upButton = new JButton("up");
    upButton.addActionListener(new MoveUpListener());
    final JButton removeButton = new JButton("remove");
    removeButton.addActionListener(new RemoveListener());
    final JButton downButton = new JButton("down");
    downButton.addActionListener(new MoveDownListener());
    buttonsBox.add(upButton);
    buttonsBox.add(removeButton);
    buttonsBox.add(downButton);
    orderedListBox.add(buttonsBox);
    orderedListBox.add(Box.createHorizontalStrut(5));
    getContentPane().add(orderedListBox, BorderLayout.CENTER);

    // buttons
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    final JButton okButton = new JButton("Ok");
    okButton.addActionListener(new RecentModelsManagementSaver());
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        dispose();
      }
    });
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    setLocationRelativeTo(owner);
  }

  public ModelBundle getRecentModelsConfig() {
    return hmiDef;
  }

  class MoveUpListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final int index = recentModelsList.getSelectedIndex();
      if (index <= 0) {
        return;
      } else {
        recentModelsListModel.swap(index, index - 1);
        recentModelsList.setSelectedIndex(index - 1);
      }
    }
  }

  class RemoveListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final int index = recentModelsList.getSelectedIndex();
      if (index <= 0) {
        return;
      } else {
        recentModelsListModel.remove(index);
        recentModelsList.setSelectedIndex(index - 1);
      }
    }
  }

  class MoveDownListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final int index = recentModelsList.getSelectedIndex();
      if (index < 0 || index == recentModelsListModel.getSize() - 1) {
        return;
      } else {
        recentModelsListModel.swap(index, index + 1);
        recentModelsList.setSelectedIndex(index + 1);
      }
    }
  }

  class RecentModelsManagementSaver implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      for (final String modelKey : recentModelsListModel.getRemovedModelKeys()) {
        hmiDef.removeModel(modelKey);
      }
      hmiDef.setReorderedRecentModelsList(recentModelsListModel.getRecentModelList());
      dispose();
    }
  }
}
