/**
 * 
 */
package com.isencia.passerelle.hmi.util;

import java.util.prefs.Preferences;

/**
 * @author viguier
 */
public class GenericHMIUserPref {
  public final static String LAYOUT = "GUI.LAYOUT";

  /**
   * Save user preference
   * 
   * @param key
   * @param value
   */
  public static void putIntPref(String key, int value) {
    Preferences.userRoot().putInt(key, value);
  }

  /**
   * Get user preference
   * 
   * @param key
   * @param defaultValue
   * @return
   */
  public static int getIntPref(String key, int defaultValue) {
    return Preferences.userRoot().getInt(key, defaultValue);
  }

  /**
   * Save user preference
   * 
   * @param key
   * @param value
   */
  public static void putByteArrayPref(String key, byte[] value) {
    Preferences.userRoot().putByteArray(key, value);
  }

  /**
   * Get user preference
   * 
   * @param key
   * @param defaultValue
   * @return
   */
  public static byte[] getByteArrayPref(String key, byte[] defaultValue) {
    return Preferences.userRoot().getByteArray(key, defaultValue);
  }

}
