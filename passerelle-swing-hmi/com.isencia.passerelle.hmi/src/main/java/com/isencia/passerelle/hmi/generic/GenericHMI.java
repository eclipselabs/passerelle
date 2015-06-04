/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import net.infonode.docking.TabWindow;
import net.infonode.tabbedpanel.Tab;
import net.infonode.tabbedpanel.TabDragEvent;
import net.infonode.tabbedpanel.TabDropDownListVisiblePolicy;
import net.infonode.tabbedpanel.TabEvent;
import net.infonode.tabbedpanel.TabLayoutPolicy;
import net.infonode.tabbedpanel.TabListener;
import net.infonode.tabbedpanel.TabRemovedEvent;
import net.infonode.tabbedpanel.TabStateChangedEvent;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.titledtab.TitledTab;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.constants.IPropertyNames;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.gui.IPasserelleComponent;
import com.isencia.passerelle.actor.gui.IPasserelleEditorPaneFactory;
import com.isencia.passerelle.actor.gui.IPasserelleEditorPaneFactory.ParameterEditorAuthorizer;
import com.isencia.passerelle.actor.gui.IPasserelleQuery;
import com.isencia.passerelle.actor.gui.PasserelleEmptyQuery;
import com.isencia.passerelle.actor.gui.PasserelleQuery;
import com.isencia.passerelle.actor.gui.PasserelleQuery.QueryLabelProvider;
import com.isencia.passerelle.actor.gui.PasserelleQuery.RenameRequest;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.actor.gui.graph.EditPreferencesDialog;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.definition.HMIDefinition;
import com.isencia.passerelle.hmi.definition.HMIDefinition.LayoutPreferences;
import com.isencia.passerelle.hmi.definition.Model;
import com.isencia.passerelle.hmi.definition.ModelBundle;
import com.isencia.passerelle.hmi.graph.LookInsideViewFactory;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.trace.TraceDialog;
import com.isencia.passerelle.hmi.trace.TraceVisualizer;
import com.isencia.passerelle.hmi.util.VersionPrinter;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.validation.ModelValidationService;
import com.isencia.passerelle.validation.ValidationContext;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;

/**
 * @todo Class Comment
 * @author erwin.de.ley@isencia.be
 */
public class GenericHMI extends HMIBase implements ParameterEditorAuthorizer, QueryLabelProvider {
  private final static Logger logger = LoggerFactory.getLogger(GenericHMI.class);

  public static final String HMI_DEF_PREFS_FILE_PROPNAME = "hmi.default.preferences.file";
  private static String HMI_DEF_PREFS_FILE;
  public static final String HMI_DEF_PREFS_PATH_PROPNAME = "hmi.default.preferences.path";
  public static final String HMI_DEF_PREFS_FILE_DEFAULT = "hmi_filter_def.xml";

  private IPasserelleEditorPaneFactory editorPaneFactory;

  private JMenuBar menuBar;

  private JScrollPane parameterScrollPane;
  private JScrollPane modelGraphScrollPane;
  private JPanel paremeterFormPanel;
  private TabWindow modelGraphTabWindow;
  private TabbedPanel modelGraphTabPanel;

  private JPanel tracePanel;

  private JPanel configPanel;

  private JLabel modelNameLabel;

  private HMIDefinition hmiDef;

  private int nrColumns = 1;
  private final Map<String, TitledTab> graphTabsMap = new HashMap<String, TitledTab>();
  private final Map<NamedObj, IPasserelleQuery> listQuery = new HashMap<NamedObj, IPasserelleQuery>();

  /**
   * Starts the Generic HMI with the default Models Definition bundle, and optionally with a graphical model editor.
   * 
   * @param showModelGraph
   *          if true, also show the graphical model editor.
   */
  public GenericHMI(final boolean showModelForms, final boolean showModelGraph) {
    super(GENERIC, showModelForms, showModelGraph);
  }

  @Override
  public void init() {
    super.init();
    String defPrefsFileName = System.getProperty(HMI_DEF_PREFS_FILE_PROPNAME, HMI_DEF_PREFS_FILE_DEFAULT);
    String defPrefsFilePath = System.getProperty(HMI_DEF_PREFS_PATH_PROPNAME);
    if (defPrefsFilePath != null && defPrefsFilePath.length() > 0) {
      HMI_DEF_PREFS_FILE = defPrefsFilePath + "/" + defPrefsFileName;
    } else {
      HMI_DEF_PREFS_FILE = EnvironmentUtils.getApplicationRootFolder() + "/" + IPropertyNames.APP_CFG_DEFAULT + "/" + defPrefsFileName;
    }

    hmiDef = HMIDefinition.parseHMIDefFile(HMI_DEF_PREFS_FILE);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void showModelForm(final String modelKey) {
    if (!showModelForms) {
      return;
    }
    
    int originalScrollPosition = parameterScrollPane.getVerticalScrollBar().getValue();

    final URL modelURL = getModelURL();
    try {
      clearModelForms(modelURL != null ? modelURL.toURI() : null);
    } catch (URISyntaxException ex) {
      // We can safely ignore this since the modelURL is in compliance with RFC2396
    }

    if (getDialogHookComponent() != null) {
      if (modelURL != null) {
        getDialogHookComponent().setTitle(HMIBase.HMI_APPLICATIONNAME + " - " + modelURL);
        modelNameLabel = new JLabel(modelURL.toString(), JLabel.CENTER);

        parameterScrollPane.getParent().add(modelNameLabel, BorderLayout.NORTH);
        parameterScrollPane.getParent().validate();
      }
    }

    if (getCurrentModel() != null) {
      final LayoutPreferences layoutPrefs = hmiDef.getLayoutPrefs(getModelIdentifierForFilterDef(getCurrentModel()));

      if (layoutPrefs != null) {
        nrColumns = layoutPrefs.getNrColumns();
      }
      // sizeAndPlaceFrame();

      getConfigPanel().setLayout(new GridLayout(1, nrColumns));
      final JPanel[] boxes = new JPanel[nrColumns];
      for (int i = 0; i < nrColumns; ++i) {
        boxes[i] = new JPanel(new VerticalLayout());

        configPanel.add(boxes[i]);
      }
      final List entities = getCurrentModel().entityList();
      if (entities != null) {
        final Vector<JPanel> actorPanels = new Vector<JPanel>();
        // render model
        if (layoutPrefs == null || layoutPrefs.getActorNames() == null || layoutPrefs.getActorNames().size() == 0) {

          // render model for model parameters
          renderModelComponent(false, getCurrentModel(), boxes[0]);

          // render model for director
          if (getCurrentModel().getDirector() != null) {
            final JPanel b = new JPanel(new VerticalLayout());
            final boolean added = renderModelComponent(false, getCurrentModel().getDirector(), b);
            if (added) {
              actorPanels.add(b);
            }
          }

          // build the vector of panels with only visible actors
          for (int i = 0; i < entities.size(); ++i) {
            final NamedObj e = (NamedObj) entities.get(i);
            final JPanel b = new JPanel(new VerticalLayout());
            // JPanel b = boxes[(i+1) / nrEntriesPerColumn];
            // Box b = boxes[(i+1) / nrEntriesPerColumn];
            final boolean added = renderModelComponent(true, e, b);
            if (added) {
              actorPanels.add(b);
            }
          }
        } else {// an actor order has been defined
          final List<String> actorNames = layoutPrefs.getActorNames();
          boolean modelParamsRendered = false;
          for (final String actorName : actorNames) {
            final Entity e = getCurrentModel().getEntity(actorName);
            final Attribute param = getCurrentModel().getAttribute(actorName);
            if (getCurrentModel().getDirector().getName().equals(actorName)) {
              // render model for director
              if (getCurrentModel().getDirector() != null) {
                final JPanel b = new JPanel(new VerticalLayout());
                final boolean added = renderModelComponent(false, getCurrentModel().getDirector(), b);
                if (added) {
                  actorPanels.add(b);
                }
              }
            } else if (e != null) {
              // render model actors
              final JPanel b = new JPanel(new VerticalLayout());
              final boolean added = renderModelComponent(true, e, b);
              if (added) {
                actorPanels.add(b);
              }
            } else if (param != null) {
              // render model for model parameters
              if (!modelParamsRendered) {
                // since all model' params are rendered at the
                // same time
                // and it is possible to have several params in
                // the layoutPrefs, make sure that is called
                // only once
                final JPanel b = new JPanel(new VerticalLayout());
                final boolean added = renderModelComponent(false, getCurrentModel(), b);
                if (added) {
                  actorPanels.add(b);
                }
                modelParamsRendered = true;
              }
            } else {
              // it's an invalid layoutPrefs definition, e.g.
              // defined for another model
              // with same name or...
              layoutPrefs.getActorNames().clear();
              showModelForm(modelKey);
              break;
            }
          }
        }
        // display actors in fonction of layout prefs.
        final int nrEntriesPerColumn = actorPanels.size() / nrColumns;
        final int more = actorPanels.size() % nrColumns;
        final Iterator it = actorPanels.iterator();
        for (int i = 0; i < nrColumns; i++) {
          final JPanel column = boxes[i];
          if (i < more) {
            if (it.hasNext()) {
              final JPanel actor = (JPanel) it.next();
              column.add(actor);
            }
          }
          for (int j = 0; j < nrEntriesPerColumn; j++) {
            if (it.hasNext()) {
              final JPanel actor = (JPanel) it.next();
              column.add(actor);
            }
          }
        }
      }
      paremeterFormPanel.add("Center", configPanel);
      // try to set to previous scroll position if still possible
      int minScroll = parameterScrollPane.getVerticalScrollBar().getMinimum();
      int maxScroll = parameterScrollPane.getVerticalScrollBar().getMaximum();
      int newScrollPosition = Math.max(Math.min(originalScrollPosition, maxScroll), minScroll);
      parameterScrollPane.validate();
      parameterScrollPane.getVerticalScrollBar().setValue(newScrollPosition);
    }
  }

  @Override
  public Flow loadModel(final URL _modelFile, final String modelKey) throws Exception {
    if (_modelFile == null) {
      throw new IllegalArgumentException("Undefined model file for " + modelKey);
    }
    boolean modelAlreadyLoaded = selectTab(_modelFile.toURI(), modelGraphTabPanel);
    Flow loadedModel = super.loadModel(_modelFile, modelKey);

    if (!modelAlreadyLoaded) {
      boolean needToValidateModels = true;
      Token validateModelsToken = PtolemyPreferences.preferenceValueLocal(loadedModel, "_validateModels");
      if (validateModelsToken != null && validateModelsToken instanceof BooleanToken) {
        needToValidateModels = ((BooleanToken) validateModelsToken).booleanValue();
      }

      if (needToValidateModels) {
        ValidationContext validationContext = new ValidationContext();
        ModelValidationService.getInstance().validate(loadedModel, validationContext);
        if (!validationContext.isValid()) {
          boolean isError = false;
          for (Nameable  validatedElement : validationContext.getElementsWithErrors()) {
            for (ValidationException e : validationContext.getErrors(validatedElement)) {
              Object obj = e.getModelElement();
              String validationErrorMsg = e.getSimpleMessage();
              Severity severity = e.getErrorCode().getSeverity();
              if (Severity.ERROR.compareTo(severity) <= 0) {
                isError = true;
              }
              String severityStr = severity.name();
              if (obj instanceof Actor) {
                ExecutionTracerService.trace((Actor) obj, severityStr + " : " + validationErrorMsg);
              } else if (obj instanceof Director) {
                ExecutionTracerService.trace((Director) obj, severityStr + " : " + validationErrorMsg);
              }
              // only need the first one, which should be the most important one
              break;
            }
          }
          if (!isError) {
            PopupUtil.showWarning(getDialogHookComponent(), "warning.flow.validationError");
          } else {
            PopupUtil.showError(getDialogHookComponent(), "warning.flow.validationError");
          }
        }
      }
    }
    return loadedModel;
  }

  @Override
  public boolean hasChangeImpact(final ChangeRequest change) {
    boolean hasImpact = super.hasChangeImpact(change);
    if (change.getDescription() != null) {
      if (change instanceof RenameRequest) {
        // showModelForm(null);
        RenameRequest _ch = (RenameRequest) change;
        String oldName = (_ch.getContext().getFullName() + "." + _ch.getOldName()).substring(1);
        String newName = (_ch.getContext().getFullName() + "." + _ch.getNewName()).substring(1);
        ((LookInsideViewFactory) graphPanel.getViewFactory()).renameView(oldName, newName);
      }
      final String[] importantChanges = new String[] { "entity", "property", "relation", "link", "unlink", "deleteRelation", "deleteEntity", "deleteProperty",
          "class" };
      for (final String changeType : importantChanges) {
        if (change.getDescription().contains(changeType)) {
          hasImpact = true;
          break;
        }
      }
    }
    return hasImpact;
  }

  @Override
  public void setChanged(URL modelURL) {
    super.setChanged(modelURL);
    if (graphTabsMap != null && getModelURL() != null) {
      // graphTabsMap.get(this.getModelURL().toString()).setText(
      // getCurrentModel().getName() + " - ***");
      graphTabsMap.get(getModelURL().toString()).setIcon(
          new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/isencia/passerelle/hmi/resources/star.png"))));
    }
  }

  @Override
  public void setSaved(URI modelURI) {
    super.setSaved(modelURI);
    if (modelURI != null) {
      if (graphTabsMap.get(modelURI.toString()) != null) {
        final TitledTab tab = graphTabsMap.get(modelURI.toString());
        tab.setIcon(null);
        // tab.setText(getCurrentModel().getName());
      }
    }
  };

  @Override
  protected void showModelGraph(final String modelKey) {
    // clearModelGraphs();
    try {
      if (graphPanelEffigy == null) {
        graphPanelEffigy = new PtolemyEffigy(getPtolemyConfiguration(), modelKey);
      }
      graphPanel = new ModelGraphPanel(getCurrentModel(), graphPanelEffigy);
      // TODO BEWARE : animation now happens in the actor
      // thread and blocks for a second!!
      graphPanel.setAnimationDelay(1000);

      // final TitledTab tab = new TitledTab(getCurrentModel().getDisplayName(), null, graphPanel, null);
      final TitledTab tab = new TitledTab(modelKey, null, graphPanel, null);

      graphTabsMap.put(getModelURL().toString(), tab);
      // mainGraphTab.getWindowProperties().setCloseEnabled(true);
      tab.setName(getModelURL().toString());
      tab.setToolTipText(getModelURL().toString());

      final TabbedPanel tabbedPane = getModelGraphPanel();
      tabbedPane.addTab(tab);
      graphPanel.registerViewFactory(new LookInsideViewFactory(this, tabbedPane, tab, graphPanelEffigy));
      // new EditionActions().addActions(graphPanel);

      selectTab(getModelURL().toURI(), tabbedPane);
      // StateMachine stuff
      StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, tab.getName(), graphPanel);
      StateMachine.getInstance().compile();

    } catch (final Throwable t) {
      logger.error(HMIMessages.getString(HMIMessages.ERROR_GENERIC), t);
    }
  }

  /**
   * @param tab
   * @param tabbedPane
   * @param modelURI
   */
  protected boolean selectTab(final URI modelURI, final TabbedPanel tabbedPane) {
    TitledTab tab = graphTabsMap.get(modelURI.toString());
    if (tab != null) {
      tabbedPane.setSelectedTab(tab);
      getModelGraphScrollPane().validate();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void clearModelGraphs(URI modelURI) {
    // if (graphTabsMap.containsKey(this.getModelURL().toString())) {
    // final View view =
    // graphTabsMap.get(this.getModelURL().toString());
    // view.close();
    String modelTabKey = modelURI.toString();
    Tab tabToClear = graphTabsMap.get(modelTabKey);
    if (tabToClear != null) {
      modelGraphTabPanel.removeTab(tabToClear);
      graphTabsMap.remove(modelTabKey);
    }
  }

  /**
   * @param nObj
   * @param panel
   * @return true if a form was effectively rendered, i.e. when at least 1 parameter was available
   * @throws IllegalActionException
   */
  protected boolean renderModelComponent(final boolean deep, final NamedObj nObj, final JPanel panel) {
    if (logger.isDebugEnabled()) {
      logger.debug("renderModelComponent() - Entity " + nObj.getFullName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    Attribute cfgAttr = nObj.getAttribute("__not_configurable");
    if (cfgAttr != null) {
      // cfg panel for this entity should not be shown
      return false;
    }
    if (nObj instanceof CompositeActor && deep) {
      return renderCompositeModelComponent((CompositeActor) nObj, panel);
    } else {
      renderModelComponentAnnotations(nObj, panel);
      final IPasserelleEditorPaneFactory epf = getEditorPaneFactoryForComponent(nObj);
      Component component = null;

      // XXX keep queries in a map to resolve memory leak
      IPasserelleQuery passerelleQuery = null;
      if (listQuery.containsKey(nObj)) {
        passerelleQuery = listQuery.get(nObj);
      } else {
        passerelleQuery = epf.createEditorPaneWithAuthorizer(nObj, this, this);
        if (!passerelleQuery.hasAutoSync()) {
          try {
            final Set<ParameterToWidgetBinder> queryBindings = passerelleQuery.getParameterBindings();
            for (final ParameterToWidgetBinder parameterToWidgetBinder : queryBindings) {
              hmiFields.put(parameterToWidgetBinder.getBoundParameter().getFullName(), parameterToWidgetBinder);
            }
          } catch (final Exception exception) {
            throw new RuntimeException("Error creating bindings for passerelleQuery", exception);
          }
        }
        listQuery.put(nObj, passerelleQuery);
      }

      final IPasserelleComponent passerelleComponent = passerelleQuery.getPasserelleComponent();
      if (!(passerelleComponent instanceof Component)) {
        return false;
      }

      component = (Component) passerelleComponent;
      if (component != null && !(component instanceof PasserelleEmptyQuery)) {
        final String name = ModelUtils.getFullNameButWithoutModelName(getCurrentModel(), nObj);
        component.setName(name);
        final JPanel globalPanel = new JPanel(new BorderLayout());

        // Panel for title
        final JPanel titlePanel = createTitlePanel(name);

        // Add a nice background to panels
        panel.setName(name + " " + panel);
        titlePanel.setBackground(panel.getBackground());
        ((JComponent) component).setBackground(panel.getBackground());
        // Border
        final Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        final TitledBorder border = BorderFactory.createTitledBorder(loweredbevel/*
                                                                                  * ,name
                                                                                  */);
        globalPanel.setBorder(border);

        globalPanel.add(titlePanel, BorderLayout.NORTH);
        globalPanel.add(component, BorderLayout.CENTER);
        panel.add(globalPanel);

        // StateMachine stuff
        StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, name, component);
        StateMachine.getInstance().compile();
        return true;
      }
      return false;
    }
  }

  protected JPanel createTitlePanel(final String name) {
    final JPanel result = new JPanel(new BorderLayout());
    result.setName("result");
    final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/isencia/passerelle/hmi/resources/param.gif")));
    final JLabel startLabel = new JLabel(icon);
    result.add(startLabel, BorderLayout.LINE_START);

    final JLabel nameLabel = new JLabel(name);
    final Font f = nameLabel.getFont();
    nameLabel.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 2));
    nameLabel.setForeground(new Color(49, 106, 196));
    result.add(nameLabel);

    return result;
  }

  protected IPasserelleEditorPaneFactory getEditorPaneFactoryForComponent(final NamedObj e) {
    IPasserelleEditorPaneFactory epf = null;
    try {
      epf = (IPasserelleEditorPaneFactory) e.getAttribute("_editorPaneFactory", IPasserelleEditorPaneFactory.class);
    } catch (final IllegalActionException e1) {
    }

    epf = epf != null ? epf : editorPaneFactory;
    return epf;
  }

  /**
   * @param e
   * @param b
   * @return true if a form was effectively rendered, i.e. when at least 1 parameter was available
   */
  @SuppressWarnings("unchecked")
  protected boolean renderCompositeModelComponent(final CompositeActor e, final JPanel b) {
    if (logger.isDebugEnabled()) {
      logger.debug("renderCompositeModelComponent() - Entity " + e.getFullName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    final IPasserelleEditorPaneFactory epf = getEditorPaneFactoryForComponent(e);
    final IPasserelleQuery pQuery = epf.createEditorPaneWithAuthorizer(e, this, this);
    final JComponent c = (JComponent) pQuery.getPasserelleComponent();

    if (pQuery != null) {
      boolean nonEmptyForm = !(pQuery instanceof PasserelleEmptyQuery);
      // remove the default message in an empty query
      if (!nonEmptyForm) {
        ((PasserelleEmptyQuery) pQuery).setText("");
      } else if (!pQuery.hasAutoSync()) {
        try {
          final Set<ParameterToWidgetBinder> queryBindings = pQuery.getParameterBindings();
          for (final ParameterToWidgetBinder parameterToWidgetBinder : queryBindings) {
            hmiFields.put(parameterToWidgetBinder.getBoundParameter().getFullName(), parameterToWidgetBinder);
          }
        } catch (final Exception exception) {
          throw new RuntimeException("Error creating bindings for passerelleQuery", exception);
        }
      }

      final String name = ModelUtils.getFullNameButWithoutModelName(getCurrentModel(), e);
      final List subActors = e.entityList();
      // TODO: && changed by || because was not displaying parameters for
      // composite
      // still a problem of display with multiactor
      if (pQuery instanceof PasserelleQuery || !subActors.isEmpty()) {
        final JPanel compositeBox = createCompositePanel(b, c, name);
        renderModelComponentAnnotations(e, compositeBox);
        for (final Iterator<NamedObj> subActorItr = subActors.iterator(); subActorItr.hasNext();) {
          final NamedObj subActor = subActorItr.next();
          nonEmptyForm |= renderModelComponent(true, subActor, compositeBox);
        }
        if (nonEmptyForm) {
          b.add(compositeBox);
          // StateMachine.getInstance().registerActionForState(
          // StateMachine.MODEL_OPEN, name, c);
          // StateMachine.getInstance().compile();
        }
        return nonEmptyForm;
      } else if (!(pQuery instanceof PasserelleEmptyQuery)) {
        // System.out.println("else name" + name);
        c.setName(name);
        c.setBorder(BorderFactory.createTitledBorder(name));
        b.add(c);
        // StateMachine.getInstance().registerActionForState(
        // StateMachine.MODEL_OPEN, name, c);
        // StateMachine.getInstance().compile();
        return true;
      }
    }
    // System.out.println("renderComposite - out");
    return false;
  }

  protected JPanel createCompositePanel(final JPanel b, final JComponent c, final String name) {
    final JPanel compositeBox = new JPanel(new VerticalLayout(5));
    compositeBox.setName("compositeBox");
    int r = b.getBackground().getRed() - 20;
    if (r < 1) {
      r = 0;
    }
    if (r > 254) {
      r = 255;
    }
    int g = b.getBackground().getGreen() - 20;
    if (g < 1) {
      g = 0;
    }
    if (g > 254) {
      g = 255;
    }
    int bl = b.getBackground().getBlue() - 20;
    if (bl < 1) {
      bl = 0;
    }
    if (bl > 254) {
      bl = 255;
    }

    compositeBox.setBackground(new Color(r, g, bl));
    final JPanel title = new JPanel(new BorderLayout());
    title.setName("title");
    title.setBackground(new Color(r, g, bl));
    /*
     * ImageIcon icon = new ImageIcon( Toolkit .getDefaultToolkit() .getImage( (getClass() .getResource("/com/isencia/passerelle/hmi/resources/composite.gif"
     * )))); JLabel lab = new JLabel(icon); title.add(lab, BorderLayout.LINE_START);
     */
    final JLabel lab2 = new JLabel(name);
    final Font f = lab2.getFont();
    lab2.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 6));
    lab2.setForeground(new Color(49, 106, 196));
    title.add(lab2);
    compositeBox.add(title);
    final Border loweredbevel = BorderFactory.createLoweredBevelBorder();
    final TitledBorder border = BorderFactory.createTitledBorder(loweredbevel);

    compositeBox.setBorder(border);
    compositeBox.setName(name);
    c.setBackground(new Color(r, g, bl));
    compositeBox.add(c);
    return compositeBox;
  }

  @SuppressWarnings("unchecked")
  protected void renderModelComponentAnnotations(final NamedObj e, final JPanel b) {
    final List<TextAttribute> annotations = e.attributeList(TextAttribute.class);
    if (!annotations.isEmpty()) {
      final Box annotationsBox = new Box(BoxLayout.Y_AXIS);
      // annotationsBox.setBorder(BorderFactory.createTitledBorder("Info"));
      for (final TextAttribute textAttribute : annotations) {

        if (isAnnotionAuthorizedForEditor(textAttribute)) {
          final Box subBox = new Box(BoxLayout.Y_AXIS);

          // subBox.setBorder(BorderFactory.createTitledBorder(textAttribute.getName()));
          final String[] annotationLines = textAttribute.text.getExpression().split("\n");

          for (final String annotationLine : annotationLines) {
            final JLabel lab = new JLabel(annotationLine);
            textAttribute.fontFamily.getExpression();
            int bold = 0;
            if (textAttribute.bold.getExpression().compareTo("true") == 0) {
              bold = Font.BOLD;
            }
            int italic = 0;
            if (textAttribute.italic.getExpression().compareTo("true") == 0) {
              italic = Font.ITALIC;
            }
            final int textSize = Integer.valueOf(textAttribute.textSize.getExpression());
            final Font font = new Font(textAttribute.fontFamily.getExpression(), bold | italic, textSize);
            lab.setFont(font);
            final String colorString = textAttribute.textColor.getExpression();
            final String sub = colorString.substring(1, colorString.lastIndexOf("}"));
            final String[] rgba = sub.split(",");
            final float r = Float.valueOf(rgba[0]);
            final float g = Float.valueOf(rgba[1]);
            final float bl = Float.valueOf(rgba[2]);
            final float a = Float.valueOf(rgba[3]);
            final Color color = new Color(r, g, bl, a);
            lab.setForeground(color);
            subBox.add(lab);
          }
          annotationsBox.add(subBox);
        }
      }
      b.add(annotationsBox);
    }
  }

  @Override
  protected void clearModelForms(URI modelURI) {
    clearPanel(getConfigPanel());
    listQuery.clear();

    if (getDialogHookComponent() != null) {
      getDialogHookComponent().setTitle(HMIBase.HMI_APPLICATIONNAME);
    }
    if (parameterScrollPane != null) {
      if (modelNameLabel != null) {
        parameterScrollPane.getParent().remove(modelNameLabel);
      }
      parameterScrollPane.getParent().validate();
    }
    // getConfigPanel().validate();
  }

  protected void clearPanel(final JComponent panel) {
    // System.out.println("->");
    final Component[] comp = panel.getComponents();

    for (final Component element : comp) {
      // System.out.println(element);
      if (element instanceof JComponent) {
        // do not remove IPasserelleQuery since a reference is kept by
        // the actor
        if (element instanceof IPasserelleQuery) {
          if (element instanceof CloseListener) {
            ((CloseListener) element).windowClosed(null, null);
          }
        } else {
          clearPanel((JComponent) element);
          // System.out.println("REMOVE " + element.getClass());
          ((JComponent) element).removeAll();
          ((JComponent) element).removeNotify();
        }
      }
    }
    // System.out.println("REMOVE PANEL " + panel.getClass());
    panel.removeAll();
    panel.removeNotify();
    // System.out.println("<-");
  }

  private class AboutBoxOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      String versionInfo = VersionPrinter.getProjectVersionInfo();
      JOptionPane.showMessageDialog(getDialogHookComponent(), versionInfo, "About", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private class LayoutPrefsClearer implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      if (getCurrentModel() != null) {
        String modelIdentifierForFilterDef = getModelIdentifierForFilterDef(getCurrentModel());
        getHmiDef().getModelLayoutPrefs().remove(modelIdentifierForFilterDef);
        getHmiDef().removeModel(modelIdentifierForFilterDef);
        saveAndApplySettings();
        showModelForm(null);
      } else {
        JOptionPane.showMessageDialog(getDialogHookComponent(), "Please select/open a model first", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  private class ParameterFilterOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      if (getCurrentModel() != null) {
        final ParameterFilterDialog dialog = new ParameterFilterDialog(getDialogHookComponent(), getCurrentModel(), hmiDef);
        dialog.setVisible(true);
        final HMIDefinition filterSettingsFromDialog = dialog.getModelParameterFilterConfig();
        if (filterSettingsFromDialog != null) {
          hmiDef = filterSettingsFromDialog;
          saveAndApplySettings();
          showModelForm(null);
        }
      } else {
        JOptionPane.showMessageDialog(getDialogHookComponent(), "Please select/open a model first", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  private class ActorOrderOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      if (getCurrentModel() != null) {
        final ActorOrderDialog dialog = new ActorOrderDialog(getDialogHookComponent(), getCurrentModel(), hmiDef);
        dialog.setVisible(true);
        final HMIDefinition filterSettingsFromDialog = dialog.getModelParameterFilterConfig();
        if (filterSettingsFromDialog != null) {
          hmiDef = filterSettingsFromDialog;
          saveAndApplySettings();
          showModelForm(null);
        }
      } else {
        JOptionPane.showMessageDialog(getDialogHookComponent(), "Please select/open a model first", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  private class ColumnCountDialogOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      final Integer chosenNrColumns = (Integer) JOptionPane.showInputDialog(getDialogHookComponent(), "Please enter nr of desired columns", "Layout",
          JOptionPane.QUESTION_MESSAGE, null, new Object[] { new Integer(1), new Integer(2), new Integer(3) }, new Integer(nrColumns));
      if (chosenNrColumns != null) {
        nrColumns = chosenNrColumns.intValue();
      }
      LayoutPreferences layoutPrefs = hmiDef.getLayoutPrefs(getModelIdentifierForFilterDef(getCurrentModel()));
      if (layoutPrefs == null) {
        layoutPrefs = new LayoutPreferences(nrColumns);
        hmiDef.addModelLayout(getModelIdentifierForFilterDef(getCurrentModel()), layoutPrefs);
      } else {
        layoutPrefs.setNrColumns(nrColumns);
      }
      saveAndApplySettings();
      showModelForm(null);
    }
  }

  private class GraphPreferencesOpener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      EditPreferencesDialog dialog = new EditPreferencesDialog(getDialogHookComponent(), getPtolemyConfiguration());

      try {
        int tabCount = modelGraphTabPanel.getTabCount();
        for (int i = 0; i < tabCount; ++i) {
          Tab tab = modelGraphTabPanel.getTabAt(i);
          // }
          // for(Tab tab : graphTabsMap.values()) {
          if (tab.getContentComponent() instanceof ModelGraphPanel) {
            ModelGraphPanel _gp = (ModelGraphPanel) tab.getContentComponent();
            GraphModel graphModel = _gp.getJGraph().getGraphPane().getGraphController().getGraphModel();
            graphModel.dispatchGraphEvent(new GraphEvent(this, GraphEvent.STRUCTURE_CHANGED, graphModel.getRoot()));
          }
        }
      } catch (Exception ex) {
        logger.error("Error applying preference changes to open model", ex);
      }
    }
  }

  private class RecentModelsManagementOpener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      final RecentModelsManagementDialog dialog = new RecentModelsManagementDialog(getDialogHookComponent(), getHmiModelsDef());
      dialog.setVisible(true);
      final ModelBundle filterSettingsFromDialog = dialog.getRecentModelsConfig();
      if (filterSettingsFromDialog != null) {
        setHmiModelsDef(filterSettingsFromDialog);
        saveAndApplySettings();
      }
    }
  }

  private void saveAndApplySettings() {
    final String def = ModelBundle.generateDef(hmiDef);
    Writer defWriter = null;
    try {
      defWriter = new FileWriter(HMI_DEF_PREFS_FILE);
      defWriter.write(def);
    } catch (final IOException e1) {
      logger.error("Error saving filter def", e1);
      PopupUtil.showError(getDialogHookComponent(), "impossible to save filter", e1.getMessage());
    } finally {
      if (defWriter != null) {
        try {
          defWriter.close();
        } catch (final Exception e1) {
        }
      }
    }
    recreateModelsMenu(modelsSubMenu);
  }

  public boolean isAnnotionAuthorizedForEditor(final TextAttribute p) {
    // CHeck if parameter is present in the filter configuration
    if (hmiDef == null || getCurrentModel() == null || hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel())) == null) {
      // we've got no usefull filter info
      return true;
    }
    final Model filterDefForCurrentModel = hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel()));
    final String alias = filterDefForCurrentModel.getFieldMapping().getValueForKey(ModelUtils.getFullNameButWithoutModelName(getCurrentModel(), p));
    return alias != null;
  }

  public boolean allowRename() {
    return false;
  }

  public boolean isAuthorizedForEditor(final Settable p) {
    // CHeck if parameter is present in the filter configuration
    if (hmiDef == null || getCurrentModel() == null || hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel())) == null) {
      // we've got no usefull filter info
      return true;
    }
    final Model filterDefForCurrentModel = hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel()));
    final String alias = filterDefForCurrentModel.getFieldMapping().getValueForKey(ModelUtils.getFullNameButWithoutModelName(getCurrentModel(), p));
    return alias != null;
  }

  /**
   * Overridable method to influence
   * 
   * @return
   */
  protected String getModelIdentifierForFilterDef(Flow model) {
    return model.getDisplayName();
  }

  public String getLabelFor(final Settable settable) {
    // CHeck if parameter is present in the filter configuration
    if (hmiDef == null || getCurrentModel() == null || hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel())) == null) {
      // we've got no usefull filter info
      return settable.getName();
    }
    final Model filterDefForCurrentModel = hmiDef.getModel(getModelIdentifierForFilterDef(getCurrentModel()));
    final String alias = filterDefForCurrentModel.getFieldMapping().getValueForKey(ModelUtils.getFullNameButWithoutModelName(getCurrentModel(), settable));
    return alias != null ? alias : settable.getFullName();
  }

  public HMIDefinition getHmiDef() {
    return hmiDef;
  }

  public void addPrefsMenu(final JMenuBar menuBar) {
    // prefs are about form editor and graph editor, so only need to create this menu when one of those editors is
    // enabled
    if (showModelForms || showModelGraph) {
      final JMenu prefsMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_PREFS));
      prefsMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_PREFS + HMIMessages.KEY).charAt(0));

      if (showModelForms) {
        final JMenuItem layoutMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_LAYOUT), HMIMessages.getString(
            HMIMessages.MENU_LAYOUT + HMIMessages.KEY).charAt(0));
        layoutMenuItem.addActionListener(new ColumnCountDialogOpener());
        prefsMenu.add(layoutMenuItem);
        final JMenuItem actorOrderMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_ACTOR_ORDER), HMIMessages.getString(
            HMIMessages.MENU_ACTOR_ORDER + HMIMessages.KEY).charAt(0));
        actorOrderMenuItem.addActionListener(new ActorOrderOpener());
        prefsMenu.add(actorOrderMenuItem);
        final JMenuItem paramFilterMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_PARAM_VISIBILITY), HMIMessages.getString(
            HMIMessages.MENU_PARAM_VISIBILITY + HMIMessages.KEY).charAt(0));
        paramFilterMenuItem.addActionListener(new ParameterFilterOpener());
        prefsMenu.add(paramFilterMenuItem);
        final JMenuItem clearLayoutPrefsMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_CLEAR_LAYOUTPREFS), 
            HMIMessages.getString(HMIMessages.MENU_CLEAR_LAYOUTPREFS + HMIMessages.KEY).charAt(0));
        clearLayoutPrefsMenuItem.addActionListener(new LayoutPrefsClearer());
        prefsMenu.add(clearLayoutPrefsMenuItem);

        prefsMenu.add(new JSeparator());
        StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, layoutMenuItem);
        StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, actorOrderMenuItem);
        StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_PREFS, paramFilterMenuItem);
      }

      if (showModelGraph) {
        final JMenuItem graphPrefsMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_GRAPH_PREFERENCES), HMIMessages.getString(
            HMIMessages.MENU_GRAPH_PREFERENCES + HMIMessages.KEY).charAt(0));
        graphPrefsMenuItem.addActionListener(new GraphPreferencesOpener());
        prefsMenu.add(graphPrefsMenuItem);
      }
      final JMenuItem manageRecentModelsMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_MANAGE_RECENTMODELS), HMIMessages.getString(
          HMIMessages.MENU_MANAGE_RECENTMODELS + HMIMessages.KEY).charAt(0));
      manageRecentModelsMenuItem.addActionListener(new RecentModelsManagementOpener());
      prefsMenu.add(manageRecentModelsMenuItem);

      menuBar.add(prefsMenu);

    }
  }

  public void addHelpMenu(final JMenuBar menuBar) {
    final JMenu helpMenu = new JMenu(HMIMessages.getString(HMIMessages.MENU_HELP));
    helpMenu.setMnemonic(HMIMessages.getString(HMIMessages.MENU_HELP + HMIMessages.KEY).charAt(0));
    final JMenuItem aboutMenuItem = new JMenuItem(HMIMessages.getString(HMIMessages.MENU_ABOUT), HMIMessages
        .getString(HMIMessages.MENU_ABOUT + HMIMessages.KEY).charAt(0));
    aboutMenuItem.addActionListener(new AboutBoxOpener());
    helpMenu.add(aboutMenuItem);
    menuBar.add(helpMenu);
  }

  public JMenuBar getMenuBar() {
    if (menuBar == null) {
      final Set<String> hideItemsSet = new HashSet<String>();
      // hideItemsSet.add(HMIMessages.MENU_TEMPLATES);
      hideItemsSet.add(HMIMessages.MENU_TRACING);
      menuBar = createDefaultMenu(null, hideItemsSet);

      addPrefsMenu(menuBar);
      addHelpMenu(menuBar);
      StateMachine.getInstance().compile();
      StateMachine.getInstance().transitionTo(StateMachine.READY);
    }
    return menuBar;
  }

  public JScrollPane getParameterScrollPane() {
    if (parameterScrollPane == null) {
      parameterScrollPane = new JScrollPane(getParameterFormPanel());
      parameterScrollPane.setName("parameters scroll");
    }
    return parameterScrollPane;
  }

  public JPanel getParameterFormPanel() {
    if (paremeterFormPanel == null) {
      paremeterFormPanel = new JPanel(new BorderLayout());
      paremeterFormPanel.setName("parameters panel");
    }
    return paremeterFormPanel;
  }

  public JScrollPane getModelGraphScrollPane() {
    if (modelGraphScrollPane == null) {
      modelGraphScrollPane = new JScrollPane(getModelGraphWindow());
      modelGraphScrollPane.setName("modelGraphScrollPane");
    }
    return modelGraphScrollPane;
  }

  public JPanel getConfigPanel() {
    if (configPanel == null) {
      configPanel = new JPanel(/* new GridLayout(1, nrColumns) */);
      configPanel.setName("configPanel");
    }
    return configPanel;
  }

  public TabWindow getModelGraphWindow() {
    if (modelGraphTabWindow == null) {
      modelGraphTabWindow = new TabWindow();
      modelGraphTabWindow.add(getModelGraphPanel());
    }

    return modelGraphTabWindow;
  }

  public TabbedPanel getModelGraphPanel() {
    if (modelGraphTabPanel == null) {

      final JButton closeButton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
          getClass().getResource("/com/isencia/passerelle/hmi/resources/close.png"))));
      closeButton.setMaximumSize(new Dimension(16, 16));
      closeButton.setFocusPainted(false);
      closeButton.setBorderPainted(false);
      closeButton.setContentAreaFilled(false);

      modelGraphTabPanel = new TabbedPanel();
      modelGraphTabPanel.getProperties().setTabLayoutPolicy(TabLayoutPolicy.COMPRESSION);
      modelGraphTabPanel.getProperties().getTabAreaComponentsProperties().setStretchEnabled(true);
      modelGraphTabPanel.setTabAreaComponents(new JComponent[] { closeButton });
      modelGraphTabPanel.getProperties().setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB);
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          closeSelectedTab();
        }
      });

      modelGraphTabPanel.addTabListener(new TabListener() {

        public void tabSelected(final TabStateChangedEvent event) {
          // refresh the parameters panel upon the selected model
          // graph
          updateParamsPanel(modelGraphTabPanel.getSelectedTab());

        }

        public void tabRemoved(final TabRemovedEvent event) {
          // refresh the parameters panel upon the selected model
          // graph
          updateParamsPanel(modelGraphTabPanel.getSelectedTab());
        }

        private void updateParamsPanel(final Tab tab) {
          if (tab != null) {
            final String modelURL = tab.getName();
            if (modelURL != null
            /*
             * && !modelURL.toString().equals( getModelURL().toString())
             */) {
              try {
                refreshParamsForm(new URI(modelURL), null);
              } catch (final MalformedURLException e) {
                e.printStackTrace();
              } catch (final Exception e) {
                e.printStackTrace();
              }
            }
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
      });

    }
    return modelGraphTabPanel;
  }

  protected void closeSelectedTab() {
    try {
      Tab modelRootTab = graphTabsMap.get(getModelURL().toURI().toString());
      Tab selectedTab = modelGraphTabPanel.getSelectedTab();
      if (!modelRootTab.equals(selectedTab)) {
        modelGraphTabPanel.removeTab(selectedTab);
      } else {
        close(getModelURL());
      }
    } catch (URISyntaxException e) {
      logger.error("", e);
    }
  }

  @Override
  public void close(URL modelURL) {
    super.close(modelURL);
    if (getModelGraphPanel().getTabCount() == 0) {
      StateMachine.getInstance().transitionTo(StateMachine.READY);
    }
  }

  @Override
  public boolean delete(URI modelURI) throws IllegalStateException {
    boolean deleted = super.delete(modelURI);
    if (deleted) {
      // Soleil Mantis 26538 : also need to clean up related layout prefs
      // these are stored based on the name of the parsed model...
      // But the FILE IS GONE!!!!
      // So we try to get things working by just checking if we can find the model key
      // from the URI in the known layout preferences...
      try {
        String modelKey = getHmiDef().getModelKey(modelURI);
        if (modelKey == null) {
          modelKey = getHmiModelsDef().getModelKey(modelURI);
        }
        if (modelKey == null) {
          modelKey = getExpectedModelName(modelURI);
        }
        if (modelKey != null) {
          getHmiDef().getModelLayoutPrefs().remove(modelKey);
          getHmiDef().removeModel(modelKey);
          saveAndApplySettings();
        }
      } catch (Exception e) {
        logger.warn("Unable to read model to clean up layout prefs after deleting " + modelURI, e);
      }
    }
    return deleted;
  }

  public JPanel getTracePanel() {
    if (tracePanel == null) {
      final TraceVisualizer traceComponent = getTraceComponent();
      final TraceDialog traceDialog = (TraceDialog) traceComponent;

      tracePanel = new JPanel(new BorderLayout());
      tracePanel.setName("tracePanel");
      tracePanel.add("Center", traceDialog.getContentPane());
    }
    return tracePanel;
  }

  public Frame getDialogHookComponent() {
    final JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, parameterScrollPane);
    return parentFrame;
  }

  public void doExitApplication() {
    // Save HMI def with list of predefined and/or recently opened models
    if (HMIBase.HMI_RECENTMODELS_FILE_STRING != null) {
      String def = ModelBundle.generateDef(getHmiModelsDef());
      Writer defWriter = null;
      try {
        defWriter = new FileWriter(HMIBase.HMI_RECENTMODELS_FILE_STRING);
        defWriter.write(def);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (defWriter != null)
          try {
            defWriter.close();
          } catch (Exception e) {
          }
      }
    }
  }

  public IPasserelleEditorPaneFactory getEditorPaneFactory() {
    return editorPaneFactory;
  }

  public void setEditorPaneFactory(final IPasserelleEditorPaneFactory editorPaneFactory) {
    this.editorPaneFactory = editorPaneFactory;
  }
}
