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

import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JTextField;
import com.isencia.util.swing.components.JSpinField;

/**
 * JYearChooser is a bean for choosing a year.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JYearChooser extends JSpinField {
  /**
   * Default JCalendar constructor.
   */
  public JYearChooser() {
    Calendar calendar = Calendar.getInstance();
    dayChooser = null;
    textField.setHorizontalAlignment(JTextField.RIGHT);
    setMinimum(calendar.getMinimum(Calendar.YEAR));
    setMaximum(calendar.getMaximum(Calendar.YEAR));
    setValue(calendar.get(Calendar.YEAR));
  }

  public boolean isFocusable() {
    return false;
  }

  protected void setValue(int newValue, boolean updateTextField, boolean updateScrollbar) {
    int oldYear = year;
    year = newValue;
    super.setValue(newValue, updateTextField, updateScrollbar);
    if (dayChooser != null) dayChooser.setYear(newValue);
    firePropertyChange("year", oldYear, year);
  }

  /**
   * Sets the year. This is a bound property.
   * 
   * @see #getYear
   * @param y the new year
   */
  public void setYear(int y) {
    super.setValue(y);
  }

  /**
   * Returns the year.
   */
  public int getYear() {
    return year;
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
   * Creates a JFrame with a JYearChooser inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("JYearChooser");
    frame.getContentPane().add(new JYearChooser());
    frame.pack();
    frame.setVisible(true);
  }

  private JDayChooser dayChooser;
  private int year;
}
