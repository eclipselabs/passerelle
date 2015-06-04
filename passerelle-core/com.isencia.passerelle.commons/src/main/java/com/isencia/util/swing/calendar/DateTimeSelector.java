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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import com.isencia.util.swing.layout.AbsoluteConstraints;
import com.isencia.util.swing.layout.AbsoluteLayout;

/**
 * A swing bases date/time selector
 */

public class DateTimeSelector extends JPanel implements PropertyChangeListener {

  FocusManager previousFocusManager = null;

  public void init() {

    setLayout(new BorderLayout());
    calendarPanel = new JCalendar();
    txtTimeField = new TimeSelector();
    btnDateVisible = new javax.swing.JButton();
    txtDateField = new javax.swing.JTextField();
    txtDateField.setEditable(false);
    setLayout(new AbsoluteLayout());
    add(txtTimeField, new AbsoluteConstraints(140, 0, 80, -1));
    btnDateVisible.setText("...");
    btnDateVisible.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setPanelVisibility(evt);
      }
    });

    add(btnDateVisible, new AbsoluteConstraints(110, 0, 30, 20));
    add(txtDateField, new AbsoluteConstraints(0, 0, 110, 20));

    txtTimeField.addPropertyChangeListener(this);
    calendarPanel.addPropertyChangeListener(this);
    calendarPanel.setBorder(new LineBorder(Color.black));
    formatter = new SimpleDateFormat("dd MMM yyyy");
    popup = new JPopupMenu();
  }

  public SimpleDateFormat getDateFormatter() {
    return formatter;
  }

  public boolean isManagingFocus() {
    return true;
  }

  public void setTimeFieldProperties(int format, int precisionType, int precision) {
    String oldTime = txtTimeField.getTime();
    SimpleDateFormat oldFormatter = txtTimeField.getTimeFormatter();
    Date oldDate = null;
    try {
      oldDate = oldFormatter.parse(oldTime);
    } catch (Exception e) {
    }
    txtTimeField = new TimeSelector(format, precisionType, precision);
    txtTimeField.setTime(txtTimeField.getTimeFormatter().format(oldDate));
    txtTimeField.addPropertyChangeListener(this);
  }

  private void setPanelVisibility(java.awt.event.ActionEvent evt) {

    oldCalendar = (Calendar) calendarPanel.getCalendar().clone();
    calendarPanel.setVisible(true);

    popup.add(calendarPanel);
    popup.show(txtDateField, 0, 25);

    previousFocusManager = FocusManager.getCurrentManager();
    FocusManager.setCurrentManager(null);

    calendarPanel.setSelectedDay();
  }

  public void propertyChange(PropertyChangeEvent evt) {

    if (evt.getPropertyName().equals("calendar")) {
      calendar = (Calendar) evt.getNewValue();
      txtDateField.setText(formatter.format(calendar.getTime()));
    }
    if (evt.getPropertyName().equals("closeMe")) {
      calendar = (Calendar) evt.getNewValue();
      txtDateField.setText(formatter.format(calendar.getTime()));
      FocusManager.setCurrentManager(previousFocusManager);
      calendarPanel.setVisible(false);
      popup.setVisible(false);
    }
    if (evt.getPropertyName().equals("escape")) {
      calendar = oldCalendar;
      txtDateField.setText(formatter.format(calendar.getTime()));
      calendarPanel.setCalendar(calendar);
      FocusManager.setCurrentManager(previousFocusManager);
      calendarPanel.setVisible(false);
      popup.setVisible(false);
    }
  }

  public String getTimeAsString() {
    return txtTimeField.getTime();
  }

  public String getDateAsString() {
    return txtDateField.getText();
  }

  public String getDateTimeAsString() {
    return getDateAsString() + " " + getTimeAsString();
  }

  public void setDate(Date newDate) {

    Calendar newCal = Calendar.getInstance();
    newCal.setTime(newDate);
    calendarPanel.setCalendar(newCal);

    String strTime = txtTimeField.getTimeFormatter().format(newDate);
    txtTimeField.setTime(strTime);

  }

  private JCalendar calendarPanel;
  private JTextField txtDateField;
  private TimeSelector txtTimeField;
  private Calendar calendar;
  private Calendar oldCalendar;
  private JButton btnDateVisible;
  private SimpleDateFormat formatter;
  private JPopupMenu popup = null;

  static public void main(String[] s) {
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };

    try {
      JFrame frame = new JFrame("JCalendar Demo");
      frame.addWindowListener(l);
      DateTimeSelector demo = new DateTimeSelector();
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

}
