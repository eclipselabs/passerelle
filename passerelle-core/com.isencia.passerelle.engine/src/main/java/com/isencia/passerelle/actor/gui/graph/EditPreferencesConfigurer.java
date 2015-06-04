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
package com.isencia.passerelle.actor.gui.graph;

import javax.swing.BoxLayout;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.util.MessageHandler;

/**
 * This class is an editor widget to show/hide port names of all actors in a model.
 * 
 * @see Configurer
 */
public class EditPreferencesConfigurer extends Query implements ChangeListener, QueryListener {

  private static final String VERSIONVALIDATION_CHECK_BOX = "versionValidationCheckBox";
  private static final String PARAMETERS_CHECK_BOX = "parametersCheckBox";
  private static final String PORT_NAMES_CHECK_BOX = "portNamesCheckBox";

  private PtolemyPreferences preferences = null;
  private Parameter validateModelParameter;
  private Parameter showPortNamesParameter;
  private StringParameter showParametersParameter;

  /**
   * Construct a configurer for the specified entity.
   * 
   * @param composite
   *          The entity to configure.
   */
  public EditPreferencesConfigurer(Configuration configuration) {
    super();

    this.addQueryListener(this);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    try {
      preferences = PtolemyPreferences.getPtolemyPreferencesWithinConfiguration(configuration);
      validateModelParameter = (Parameter) preferences.getAttribute("_validateModels");
      if (validateModelParameter == null) {
        validateModelParameter = new Parameter(preferences, "_validateModels");
        validateModelParameter.setToken(new BooleanToken(true));
      }
      showPortNamesParameter = (Parameter) preferences.getAttribute("_showPortNames");
      if (showPortNamesParameter == null) {
        showPortNamesParameter = new Parameter(preferences, "_showPortNames");
        showPortNamesParameter.setToken(new BooleanToken(false));
      }
      showParametersParameter = (StringParameter) preferences.getAttribute("_showParameters");
      addCheckBox(VERSIONVALIDATION_CHECK_BOX, "Validate models after opening", ((BooleanToken) validateModelParameter.getToken()).booleanValue());
      addCheckBox(PORT_NAMES_CHECK_BOX, "Show port names", ((BooleanToken) showPortNamesParameter.getToken()).booleanValue());
      addCheckBox(PARAMETERS_CHECK_BOX, "Show parameters", "All".equals(showParametersParameter.getExpression()));
    } catch (Exception ex) {
      MessageHandler.error("Error reading preferences", ex);
    }
  }

  public void apply() {
    try {
      boolean validateModels = getBooleanValue(VERSIONVALIDATION_CHECK_BOX);
      boolean showPortNames = getBooleanValue(PORT_NAMES_CHECK_BOX);
      boolean showParameters = getBooleanValue(PARAMETERS_CHECK_BOX);

      if (showParameters)
        showParametersParameter.setToken(new StringToken("All"));
      else
        showParametersParameter.setToken(new StringToken("None"));

      // specific for passerelle : also maintain this as a Ptolemy Preference
      showPortNamesParameter.setToken(new BooleanToken(showPortNames));
      validateModelParameter.setToken(new BooleanToken(validateModels));
      preferences.setAsDefault();
      preferences.save();
    } catch (Exception e) {
      MessageHandler.error("Error setting preferences", e);
    }
  }

  /**
   * React to the fact that the change has been successfully executed by doing nothing.
   * 
   * @param change
   *          The change that has been executed.
   */
  public void changeExecuted(ChangeRequest change) {
    // Nothing to do.
  }

  /**
   * React to the fact that the change has failed by reporting it.
   * 
   * @param change
   *          The change that was attempted.
   * @param exception
   *          The exception that resulted.
   */
  public void changeFailed(ChangeRequest change, Exception exception) {
    // Ignore if this is not the originator.
    if ((change != null) && (change.getSource() != this)) {
      return;
    }

    if ((change != null) && !change.isErrorReported()) {
      change.setErrorReported(true);
      MessageHandler.error("Preferences change failed: ", exception);
    }
  }

  /**
   * Called to notify that one of the entries has changed. This simply sets a flag that enables application of the
   * change when the apply() method is called.
   * 
   * @param name
   *          The name of the entry that changed.
   */
  public void changed(String name) {
  }

}
