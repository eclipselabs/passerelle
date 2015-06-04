/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.definition.HMIDefinition.LayoutPreferences;

/**
 * The pop-up dialog to set the order in which actors should be shown in the
 * HMI.
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class ActorOrderDialog extends JDialog {

  CompositeActor model;
  HMIDefinition hmiDef;
  JList actorList;
  ActorListModel actorListModel;

  @SuppressWarnings("unchecked")
  public ActorOrderDialog(final Frame owner, final CompositeActor model, final HMIDefinition hmiDef) {
    super(owner, "Actor list", true);

    this.model = model;
    this.hmiDef = hmiDef;

    getContentPane().setLayout(new BorderLayout());

    if (model != null) {
      final Box titleBox = new Box(BoxLayout.Y_AXIS);
      titleBox.add(Box.createVerticalStrut(10));
      getContentPane().add(titleBox, BorderLayout.NORTH);

      final Box orderedListBox = new Box(BoxLayout.X_AXIS);
      actorListModel = new ActorListModel(model);
			LayoutPreferences layoutPrefs = hmiDef.getLayoutPrefs(model.getDisplayName());
			if (layoutPrefs == null || layoutPrefs.getActorNames().size() == 0) {

        // add director
        actorListModel.add(model.getDirector());
        // just show the list of actors of the model
        final List<NamedObj> actors = model.entityList(NamedObj.class);
        for (final NamedObj actor : actors) {
          // System.out.println("adding " + actor.getFullName());
          actorListModel.add(actor);
        }
        final List<Parameter> params = model.attributeList(Parameter.class);
        for (final Parameter param : params) {
          // System.out.println("adding " + param.getFullName());
          actorListModel.add(param);
        }
      } else {
				final List<String> actorNamesList = layoutPrefs.getActorNames();

        // an order is already saved in hmiDef
        for (final String actorName : actorNamesList) {
          // System.out.println("adding from filter "
          // + model.getEntity(actorName));
          actorListModel.add(model.getEntity(actorName));
          actorListModel.add(model.getAttribute(actorName));
        }

        // add actors/params that are in the model but not in hmiDef
        if (!actorListModel.containsName(ModelUtils.getFullNameButWithoutModelName(model, model.getDirector()))) {
          actorListModel.add(model.getDirector());
        }
        final List<NamedObj> actors = model.entityList(NamedObj.class);
        for (final NamedObj actor : actors) {
          if (!actorListModel.containsName(ModelUtils.getFullNameButWithoutModelName(model, actor))) {
            // System.out.println("adding " + actor.getFullName());
            actorListModel.add(actor);
          }
        }
        final List<Parameter> params = model.attributeList(Parameter.class);
        for (final Parameter param : params) {
          if (!actorListModel.containsName(ModelUtils.getFullNameButWithoutModelName(model, param))) {
            // System.out.println("adding " + param.getFullName());
            actorListModel.add(param);
          }
        }

      }
      actorList = new JList(actorListModel);
      final JScrollPane listScrollPane = new JScrollPane(actorList);
      actorList.setVisibleRowCount(10);
      actorList.setFixedCellWidth(120);
      setSize(200, 250);
      orderedListBox.add(Box.createHorizontalStrut(5));
      orderedListBox.add(listScrollPane);
      orderedListBox.add(Box.createHorizontalStrut(5));
      final Box buttonsBox = new Box(BoxLayout.Y_AXIS);
      final JButton upButton = new JButton("up");
      upButton.addActionListener(new MoveUpListener());
      final JButton downButton = new JButton("down");
      downButton.addActionListener(new MoveDownListener());
      buttonsBox.add(upButton);
      buttonsBox.add(downButton);
      orderedListBox.add(buttonsBox);
      orderedListBox.add(Box.createHorizontalStrut(5));
      getContentPane().add(orderedListBox, BorderLayout.CENTER);

      // buttons
      final JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new FlowLayout());
      final JButton okButton = new JButton("Ok");
      okButton.addActionListener(new ActorOrderSaver());
      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          dispose();
        }
      });
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);

      getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
    setLocationRelativeTo(owner);
  }

  public HMIDefinition getModelParameterFilterConfig() {
    return hmiDef;
  }

  class MoveUpListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final int index = actorList.getSelectedIndex();
      if (index <= 0) {
        return;
      } else {
        actorListModel.swap(index, index - 1);
        actorList.setSelectedIndex(index - 1);
      }
    }

  }

  class MoveDownListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final int index = actorList.getSelectedIndex();
      if (index < 0 || index == actorListModel.getSize() - 1) {
        return;
      } else {
        actorListModel.swap(index, index + 1);
        actorList.setSelectedIndex(index + 1);
      }
    }

  }

  class ActorOrderSaver implements ActionListener {

    public void actionPerformed(final ActionEvent e) {
      if (hmiDef == null) {
        hmiDef = new HMIDefinition();
      }
			LayoutPreferences layoutPrefs = hmiDef.getLayoutPrefs(model.getDisplayName());
      if (layoutPrefs == null) {
        layoutPrefs = new LayoutPreferences(1);
				hmiDef.addModelLayout(model.getDisplayName(), layoutPrefs);
      }
      layoutPrefs.getActorNames().clear();

      for (final NamedObj actor : actorListModel.getActorList()) {
        layoutPrefs.getActorNames().add(ModelUtils.getFullNameButWithoutModelName(model, actor));
      }

      dispose();
    }
  }
}
