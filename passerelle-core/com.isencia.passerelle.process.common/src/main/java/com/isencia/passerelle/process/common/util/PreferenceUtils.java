/**
 * 
 */
package com.isencia.passerelle.process.common.util;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.preferences.PreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This is a utility class that should always be used when needing to retrieve preferences Otherwise you'll might end up
 * getting the preferences from the wrong location
 * 
 * Using a lightly hacked edition of the equinox prefs bundle, this can work also outside a running OSGi/Equinox
 * platform, e.g. in plain Java/JUnit test runs.
 * 
 * @author durdav
 * @author delerw
 * 
 */
public class PreferenceUtils {

  public static final String APPLICATION_ROOT_KEY = "com.isencia.sherpa.preferences";
  public static final String CONFIG_ROOT_KEY = "config";
  private static final String BACKENDS_NODE = "backends";
  public static final String NOTIFICATION_NODE = "notifications";
  public static final String THRESHOLDS_ROOT_KEY = "thresholds";
  public static final String REFDATA_ROOT_KEY = "refdata";

  public static Preferences getRootNode() {

    @SuppressWarnings("restriction")
    Preferences root = PreferencesService.getDefault().getRootNode().node(InstanceScope.SCOPE);
    return root.node(APPLICATION_ROOT_KEY);
  }

  public static Preferences getConfigNode() {
    return getRootNode().node(CONFIG_ROOT_KEY);
  }

  public static Preferences getBackendsConfigNode() {
    return getConfigNode().node(BACKENDS_NODE);
  }

  public static Preferences getNotificationsConfigNode() {
    return getConfigNode().node(NOTIFICATION_NODE);
  }

  public static Preferences getThresholdNode() {
    return getRootNode().node(THRESHOLDS_ROOT_KEY);
  }

  public static Preferences getRefdataNode() {
    return getRootNode().node(REFDATA_ROOT_KEY);
  }

  // public static String lookupNodeValue(Preferences requestedNode, String key, String defaultValue) {
  //
  // String unresolvedValue = null;
  //
  // // Keep going up through the hierarchy until you find a value for the given key
  // do {
  // unresolvedValue = requestedNode.get(key, null);
  // requestedNode = requestedNode.parent();
  // } while (unresolvedValue == null && requestedNode != null);
  //
  // // Nothing found
  // if (unresolvedValue == null) {
  // return defaultValue;
  // }
  //
  // return PropertyPlaceholderServiceProxy.resolve(unresolvedValue);
  // }

  /**
   * Dumps the preferences starting from the given node, on the given writer.
   * 
   * @param writer
   * @param node
   * @throws IOException
   * @throws BackingStoreException
   */
  public static void dumpPreferences(Writer writer, Preferences node) throws IOException, BackingStoreException {
    dumpPreferencesLevel(writer, node, 0);
  }

  private static void dumpPreferencesLevel(Writer writer, Preferences node, int level) throws IOException, BackingStoreException {
    addIndentedNewLine(writer, level);
    writer.append("[Preference : " + node.absolutePath() + "");
    String[] keys = node.keys();
    if (keys != null && keys.length > 0) {
      addIndentedNewLine(writer, level + 1);
      for (String key : keys) {
        writer.append(key + "=" + node.get(key, null) + " ; ");
      }
    }

    String[] childrenNames = node.childrenNames();
    if (childrenNames != null && childrenNames.length > 0) {
      for (String childName : childrenNames) {
        dumpPreferencesLevel(writer, node.node(childName), level + 1);
      }
    }
    addIndentedNewLine(writer, level);
    writer.append("]");
  }

  private static void addIndentedNewLine(Writer writer, int level) throws IOException {
    writer.append("\n");
    for (int i = 0; i < level; ++i)
      writer.append(" ");
  }

  private static final String PREFERENCE_NOT_FOUND = "preference not found";

  public static String getPreference(String name) {
    String[] parts = StringUtils.split(name, "/");
    if (parts.length > 1) {
      Preferences prefs = null;
      for (int i = 0; i < parts.length - 1; i++) {
        prefs = getRootNode().node(parts[i]);

      }
      if (prefs != null) {
        return prefs.get(parts[parts.length - 1], PREFERENCE_NOT_FOUND);
      }
    } else {
      return getRootNode().get(parts[0], PREFERENCE_NOT_FOUND);
    }
    return PREFERENCE_NOT_FOUND;

  }
}
