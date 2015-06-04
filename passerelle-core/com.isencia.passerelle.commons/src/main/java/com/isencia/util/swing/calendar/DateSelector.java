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
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import com.isencia.util.swing.layout.AbsoluteConstraints;
import com.isencia.util.swing.layout.AbsoluteLayout;

/**
 * A swing bases date selector
 */

public class DateSelector extends JPanel implements PropertyChangeListener {

  public void init() {

    setLayout(new BorderLayout());
    calendarPanel = new JCalendar();
    btnDateVisible = new javax.swing.JButton();
    txtDateField = new javax.swing.JTextField();
    txtDateField.setEditable(false);
    setLayout(new AbsoluteLayout());
    btnDateVisible.setText("...");
    btnDateVisible.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setPanelVisibility(evt);
      }
    });

    add(btnDateVisible, new AbsoluteConstraints(110, 0, 30, 20));
    add(txtDateField, new AbsoluteConstraints(0, 0, 110, 20));

    calendarPanel.addPropertyChangeListener(this);

    calendarPanel.setBorder(new LineBorder(Color.black));
    formatter = new SimpleDateFormat("dd/MM/yyyy");
    popup = new JPopupMenu();

  }

  public boolean isManagingFocus() {
    return true;
  }

  private void setPanelVisibility(java.awt.event.ActionEvent evt) {
    oldCalendar = (Calendar) calendarPanel.getCalendar().clone();
    calendarPanel.setVisible(true);

    popup.add(calendarPanel);
    popup.show(txtDateField, 0, 25);

    calendarPanel.setSelectedDay();
  }

  public void setDateFormatter(SimpleDateFormat formatter) {
    this.formatter = formatter;
  }

  public SimpleDateFormat getDateFormatter() {
    return formatter;
  }

  public void propertyChange(PropertyChangeEvent evt) {

    if (evt.getPropertyName().equals("calendar")) {
      calendar = (Calendar) evt.getNewValue();
      txtDateField.setText(formatter.format(calendar.getTime()));
    }

    if (evt.getPropertyName().equals("closeMe")) {
      calendar = (Calendar) evt.getNewValue();
      txtDateField.setText(formatter.format(calendar.getTime()));
      calendarPanel.setVisible(false);
      popup.setVisible(false);
    }

    if (evt.getPropertyName().equals("escape")) {
      calendar = oldCalendar;
      txtDateField.setText(formatter.format(calendar.getTime()));
      calendarPanel.setCalendar(calendar);
      calendarPanel.setVisible(false);
      popup.setVisible(false);

    }

  }

  public String getDateAsString() {
    return txtDateField.getText();
  }

  static public void main(String[] s) {
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };

    try {
      JFrame frame = new JFrame("JCalendar Demo");
      frame.addWindowListener(l);
      DateSelector demo = new DateSelector();
      demo.init();
      String sdate = "02/02/2002";
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      Date d = sdf.parse(sdate);
      demo.setDate(d);
      frame.getContentPane().add(demo);
      frame.pack();
      frame.setVisible(true);

    } catch (ParseException e) {
    }
  }

  public void setDate(Date newDate) {

    Calendar newCal = Calendar.getInstance();
    newCal.setTime(newDate);

    calendarPanel.setCalendar(newCal);

  }

  public Date getDate() {

    Date calendarPanelDate = null;
    if (calendarPanel != null && calendarPanel.getCalendar() != null) {
      calendarPanelDate = calendarPanel.getCalendar().getTime();
    }
    return (calendarPanelDate);
  }

  private JCalendar calendarPanel;
  private JTextField txtDateField;
  private Calendar calendar;
  private Calendar oldCalendar;
  private JButton btnDateVisible;
  private SimpleDateFormat formatter = null;
  private JPopupMenu popup = null;

  /**
   * @see Component#isFocusTraversable()
   */
  public boolean isFocusTraversable() {
    return false;
  }

}
