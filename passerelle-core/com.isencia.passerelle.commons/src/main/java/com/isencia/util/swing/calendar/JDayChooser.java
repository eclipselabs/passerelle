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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * JCalendar is a bean for choosing a day.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener, MouseListener {
  /**
   * Default JDayChooser constructor.
   */
  public JDayChooser() {
    locale = Locale.getDefault();
    days = new JButton[49];
    selectedDay = null;
    Calendar calendar = Calendar.getInstance(locale);
    today = (Calendar) calendar.clone();

    setLayout(new GridLayout(7, 7));

    for (int y = 0; y < 7; y++) {
      for (int x = 0; x < 7; x++) {
        int index = x + 7 * y;
        if (y == 0) {
          // Create a button that doesn't react on clicks or focus changes
          // Thanks to Thomas Schaefer for the focus hint :)
          days[index] = new JButton() {
            public void addMouseListener(MouseListener l) {
            };

            // This method has been deprecated by 1.4
            // and will be replaced by isFocusable in future versions
            public boolean isFocusTraversable() {
              return false;
            }
          };
          days[index].setBackground(new Color(180, 180, 200));
        } else {
          days[index] = new JButton("x");
          days[index].addActionListener(this);
          days[index].addKeyListener(this);
          days[index].addFocusListener(this);
          days[index].addMouseListener(this);
        }

        days[index].setMargin(new Insets(0, 0, 0, 0));
        days[index].setFocusPainted(false);
        add(days[index]);
      }
    }
    init();
    setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    initialized = true;
  }

  /**
   * Initilizes the locale specific names for the days of the week.
   */
  protected void init() {
    colorRed = new Color(164, 0, 0);
    colorBlue = new Color(0, 0, 164);
    JButton testButton = new JButton();
    oldDayBackgroundColor = testButton.getBackground();
    selectedColor = new Color(160, 160, 160);

    calendar = Calendar.getInstance(locale);
    int firstDayOfWeek = calendar.getFirstDayOfWeek();
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
    dayNames = dateFormatSymbols.getShortWeekdays();
    int day = firstDayOfWeek;
    for (int i = 0; i < 7; i++) {
      days[i].setText(dayNames[day]);
      if (day == 1)
        days[i].setForeground(colorRed);
      else
        days[i].setForeground(colorBlue);

      if (day < 7)
        day++;
      else
        day -= 6;

    }
    drawDays();
  }

  /**
   * Hides and shows the day buttons.
   */
  protected void drawDays() {
    Calendar tmpCalendar = (Calendar) calendar.clone();
    int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
    tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

    firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
    if (firstDay < 0) firstDay += 7;

    int i;
    firstDay += 7;
    for (i = 7; i < firstDay; i++) {
      days[i].setVisible(false);
      days[i].setText("");
    }

    tmpCalendar.add(Calendar.MONTH, 1);
    Date firstDayInNextMonth = tmpCalendar.getTime();
    tmpCalendar.add(Calendar.MONTH, -1);

    Date day = tmpCalendar.getTime();
    int n = 0;
    Color foregroundColor = getForeground();
    while (day.before(firstDayInNextMonth)) {
      days[i + n].setText(Integer.toString(n + 1));
      days[i + n].setVisible(true);
      if (tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
        days[i + n].setForeground(colorRed);
      } else
        days[i + n].setForeground(foregroundColor);

      if (n + 1 == this.day) {
        days[i + n].setBackground(selectedColor);
        selectedDay = days[i + n];
      } else
        days[i + n].setBackground(oldDayBackgroundColor);

      n++;
      tmpCalendar.add(Calendar.DATE, 1);
      day = tmpCalendar.getTime();
    }

    lastDay = n + i - 1;
    for (int k = lastDay + 1; k < 49; k++) {
      days[k].setVisible(false);
      days[k].setText("");
    }
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
   * Sets the locale.
   * 
   * @see #getLocale
   */
  public void setLocale(Locale l) {
    if (!initialized)
      super.setLocale(l);
    else {
      locale = l;
      init();
    }
  }

  /**
   * Sets the day. This is a bound property.
   * 
   * @param d the day
   * @see #getDay
   */

  public void setSelectedDay() {
    setDay(getDay());
  }

  public void setDay(int d) {
    if (d < 1) d = 1;

    Calendar tmpCalendar = (Calendar) calendar.clone();
    tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
    tmpCalendar.add(Calendar.MONTH, 1);
    tmpCalendar.add(Calendar.DATE, -1);
    int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);

    if (d > maxDaysInMonth) d = maxDaysInMonth;

    int oldDay = day;
    day = d;

    if (selectedDay != null) {
      selectedDay.setBackground(oldDayBackgroundColor);
      selectedDay.repaint(); // Bug: needed for Swing 1.0.3
    }

    for (int i = 7; i < 49; i++) {
      if (days[i].getText().equals(Integer.toString(day))) {
        selectedDay = days[i];
        selectedDay.setBackground(selectedColor);
        if (!selectedDay.hasFocus()) {
          selectedDay.requestFocus();
        }
        break;
      }
    }
    firePropertyChange("day", oldDay, day);

  }

  /**
   * Returns the selected day.
   * 
   * @see #setDay
   */
  public int getDay() {
    return day;
  }

  /**
   * Sets a specific month. This is needed for correct graphical representation
   * of the days.
   * 
   * @param month the new month
   */
  public void setMonth(int month) {
    calendar.set(Calendar.MONTH, month);
    setDay(day);
    drawDays();
  }

  /**
   * Sets a specific year. This is needed for correct graphical representation
   * of the days.
   * 
   * @param year the new year
   */
  public void setYear(int year) {
    calendar.set(Calendar.YEAR, year);
    drawDays();
  }

  /**
   * Sets a specific calendar. This is needed for correct graphical
   * representation of the days.
   * 
   * @param c the new calendar
   */
  public void setCalendar(Calendar c) {
    calendar = c;
    drawDays();
  }

  /**
   * Sets the font property.
   * 
   * @param font the new font
   */
  public void setFont(Font font) {
    if (days != null) {
      for (int i = 0; i < 49; i++)
        days[i].setFont(font);
    }
  }

  /**
   * Sets the foregroundColor color.
   * 
   * @param fg the new foregroundColor
   */
  public void setForeground(Color fg) {
    super.setForeground(fg);
    if (days != null) {
      for (int i = 7; i < 49; i++)
        days[i].setForeground(fg);
      drawDays();
    }
  }

  /**
   * Returns "JDayChooser".
   */
  public String getName() {
    return "JDayChooser";
  }

  /**
   * JDayChooser is the ActionListener for all day buttons.
   */
  public void actionPerformed(ActionEvent e) {
    try {
      JButton button = (JButton) e.getSource();
      int day = (new Integer(button.getText())).intValue();
      setDay(day);
    } catch (Exception ex) {
    }
  }

  /**
   * JDayChooser is the FocusListener for all day buttons.
   */
  public void focusGained(FocusEvent e) {
    if (!isFirst) {
      actionPerformed(new ActionEvent(e.getSource(), 0, null));
    } else {
      isFirst = false;
    }
  }

  /**
   * Does nothing.
   */
  public void focusLost(FocusEvent e) {

  }

  /**
   * JDayChooser is the KeyListener for all day buttons. (Added by Thomas
   * Schaefer)
   */
  public void keyPressed(KeyEvent e) {

    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      firePropertyChange("escape", 1, 0);
      return;
    }

    if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
      firePropertyChange("monthPlus1", 1, 0);
      return;
    }

    if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
      firePropertyChange("monthMinus1", 1, 0);
      return;
    }

    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
      firePropertyChange("closeMe", 1, 0);
      return;
    }

    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      firePropertyChange("closeMe", 1, 0);
      return;
    }

    int offset = e.getKeyCode() == KeyEvent.VK_UP ? -7 : e.getKeyCode() == KeyEvent.VK_DOWN ? +7 : e.getKeyCode() == KeyEvent.VK_LEFT ? -1
        : e.getKeyCode() == KeyEvent.VK_RIGHT ? +1 : 0;

    for (int i = getComponentCount() - 1; i >= 0; --i)
      if (getComponent(i) == selectedDay) {
        i += offset;
        if (i < firstDay && (offset == 1 || offset == -1)) {
          firePropertyChange("monthMinus1", 1, 0);
          setDay(getLastDayOfPreviousMonth());
        } else if (i > lastDay && (offset == 1 || offset == -1)) {
          firePropertyChange("monthPlus1", 1, 0);
          setDay(1);
        } else if (i >= firstDay && i <= lastDay) {
          if (!days[i].hasFocus())
            days[i].requestFocus();
          else
            actionPerformed(new ActionEvent(days[i], 0, null));
        }
        break;
      }
  }

  /**
   * returns the last day of the previous month
   */

  private int getLastDayOfPreviousMonth() {

    Calendar tmpCalendar = (Calendar) calendar.clone();
    if (tmpCalendar.get(Calendar.MONTH) > 0) {
      tmpCalendar.add(Calendar.MONTH, -1);
    } else {
      tmpCalendar.set(Calendar.MONTH, 11);
      tmpCalendar.add(Calendar.YEAR, -1);
    }
    return tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * Does nothing.
   */
  public void keyTyped(KeyEvent e) {
  }

  /**
   * Does nothing.
   */
  public void keyReleased(KeyEvent e) {
  }

  /**
   * Creates a JFrame with a JDayChooser inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("JDayChooser");
    frame.getContentPane().add(new JDayChooser());
    frame.pack();
    frame.setVisible(true);
  }

  private boolean isFirst = true;
  private JButton days[];
  private JButton selectedDay;
  private int day;
  private Color oldDayBackgroundColor;
  private Color selectedColor;
  private Color colorRed;
  private Color colorBlue;
  private String dayNames[];
  private Calendar calendar;
  private Calendar today;
  private Locale locale;
  private boolean initialized = false;
  private int firstDay = -1;
  private int lastDay = -1;

  /**
   * @see MouseListener#mouseClicked(MouseEvent)
   */
  public void mouseClicked(MouseEvent arg0) {
    firePropertyChange("closeMe", 1, 0);
  }

  /**
   * @see MouseListener#mouseEntered(MouseEvent)
   */
  public void mouseEntered(MouseEvent arg0) {
  }

  /**
   * @see MouseListener#mouseExited(MouseEvent)
   */
  public void mouseExited(MouseEvent arg0) {
  }

  /**
   * @see MouseListener#mousePressed(MouseEvent)
   */
  public void mousePressed(MouseEvent arg0) {
  }

  /**
   * @see MouseListener#mouseReleased(MouseEvent)
   */
  public void mouseReleased(MouseEvent arg0) {
  }

  /**
   * Gets the selectedDay.
   * 
   * @return Returns a JButton
   */
  public JButton getSelectedDay() {
    return selectedDay;
  }

}
