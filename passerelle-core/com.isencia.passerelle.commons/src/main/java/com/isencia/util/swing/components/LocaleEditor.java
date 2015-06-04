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

import java.util.Calendar;
import java.util.Locale;

/**
 * Property editor for locales.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class LocaleEditor extends java.beans.PropertyEditorSupport {
  /**
   * Default LocaleEditor constructor.
   */
  public LocaleEditor() {
    locale = Locale.getDefault();
    locales = Calendar.getAvailableLocales();
    length = locales.length;
    localeStrings = new String[length];
  }

  /**
   * Returns the locale Strings.
   */
  public String[] getTags() {
    for (int i = 0; i < length; i++)
      localeStrings[i] = locales[i].getDisplayName();
    return localeStrings;
  }

  /**
   * Sets the locale Strings as text and invokes setValue( locale ).
   */
  public void setAsText(String text) throws IllegalArgumentException {
    for (int i = 0; i < length; i++)
      if (text.equals(locales[i].getDisplayName())) {
        locale = locales[i];
        setValue(locale);
        break;
      }
  }

  /**
   * Returnss the locale String as text.
   */
  public String getAsText() {
    return locale.getDisplayName();
  }

  private Locale[] locales;
  private String[] localeStrings;
  private Locale locale;
  private int length;
}
