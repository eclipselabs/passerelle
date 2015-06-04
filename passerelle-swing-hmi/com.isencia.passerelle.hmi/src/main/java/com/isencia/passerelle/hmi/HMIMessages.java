/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author erwin.de.ley@isencia.be
 */
public class HMIMessages {
  public final static String BUNDLE_NAME = "com.isencia.passerelle.hmi.resources.hmi-messages";//$NON-NLS-1$
  public final static String ERROR_GENERIC = "hmi.error.generic"; //$NON-NLS-1$
  public final static String GRAPH_TITLE = "hmi.graph.title"; //$NON-NLS-1$
  public final static String INFO_EXIT = "hmi.info.exit"; //$NON-NLS-1$
  public final static String INFO_START = "hmi.info.start"; //$NON-NLS-1$
  public final static String KEY = ".key"; //$NON-NLS-1$
  public final static String MENU_ABOUT = "hmi.menu.about"; //$NON-NLS-1$
  public final static String MENU_ACTOR_ORDER = "hmi.menu.actorOrder"; //$NON-NLS-1$
  public final static String MENU_ACTORLIBRARY_RELOAD = "hmi.menu.actorLib.reload"; //$NON-NLS-1$
  public final static String MENU_ACTORLIBRARY_RELOAD_TOOLTIP = "hmi.menu.actorLib.reload.tooltip"; //$NON-NLS-1$
  public final static String MENU_ANIMATE = "hmi.menu.animate"; //$NON-NLS-1$
  public final static String MENU_CLOSE = "hmi.menu.close"; //$NON-NLS-1$
  public final static String MENU_CLOSEALL = "hmi.menu.close.all"; //$NON-NLS-1$
  public final static String MENU_DEBUG = "hmi.menu.debug"; //$NON-NLS-1$
  public final static String MENU_DEBUG_STEP = "hmi.menu.debug.step"; //$NON-NLS-1$
  public final static String MENU_EXECUTE = "hmi.menu.execute"; //$NON-NLS-1$
  public final static String MENU_EXIT = "hmi.menu.exit"; //$NON-NLS-1$
  public final static String MENU_FILE = "hmi.menu.file"; //$NON-NLS-1$
  public final static String MENU_GRAPH = "hmi.menu.graph"; //$NON-NLS-1$
  public final static String MENU_GRAPH_PREFERENCES = "hmi.menu.graph.prefs"; //$NON-NLS-1$
  public final static String MENU_INTERACTIVE_ERRORHANDLING = "hmi.menu.interactive_errorhandling"; //$NON-NLS-1$
  public final static String MENU_LAYOUT = "hmi.menu.layout"; //$NON-NLS-1$
  public final static String MENU_MANAGE_RECENTMODELS = "hmi.menu.recentModels"; //$NON-NLS-1$
  public final static String MENU_MONITORING = "hmi.menu.monitoring"; //$NON-NLS-1$
  public final static String MENU_NEW = "hmi.menu.new"; //$NON-NLS-1$
  public final static String MENU_OPEN = "hmi.menu.open"; //$NON-NLS-1$
  public final static String MENU_PARAM_VISIBILITY = "hmi.menu.paramVisibility"; //$NON-NLS-1$
  public final static String MENU_CLEAR_LAYOUTPREFS = "hmi.menu.clearLayoutPrefs"; //$NON-NLS-1$
  public final static String MENU_PREFS = "hmi.menu.prefs"; //$NON-NLS-1$
  public final static String MENU_PRINT = "hmi.menu.print"; //$NON-NLS-1$
  public final static String MENU_RESUME = "hmi.menu.resume"; //$NON-NLS-1$
  public final static String MENU_RUN = "hmi.menu.run"; //$NON-NLS-1$
  public final static String MENU_SAVE = "hmi.menu.save"; //$NON-NLS-1$
  public final static String MENU_SAVEAS = "hmi.menu.saveAs"; //$NON-NLS-1$
  public final static String MENU_SAVEALL = "hmi.menu.saveAll"; //$NON-NLS-1$
  public final static String MENU_SHOW = "hmi.menu.show"; //$NON-NLS-1$
  public final static String MENU_STOP = "hmi.menu.stop"; //$NON-NLS-1$
  public final static String MENU_SUSPEND = "hmi.menu.suspend"; //$NON-NLS-1$
  public final static String MENU_TEMPLATES = "hmi.menu.template"; //$NON-NLS-1$
  public final static String MENU_TRACING = "hmi.menu.tracing"; //$NON-NLS-1$
  public final static String MENU_HELP = "hmi.menu.help"; //$NON-NLS-1$
  public final static String TITLE = "hmi.title"; //$NON-NLS-1$
  public final static String FILECHOOSER_DELETE_TITLE = "hmi.fileChooser.delete.title"; //$NON-NLS-1$
  public final static String FILECHOOSER_DELETE_MESSAGE = "hmi.fileChooser.delete.message"; //$NON-NLS-1$
  public static final String FILECHOOSER_DELETE_IMPOSSIBLE = "hmi.fileChooser.delete.impossible"; //$NON-NLS-1$
  public static final String FILECHOOSER_DELETE_FAILED = "hmi.fileChooser.delete.failed"; //$NON-NLS-1$
  public final static String HMI_ERROR_FILE_NEW = "error.file.new"; //$NON-NLS-1$
  public final static String HMI_ERROR_FILE_OPEN = "error.file.open"; //$NON-NLS-1$
  public final static String HMI_ERROR_FILE_SAVE = "error.file.save"; //$NON-NLS-1$
  public final static String HMI_ERROR_FILE_OPEN_INFO = "error.file.open.info"; //$NON-NLS-1$
  
  protected HMIMessages() {
  }

  public static String getString(String key) {
    return getString(key, null);
  }

  public static String getString(String key, String bundle) {
    if (bundle == null) bundle = BUNDLE_NAME;
    try {
      ResourceBundle b = ResourceBundle.getBundle(bundle);
      return b.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
  }
}
