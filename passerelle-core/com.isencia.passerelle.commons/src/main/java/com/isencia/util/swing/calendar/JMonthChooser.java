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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * JMonthChooser is a bean for choosing a month.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JMonthChooser extends JPanel implements ItemListener, AdjustmentListener, FocusListener {

  public static final int RIGHT_SPINNER = 0;
  public static final int LEFT_SPINNER = 1;
  public static final int NO_SPINNER = 2;

  /**
   * Default JMonthChooser constructor.
   */
  public JMonthChooser() {
    this(RIGHT_SPINNER);
  }

  /**
   * JMonthChooser constructor with month spinner parameter.
   * 
   * @param spinner Possible values are RIGHT_SPINNER, LEFT_SPINNER, NO_SPINNER
   */
  public JMonthChooser(int spinner) {
    super();

    setLayout(new BorderLayout());

    comboBox = new Choice() {
      public boolean isFocusTraversable() {
        return false;
      };
    };
    comboBox.addItemListener(this);

    // comboBox.setEditable(false);
    dayChooser = null;
    locale = Locale.getDefault();
    initNames();
    setMonth(Calendar.getInstance().get(Calendar.MONTH));
    add(comboBox, BorderLayout.CENTER);

    if (spinner != NO_SPINNER) {
      // 10000 possible clicks in both directions should be enough :)
      JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, -10000, 10000) {
        public boolean isFocusTraversable() {
          return false;
        };
      };
      scrollBar.setPreferredSize(new Dimension(scrollBar.getPreferredSize().width, this.getPreferredSize().height));
      scrollBar.setVisibleAmount(0);
      scrollBar.addAdjustmentListener(this);

      if (spinner == RIGHT_SPINNER)
        add(scrollBar, BorderLayout.EAST);
      else
        add(scrollBar, BorderLayout.WEST);
    }
    initialized = true;

  }

  public boolean isFocusTraversable() {
    return false;
  }

  /**
   * Initializes the locale specific month names.
   */
  public void initNames() {
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
    String[] monthNames = dateFormatSymbols.getMonths();
    if (comboBox.getItemCount() == 12) comboBox.removeAll();
    for (int i = 0; i < 12; i++)
      comboBox.addItem(monthNames[i]);
    comboBox.select(month);
  }

  /**
   * The ItemListener for the months.
   */
  public void itemStateChanged(ItemEvent iEvt) {
    int index = comboBox.getSelectedIndex();
    if (index >= 0) setMonth(index, false);
  }

  /**
   * The 2 buttons are implemented with a JScrollBar.
   */
  public void adjustmentValueChanged(AdjustmentEvent e) {
    boolean increase = true;
    int newScrollBarValue = e.getValue();
    if (newScrollBarValue > oldScrollBarValue) increase = false;
    oldScrollBarValue = newScrollBarValue;
    int month = getMonth();
    if (increase) {
      month += 1;
      if (month == 12) {
        month = 0;
        if (yearChooser != null) {
          int year = yearChooser.getYear();
          year += 1;
          yearChooser.setYear(year);
        }
      }
    } else {
      month -= 1;
      if (month == -1) {
        month = 11;
        if (yearChooser != null) {
          int year = yearChooser.getYear();
          year -= 1;
          yearChooser.setYear(year);
        }
      }
    }
    setMonth(month);
  }

  private void setMonth(int newMonth, boolean select) {
    int oldMonth = month;
    month = newMonth;
    if (select) comboBox.select(month);
    if (dayChooser != null) dayChooser.setMonth(month);
    firePropertyChange("month", oldMonth, month);
  }

  /**
   * Sets the month. This is a bound property.
   * 
   * @see #getMonth
   */
  public void setMonth(int newMonth) {
    setMonth(newMonth, true);
  }

  /**
   * Returns the month.
   * 
   * @see #setMonth
   */
  public int getMonth() {
    return month;
  }

  /**
   * Convenience method set a day chooser.
   * 
   * @param dayChooser the day chooser
   */
  public void setDayChooser(JDayChooser dayChooser) {
    this.dayChooser = dayChooser;
  }

  /**
   * Convenience method set a year chooser. If set, the spin buttons will spin
   * the year as well
   * 
   * @param dayChooser the day chooser
   */
  public void setYearChooser(JYearChooser yearChooser) {
    this.yearChooser = yearChooser;
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
   * Set the locale and initializes the new month names.
   * 
   * @see #getLocale
   */
  public void setLocale(Locale l) {
    if (!initialized)
      super.setLocale(l);
    else {
      locale = l;
      initNames();
    }
  }

  /**
   * Creates a JFrame with a JMonthChooser inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("MonthChooser");
    frame.getContentPane().add(new JMonthChooser());
    frame.pack();
    frame.setVisible(true);
  }

  private Locale locale;
  private int month;
  private int oldScrollBarValue = 0; // needed for comparison
  private JDayChooser dayChooser = null;
  private JYearChooser yearChooser = null;
  private Choice comboBox;
  private boolean initialized = false;

  /**
   * @see FocusListener#focusGained(FocusEvent)
   */
  public void focusGained(FocusEvent e) {

  }

  /**
   * @see FocusListener#focusLost(FocusEvent)
   */
  public void focusLost(FocusEvent e) {
  }

}
