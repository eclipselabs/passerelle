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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JFrame;

/**
 * JLocaleChooser is a bean for choosing locales.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JLocaleChooser extends JComboBox implements ItemListener {
  /**
   * Default JLocaleChooser constructor.
   */
  public JLocaleChooser() {
    super();
    addItemListener(this);
    locales = Calendar.getAvailableLocales();
    localeCount = locales.length;

    for (int i = 0; i < localeCount; i++) {
      if (locales[i].getCountry().length() > 0) {
        addItem(locales[i].getDisplayName());
      }
    }

    setLocale(Locale.getDefault());
  }

  /**
   * The ItemListener for the locales.
   */
  public void itemStateChanged(ItemEvent iEvt) {
    String item = (String) iEvt.getItem();
    int i;

    for (i = 0; i < localeCount; i++) {
      if (locales[i].getDisplayName().equals(item)) break;
    }
    setLocale(locales[i], false);
  }

  /**
   * Sets the locale.
   * 
   * @see #getLocale
   */
  private void setLocale(Locale l, boolean select) {
    Locale oldLocale = locale;
    locale = l;
    int n = 0;

    if (select) {
      for (int i = 0; i < localeCount; i++) {
        if (locales[i].getCountry().length() > 0) {
          if (locales[i].equals(locale)) setSelectedIndex(n);
          n += 1;
        }
      }
    }

    firePropertyChange("locale", oldLocale, locale);
  }

  /**
   * Sets the locale. This is a bound property.
   * 
   * @see #getLocale
   */
  public void setLocale(Locale l) {
    setLocale(l, true);
  }

  /**
   * Returns the locale.
   * 
   * @see #setLocale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Creates a JFrame with a JLocaleChooser inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("LocaleChooser");
    frame.getContentPane().add(new JLocaleChooser());
    frame.pack();
    frame.setVisible(true);
  }

  private Locale[] locales;
  private Locale locale;
  private int localeCount;
}
