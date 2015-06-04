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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.PasserelleConfigurer;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.definition.FieldMapping;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.definition.HMIDefinition.LayoutPreferences;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.form.CheckableComponent;
import com.isencia.passerelle.hmi.form.HMIComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The pop-up dialog to set which parameters should be shown in the HMI and to define optional alias labels for them.
 * 
 * @author erwin.de.ley@isencia.be
 */
@SuppressWarnings("serial")
public class ParameterFilterDialog extends JDialog {

  private static final boolean COLLAPSED = true;
  private static final boolean OPEN = false;

  CompositeActor model;
  HMIDefinition modelParameterFilterConfig;
  Collection<CheckableComponent> filters = new ArrayList<CheckableComponent>();
  Collection<JXTaskPane> taskPanes = new ArrayList<JXTaskPane>();
  JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

  public ParameterFilterDialog(Frame parentFrame, CompositeActor model, HMIDefinition modelParameterFilterConfig) {
    super(parentFrame, "Filter parameters", true);

    this.model = model;
    this.modelParameterFilterConfig = modelParameterFilterConfig;

    getContentPane().setLayout(new BorderLayout());

    if (model != null) {
      int paramCount = 0;

      getContentPane().add(buildHeaderBox(), BorderLayout.NORTH);

      JScrollPane formScrollPane = new JScrollPane(taskPaneContainer);
      getContentPane().add(formScrollPane, BorderLayout.CENTER);

      // paramCount = buildTopLevelCfgPane(model, paramCount, COLLAPSED);
      paramCount = addParameterFiltersForComposite(taskPaneContainer, taskPanes, model, model, paramCount, OPEN);

      pack();
      int verticalSize = Math.min(200 + paramCount * 50, 700);
      setSize(500, verticalSize);

      getContentPane().add(formScrollPane, BorderLayout.CENTER);

      // Ok / cancel buttons

      JPanel okCancelButtonPanel = new JPanel();
      okCancelButtonPanel.setLayout(new FlowLayout());
      JButton okButton = new JButton("Ok");
      okButton.addActionListener(new ParameterFilterSaver());
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dispose();
        }
      });
      okCancelButtonPanel.add(okButton);
      okCancelButtonPanel.add(cancelButton);

      getContentPane().add(okCancelButtonPanel, BorderLayout.SOUTH);
    }
    setLocationRelativeTo(parentFrame);
  }

  /**
   * @return
   */
  private Box buildHeaderBox() {
    Box headerBox = new Box(BoxLayout.Y_AXIS);
    headerBox.add(Box.createVerticalStrut(5));
    Box titleLabelBox = new Box(BoxLayout.X_AXIS);
    titleLabelBox.add(new JLabel("  Please indicate which parameters you want to show in the UI"));
    titleLabelBox.add(Box.createHorizontalGlue());
    headerBox.add(titleLabelBox);
    headerBox.add(Box.createVerticalStrut(5));

    JCheckBox selectCheckBox = new JCheckBox();
    selectCheckBox.addActionListener(new ParameterFilterChecker(selectCheckBox));
    Box selectBox = new Box(BoxLayout.X_AXIS);
    selectBox.add(new JLabel(" Select/Deselect all "));
    selectBox.add(Box.createHorizontalStrut(5));
    selectBox.add(selectCheckBox);
    selectBox.add(Box.createHorizontalGlue());
    headerBox.add(selectBox);
    headerBox.add(Box.createVerticalStrut(5));

    JCheckBox expandCheckBox = new JCheckBox();
    expandCheckBox.addActionListener(new CfgPanelExpander(expandCheckBox));
    Box expandBox = new Box(BoxLayout.X_AXIS);
    expandBox.add(new JLabel(" Expand/Collapse all "));
    expandBox.add(Box.createHorizontalStrut(5));
    expandBox.add(expandCheckBox);
    expandBox.add(Box.createHorizontalGlue());
    headerBox.add(expandBox);
    headerBox.add(Box.createVerticalStrut(5));
    return headerBox;
  }

  private List<Settable> getVisibleParameters(NamedObj n) {
    List<Settable> parameters = n.attributeList(Settable.class);
    List<Settable> visibleParameters = new ArrayList<Settable>();
    for (Settable p : parameters) {
      if (PasserelleConfigurer.isVisible(n, p)) {
        visibleParameters.add(p);
      }
    }
    return visibleParameters;
  }

  private int addParameterFiltersForComposite(JPanel parentPanel, Collection<JXTaskPane> taskPanes, CompositeActor model, CompositeActor parent,
      int paramCount, boolean collapsed) {
    // Top-level parameters ("on the model canvas")
    // only show if visible parameters found
    List<Settable> parameters = getVisibleParameters(parent);
    if (!parameters.isEmpty()) {
      JXTaskPane cfgPanel = null;
      boolean _collapsed = COLLAPSED;
      if (parentPanel instanceof JXTaskPane) {
        cfgPanel = (JXTaskPane) parentPanel;
        _collapsed = OPEN;
      } else {
        cfgPanel = new JXTaskPane();
        taskPanes.add(cfgPanel);
        parentPanel.add(cfgPanel);
      }
      paramCount = buildParameterFilter(model, paramCount, parameters, parent, cfgPanel, _collapsed);
    }
    // Director parameters if we're at top level
    // only show if visible parameters found
    if (model == parent) {
      Director d = parent.getDirector();
      if (d != null) {
        parameters = getVisibleParameters(d);
        if (!parameters.isEmpty()) {
          JXTaskPane cfgPanel = new JXTaskPane();
          taskPanes.add(cfgPanel);
          parentPanel.add(cfgPanel);
          paramCount = buildParameterFilter(model, paramCount, parameters, d, cfgPanel, COLLAPSED);
        }
      }
    }

    List<Actor> actors = parent.entityList(ptolemy.actor.Actor.class);
    for (Iterator<Actor> iter = actors.iterator(); iter.hasNext();) {
      Entity a = iter.next();
      if (a instanceof CompositeActor) {
        JXTaskPane cfgPanel = new JXTaskPane();
        taskPanes.add(cfgPanel);
        parentPanel.add(cfgPanel);
        paramCount = addParameterFiltersForComposite(cfgPanel, taskPanes, model, (CompositeActor) a, paramCount, collapsed);
      } else {
        // always show actor cfg panel, even if no visible parameters found
        parameters = getVisibleParameters(a);
        JXTaskPane cfgPanel = new JXTaskPane();
        taskPanes.add(cfgPanel);
        parentPanel.add(cfgPanel);
        paramCount = buildParameterFilter(model, paramCount, parameters, a, cfgPanel, collapsed);
      }
    }
    return paramCount;
  }

  private int buildParameterFilter(CompositeActor model, int paramCount, List<Settable> parameters, NamedObj a, JXTaskPane cfgPanel, boolean collapsed) {
    FormLayout formLayout = new FormLayout("min(100dlu;pref),5dlu,pref,2dlu", ""); // add rows dynamically
    DefaultFormBuilder builder = new DefaultFormBuilder(formLayout);
    builder.setComponentFactory(HMIComponentFactory.getInstance());
    builder.setDefaultDialogBorder();
    setBold(builder.appendTitle("Parameters"));
    setBold(builder.appendTitle("          Aliases for UI labels"));
    builder.appendSeparator();

    cfgPanel.setCollapsed(collapsed);
    if (model != a) {
      cfgPanel.setTitle(ModelUtils.getFullNameButWithoutModelName(model, a));
    } else {
      cfgPanel.setTitle("Model " + a.getName());
    }
    cfgPanel.setSpecial(true);
    cfgPanel.add(builder.getPanel());
    Model filterCfgModel = getModelParameterFilterConfig().getModel(model.getDisplayName());
    boolean filterCfgKnown = filterCfgModel != null;
    for (int i = 0; i < parameters.size(); i++, paramCount++) {
      Settable p = parameters.get(i);
      String stdName = p.getName();
      String alias = null;
      boolean checked = true;
      if (filterCfgKnown) {
        try {
          alias = filterCfgModel.getFieldMapping().getValueForKey(ModelUtils.getFullNameButWithoutModelName(model, p));
          checked = (alias != null);
          alias = stdName.equals(alias) ? null : alias;
        } catch (Exception e) {
          // just in case...
        }
      }

      CheckableComponent cComp = new CheckableComponent(new JTextField(alias, 20), p, checked);
      builder.append(p.getName(), cComp);
      filters.add(cComp);
    }
    return paramCount;
  }

  /**
   * @param label
   */
  private void setBold(JLabel label) {
    Font f = label.getFont();
    label.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
  }

  public HMIDefinition getModelParameterFilterConfig() {
    return modelParameterFilterConfig;
  }

  class ParameterFilterSaver implements ActionListener {

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
      if (modelParameterFilterConfig == null) {
        modelParameterFilterConfig = new HMIDefinition();
      }
      Model modelConfig = modelParameterFilterConfig.getModel(model.getDisplayName());
      if (modelConfig == null) {
        modelParameterFilterConfig.addModel(model.getDisplayName(), new Model(null, new FieldMapping()));
        modelConfig = modelParameterFilterConfig.getModel(model.getDisplayName());
      }

      FieldMapping fieldConfig = modelConfig.getFieldMapping();
      if (fieldConfig == null) {
        modelConfig.setFieldMapping(new FieldMapping());
        fieldConfig = modelConfig.getFieldMapping();
      }

      // need to make sure we clear previous field settings
      fieldConfig.getFieldMappings().clear();

      for (Iterator<CheckableComponent> filterItr = filters.iterator(); filterItr.hasNext();) {
        CheckableComponent filter = filterItr.next();
        Settable p = (Settable) filter.getSubject();
        boolean showIt = filter.isChecked();
        String alias = ((JTextField) filter.getCheckedComponent()).getText();
        if (alias == null || alias.trim().length() == 0)
          alias = p.getName();
        else
          alias = alias.trim();

        String paramName = ModelUtils.getFullNameButWithoutModelName(model, p);

        if (showIt) {
          fieldConfig.addFieldMapping(paramName, alias);
          // need to ensure the actor is added to the list of ordered actors, if that one has been set in the past
          LayoutPreferences layoutPrefs = modelParameterFilterConfig.getLayoutPrefs(model.getDisplayName());
          if (layoutPrefs != null) {
            // only top-level actors are maintained in order for now... (check ActorOrderDialog)
            Nameable container = p.getContainer();
            if (container.getContainer() == null || container.getContainer().getContainer() == null) {
              String actorName = ModelUtils.getFullNameButWithoutModelName(model, container);
              if (!layoutPrefs.getActorNames().contains(actorName)) {
                layoutPrefs.getActorNames().add(0, actorName);
              }
            }
          }
        }

        // System.out.println("Filter "+paramName+" set as : checked:"+Boolean.toString(showIt)+" - alias:"+alias);
      }

      dispose();
    }

  }

  class ParameterFilterChecker implements ActionListener {
    private JCheckBox checkBox;

    public ParameterFilterChecker(JCheckBox checkBox) {
      this.checkBox = checkBox;
    }

    public void actionPerformed(ActionEvent e) {
      for (Iterator<CheckableComponent> filterItr = filters.iterator(); filterItr.hasNext();) {
        CheckableComponent filter = filterItr.next();
        filter.setChecked(checkBox.isSelected());
      }
      ParameterFilterDialog.this.validate();
    }
  }

  class CfgPanelExpander implements ActionListener {
    private JCheckBox checkBox;

    public CfgPanelExpander(JCheckBox checkBox) {
      this.checkBox = checkBox;
    }

    public void actionPerformed(ActionEvent e) {
      for (Iterator<JXTaskPane> paneItr = taskPanes.iterator(); paneItr.hasNext();) {
        JXTaskPane pane = paneItr.next();
        pane.setCollapsed(!checkBox.isSelected());
      }
      ParameterFilterDialog.this.validate();
    }
  }

}
