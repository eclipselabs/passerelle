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
package com.isencia.util.swing.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * JCalendar is a bean for entering a date by choosing the year, month and day.
 * 
 * @version 1.1 02/14/02
 * @author Kai Toedter
 */
public class JCalendar extends JPanel implements PropertyChangeListener {
  /**
   * Default JCalendar constructor.
   */
  public JCalendar() {
    this(JMonthChooser.RIGHT_SPINNER);
  }

  /**
   * JCalendar constructor with month spinner parameter.
   * 
   * @param monthSpinner Possible values are JMonthChooser.RIGHT_SPINNER,
   *          JMonthChooser.LEFT_SPINNER, JMonthChooser.NO_SPINNER
   */
  public JCalendar(int monthSpinner) {

    // needed for setFont() etc.
    dayChooser = null;
    monthChooser = null;
    yearChooser = null;
    locale = Locale.getDefault();
    calendar = Calendar.getInstance();
    setLayout(new BorderLayout());

    JPanel myPanel = new JPanel();
    myPanel.setLayout(new GridLayout(1, 3));
    monthChooser = new JMonthChooser(monthSpinner);
    yearChooser = new JYearChooser();
    monthChooser.setYearChooser(yearChooser);
    myPanel.add(monthChooser);
    myPanel.add(yearChooser);
    dayChooser = new JDayChooser();
    dayChooser.addPropertyChangeListener(this);
    monthChooser.setDayChooser(dayChooser);
    monthChooser.addPropertyChangeListener(this);
    yearChooser.setDayChooser(dayChooser);
    yearChooser.addPropertyChangeListener(this);
    add(myPanel, BorderLayout.NORTH);
    add(dayChooser, BorderLayout.CENTER);
    initialized = true;
  }

  public void setSelectedDay() {
    dayChooser.setSelectedDay();
  }

  private void setCalendar(Calendar c, boolean update, boolean closeMe) {
    Calendar oldCalendar = calendar;
    calendar = c;

    if (update) {

      // Thanks to Jeff Ulmer for correcting a bug in the sequence :)
      yearChooser.setYear(c.get(Calendar.YEAR));
      monthChooser.setMonth(c.get(Calendar.MONTH));
      dayChooser.setDay(c.get(Calendar.DATE));
    }
    if (closeMe)
      firePropertyChange("closeMe", null, calendar);
    else
      firePropertyChange("calendar", oldCalendar, calendar);
  }

  /**
   * Sets the calendar property. This is a bound property.
   * 
   * @see #getCalendar
   * @param c the new calendar
   */
  public void setCalendar(Calendar c) {
    setCalendar(c, true, false);
  }

  /**
   * Returns the calendar property.
   * 
   * @return the value of the calendar property.
   * @see #setCalendar
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Sets the locale property. This is a bound property.
   * 
   * @see #getLocale
   */
  public void setLocale(Locale l) {
    if (!initialized)
      super.setLocale(l);
    else {
      Locale oldLocale = locale;
      locale = l;
      dayChooser.setLocale(locale);
      monthChooser.setLocale(locale);
      firePropertyChange("locale", oldLocale, locale);
    }
  }

  /**
   * Returns the locale.
   * 
   * @return the value of the locale property.
   * @see #setLocale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Sets the font property.
   * 
   * @param font the new font
   */
  public void setFont(Font font) {
    super.setFont(font);

    if (dayChooser != null) {
      dayChooser.setFont(font);
      monthChooser.setFont(font);
      yearChooser.setFont(font);
    }
  }

  /**
   * Sets the foreground color.
   * 
   * @param fg the new foreground
   */
  public void setForeground(Color fg) {
    super.setForeground(fg);

    if (dayChooser != null) {
      dayChooser.setForeground(fg);
      monthChooser.setForeground(fg);
      yearChooser.setForeground(fg);
    }
  }

  /**
   * Sets the background color.
   * 
   * @param bg the new background
   */
  public void setBackground(Color bg) {
    super.setBackground(bg);

    if (dayChooser != null) dayChooser.setBackground(bg);
  }

  /**
   * JCalendar is a PropertyChangeListener, for its day, month and year chooser.
   */
  public void propertyChange(PropertyChangeEvent evt) {
    if (calendar != null) {
      Calendar c = (Calendar) calendar.clone();

      if (evt.getPropertyName().equals("escape")) {
        firePropertyChange("escape", 1, 0);
      } else if (evt.getPropertyName().equals("day")) {
        c.set(Calendar.DAY_OF_MONTH, ((Integer) evt.getNewValue()).intValue());
        setCalendar(c, false, false);
      } else if (evt.getPropertyName().equals("closeMe")) {
        setCalendar(c, false, true);
      } else if (evt.getPropertyName().equals("month")) {
        c.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());
        setCalendar(c, false, false);
      } else if (evt.getPropertyName().equals("monthPlus1")) {
        if (c.get(Calendar.MONTH) < 12) {
          c.add(Calendar.MONTH, 1);
        } else {
          c.set(Calendar.MONTH, 1);
          c.add(Calendar.YEAR, 1);
        }
        setCalendar(c, true, false);
      } else if (evt.getPropertyName().equals("monthMinus1")) {
        if (c.get(Calendar.MONTH) > 0) {
          c.add(Calendar.MONTH, -1);
        } else {
          c.set(Calendar.MONTH, 11);
          c.add(Calendar.YEAR, -1);
        }
        setCalendar(c, true, false);
      } else if (evt.getPropertyName().equals("year")) {
        c.set(Calendar.YEAR, ((Integer) evt.getNewValue()).intValue());
        setCalendar(c, false, false);
      }
    }
  }

  /**
   * Returns "JCalendar".
   */
  public String getName() {
    return "JCalendar";
  }

  /**
   * Creates a JFrame with a JCalendar inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("JCalendar");
    frame.getContentPane().add(new JCalendar());
    frame.pack();
    frame.setVisible(true);
  }

  private JYearChooser yearChooser;
  private JMonthChooser monthChooser;
  private JDayChooser dayChooser;
  private Calendar calendar;
  private Locale locale;
  private boolean initialized = false;
}