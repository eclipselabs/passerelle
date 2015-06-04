package com.isencia.passerelle.workbench.model.activator;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
  private static Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  // The plug-in ID
  public static final String PLUGIN_ID = "com.isencia.passerelle.workbench.model";

  // The shared instance
  private static Activator plugin;

  public void start(BundleContext context) throws Exception {

    // Configure the workflow log. First check if the property 'workflow.logback.configurationFile'exists:
    String logConfigFile = System.getProperty("workflow.logback.configurationFile");
    if (logConfigFile != null) {
      // Ok, it exists - check if the corresponding log configuration file exists
      File file = new File(logConfigFile);
      if (file.exists()) {
        LOGGER.debug("Workflow logging configuration file found at '{}'", logConfigFile);
      } else {
        LOGGER.warn("Workflow logging configuration file not found at '{}'", logConfigFile);
        logConfigFile = null;
      }
    } else {
      // Check if the property 'logback.configurationFile' exists:
      logConfigFile = System.getProperty("logback.configurationFile");
      if (logConfigFile != null) {
        File file = new File(logConfigFile);
        if (file.exists()) {
          LOGGER.debug("Logging configuration file found at '{}'", logConfigFile);
          System.setProperty("workflow.logback.configurationFile", logConfigFile);
        } else {
          LOGGER.warn("Logging configuration file not found at '{}'", logConfigFile);
          logConfigFile = null;
        }
      }
    }

    // Load default config file if no property set or config file not found
    if (logConfigFile == null) {
      try {
        // Find the default configuration file
        ProtectionDomain pd = Activator.class.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL url = cs.getLocation();
        File file = new File(url.getFile(), "config/logback.xml");
        String absPath = file.getAbsolutePath().toString();
        if (file.exists()) {
          LOGGER.debug("Default workflow logging configuration file found at '{}'", absPath);
          System.setProperty("workflow.logback.configurationFile", absPath);
        } else {
          LOGGER.error("Default workflow logging configuration file not found at '{}'", absPath);
        }
      } catch (Exception e) {
        LOGGER.error("Could not set up default workflow logging", e);
      }
    }

    super.start(context);

    plugin = this;
  }

  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

}
