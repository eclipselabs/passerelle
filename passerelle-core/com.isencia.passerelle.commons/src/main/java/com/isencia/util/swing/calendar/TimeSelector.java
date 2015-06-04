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
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * JSpinField is a numeric field with 2 spin buttons to increase or decrease the
 * value.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class TimeSelector extends JPanel implements AdjustmentListener, DocumentListener {
  /**
   * Default JSpinField constructor.
   */
  public TimeSelector() {
    super();
    min = -10000;
    max = 10000;

    setLayout(new BorderLayout());
    textField = new TimeTextField();
    textField.getDocument().addDocumentListener(this);
    add(textField, BorderLayout.CENTER);
    scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 100);
    scrollBar.setPreferredSize(new Dimension(scrollBar.getPreferredSize().width, textField.getPreferredSize().height));
    scrollBar.setMinimum(min);
    scrollBar.setMaximum(max);
    scrollBar.setValue(0);

    scrollBar.addAdjustmentListener(this);
    add(scrollBar, BorderLayout.EAST);
  }

  public TimeSelector(int format, int precisionType, int precision) {
    super();
    min = -10000;
    max = 10000;

    setLayout(new BorderLayout());
    textField = new TimeTextField(format, precisionType, precision);
    add(textField, BorderLayout.CENTER);
    scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 100);
    scrollBar.setPreferredSize(new Dimension(scrollBar.getPreferredSize().width, textField.getPreferredSize().height));
    scrollBar.setMinimum(min);
    scrollBar.setMaximum(max);
    scrollBar.setValue(0);

    scrollBar.addAdjustmentListener(this);
    add(scrollBar, BorderLayout.EAST);

  }

  /**
   * The 2 buttons are implemented with a JScrollBar.
   */
  public void adjustmentValueChanged(AdjustmentEvent e) {

    long currentTime = (new Date()).getTime();
    long i = currentTime - lastPressedButtonTime;

    int interval = textField.precision;

    int currentValue = e.getValue();

    if (currentValue < previousValue) {

      if (lastPressedButton == 1) { // UP
        if (i < 100)
          countKeyPressedButton++;
        else
          countKeyPressedButton = 0;
      } else
        countKeyPressedButton = 0;
      if (countKeyPressedButton > 10) interval = interval * 2;
      if (countKeyPressedButton > 50) interval = interval * 10;
      if (countKeyPressedButton > 250) interval = interval * 50;
      lastPressedButton = 1;// UP
      textField.addInterval(interval);

    } else {
      if (lastPressedButton == 0) { // DOWN
        if (i < 100)
          countKeyPressedButton++;
        else
          countKeyPressedButton = 0;
      } else
        countKeyPressedButton = 0;
      if (countKeyPressedButton > 10) interval = interval * 2;
      if (countKeyPressedButton > 50) interval = interval * 10;
      if (countKeyPressedButton > 250) interval = interval * 50;
      lastPressedButton = 0;// DOWN
      textField.addInterval(-interval);

    }
    previousValue = currentValue;
    lastPressedButtonTime = currentTime;
  }

  protected void _fireTimePropertyChange(Date oldTime, Date newTime) {
    firePropertyChange("time", oldTime, newTime);
  }

  /**
   * Creates a JFrame with a JSpinField inside and can be used for testing.
   */
  static public void main(String[] s) {
    WindowListener l = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    JFrame frame = new JFrame("JSpinField");
    frame.addWindowListener(l);
    frame.getContentPane().add(new TimeSelector());
    frame.pack();
    frame.setVisible(true);
  }

  public String getTime() {
    return textField.getText();
  }

  public SimpleDateFormat getTimeFormatter() {
    return textField.formatter;
  }

  public void setTime(String newTime) {
    textField.setTime(newTime);
  }

  protected TimeTextField textField;
  protected JScrollBar scrollBar;
  private int min;
  private int max;
  private int previousValue = 0;
  private int lastPressedButton = -1; // (DOWN=0,UP=1)
  private long lastPressedButtonTime = 0;
  private int countKeyPressedButton = 0;

  // ////////////////////////////////////////////////////////////////////

  class TimeTextField extends JTextField implements KeyListener {

    public SimpleDateFormat formatter = null;
    private boolean noCheck = true;
    public int precisionType = 0;
    public int precision = 0;
    public int format = 0;
    private int currentCaretPosition = 0;
    public Date date = null;

    private int lastPressedKey = -1;
    private long lastPressedKeyTime = -1;
    private long countKeyPressed = 0;

    public final static int FORMAT_12H = 1;
    public final static int FORMAT_24H = 2;
    public final static int PRECISION_HOURS = 2;
    public final static int PRECISION_MINUTES = 5;
    public final static int PRECISION_SECONDS = 8;

    /**
     * Default constructor set the time at the current time with 24h format eg:
     * 23:45:35
     */
    public TimeTextField() {
      this(FORMAT_24H, PRECISION_SECONDS, 1);
    }

    /**
     * Constructor
     * 
     * @param initval = time e.g. "09:56:05 PM"
     * @param format = FORMAT_12H or FORMAT_24H
     * @param precisiontype =
     *          PRECISION_HOURS,PRECISION_MINUTES,PRECISION_SECONDS
     * @param precision = precision of the printed time eg:5 eg :
     *          initval="09:55 PM" format = FORMAT_12H precisiontype =
     *          PRECISION_MINUTES precision=5
     */
    public TimeTextField(int format, int precisionType, int precision) {

      this.format = format;
      this.precisionType = precisionType;
      this.precision = precision;

      formatter = new SimpleDateFormat(getDateFormat());
      setDocument(new TimeDocument());
      Date now = new Date();
      setTime(formatter.format(now));
      addKeyListener(this);
    }

    public SimpleDateFormat getTimeFormatter() {
      return formatter;
    }

    /**
     * Replace the time with the newTime when well formatted
     * 
     * @param newTime : String = the new Time
     */

    public void setTime(String newTime) {
      noCheck = true;

      try {
        Date tmpDate = formatter.parse(newTime);
        currentCaretPosition = getCaretPosition();
        Date oldDate = date != null ? new Date(date.getTime()) : null;
        date = transformDate(tmpDate);
        ((TimeDocument) getDocument()).clear(0, getMaxOffset());
        setText(formatter.format(date));
        _fireTimePropertyChange(oldDate, date);
      } catch (ParseException e) {
        noCheck = false;
      }
    }

    /**
     * add the specified interval
     * 
     * @param interval : int (positive or negative)
     */

    private void addInterval(int interval) {
      noCheck = true;
      currentCaretPosition = getCaretPosition();
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      switch (precisionType) {
      case PRECISION_SECONDS:
        cal.add(Calendar.SECOND, interval);
        break;
      case PRECISION_MINUTES:
        cal.add(Calendar.MINUTE, interval);
        break;
      case PRECISION_HOURS:
        cal.add(Calendar.HOUR, interval);
        break;
      default:
        break;
      }
      Date oldDate = date != null ? new Date(date.getTime()) : null;
      date = cal.getTime();
      ((TimeDocument) getDocument()).clear(0, getMaxOffset());
      setText(formatter.format(date));
      _fireTimePropertyChange(oldDate, date);

    }

    /**
     * @see KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent e) {

      long currentTime = (new Date()).getTime();
      long i = currentTime - lastPressedKeyTime;
      int interval = precision;

      if (e.getKeyCode() == KeyEvent.VK_UP) {
        if (lastPressedKey == KeyEvent.VK_UP) {
          if (i < 100)
            countKeyPressed++;
          else
            countKeyPressed = 0;
        } else
          countKeyPressed = 0;
        if (countKeyPressed > 10) interval = interval * 2;
        if (countKeyPressed > 50) interval = interval * 10;
        if (countKeyPressed > 250) interval = interval * 50;
        addInterval(interval);
      }

      if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        if (lastPressedKey == KeyEvent.VK_DOWN) {
          if (i < 100)
            countKeyPressed++;
          else
            countKeyPressed = 0;
        } else
          countKeyPressed = 0;
        if (countKeyPressed > 10) interval = interval * 2;
        if (countKeyPressed > 50) interval = interval * 10;
        if (countKeyPressed > 250) interval = interval * 50;

        addInterval(-interval);
      }

      if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
        if (lastPressedKey == KeyEvent.VK_PAGE_UP) {
          if (i < 100)
            countKeyPressed++;
          else
            countKeyPressed = 0;
        } else
          countKeyPressed = 0;
        if (countKeyPressed > 10) interval = interval * 2;
        if (countKeyPressed > 50) interval = interval * 10;

        addInterval(5 * interval);
      }

      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
        if (lastPressedKey == KeyEvent.VK_PAGE_DOWN) {
          if (i < 100)
            countKeyPressed++;
          else
            countKeyPressed = 0;
        } else
          countKeyPressed = 0;
        if (countKeyPressed > 10) interval = interval * 2;
        if (countKeyPressed > 50) interval = interval * 10;

        addInterval(-5 * interval);
      }

      lastPressedKey = e.getKeyCode();
      lastPressedKeyTime = currentTime;

    }

    /**
     * @see KeyListener#keyReleased(KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * @see KeyListener#keyTyped(KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * returns the maximum lenght of the text in the textfield
     */

    private int getMaxOffset() {
      switch (format) {
      case FORMAT_12H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          return 11;
        case PRECISION_MINUTES:
          return 8;
        case PRECISION_HOURS:
          return 5;
        default:
          break;
        }
        break;
      case FORMAT_24H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          return 8;
        case PRECISION_MINUTES:
          return 5;
        case PRECISION_HOURS:
          return 3;
        default:
          break;
        }
        break;
      default:
        break;
      }
      return 0;
    }

    /**
     * returns the format of the time according to the specified precisionformat
     * and timeformat
     */

    private String getDateFormat() {
      String dateFormat = null;
      switch (format) {
      case FORMAT_12H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          dateFormat = "hh:mm:ss a";
          break;
        case PRECISION_MINUTES:
          dateFormat = "hh:mm a";
          break;
        case PRECISION_HOURS:
          dateFormat = "hh a";
          break;
        default:
          break;
        }
        break;
      case FORMAT_24H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          dateFormat = "HH:mm:ss";
          break;
        case PRECISION_MINUTES:
          dateFormat = "HH:mm";
          break;
        case PRECISION_HOURS:
          dateFormat = "HH";
          break;
        default:
          break;
        }
        break;
      default:
        break;
      }
      return dateFormat;
    }

    /**
     * returns the jump the caret has to take when text is inserted
     */

    private int getJump(int offs) {
      int jump = 1;
      switch (format) {
      case FORMAT_12H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          if (offs == 1 || offs == 4 || offs == 7)
            jump = 2;
          else if (offs == 9) jump = 0;
          break;
        case PRECISION_MINUTES:
          if (offs == 1 || offs == 4)
            jump = 2;
          else if (offs == 6) jump = 0;
          break;
        case PRECISION_HOURS:
          if (offs == 1)
            jump = 2;
          else if (offs == 3) jump = 0;
          break;
        default:
          break;
        }
        break;
      case FORMAT_24H:
        switch (precisionType) {
        case PRECISION_SECONDS:
          if (offs == 1 || offs == 4)
            jump = 2;
          else if (offs == 7) jump = 0;
          break;
        case PRECISION_MINUTES:
          if (offs == 1)
            jump = 2;
          else if (offs == 3) jump = 0;
          break;
        case PRECISION_HOURS:
          if (offs == 1) jump = 0;
          break;
        default:
          break;
        }
        break;
      default:
        break;
      }
      return offs + jump;
    }

    /**
     * transform the time according to the specified precision
     */

    public Date transformDate(Date newDate) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(newDate);
      int datePart = -1;
      switch (precisionType) {
      case PRECISION_SECONDS:
        datePart = cal.get(Calendar.SECOND);
        break;
      case PRECISION_MINUTES:
        datePart = cal.get(Calendar.MINUTE);
        break;
      case PRECISION_HOURS:
        switch (format) {
        case FORMAT_12H:
          datePart = cal.get(Calendar.HOUR);
          break;
        case FORMAT_24H:
          datePart = cal.get(Calendar.HOUR_OF_DAY);
          break;
        default:
          break;
        }
      default:
        break;
      }

      if (datePart % precision != 0) {
        int newValue = 0;
        if (datePart % precision > precision / 2) {
          newValue = datePart + Math.abs(precision - datePart % precision);
        } else {
          newValue = Math.abs(datePart - datePart % precision);
        }
        switch (precisionType) {
        case PRECISION_SECONDS:
          cal.set(Calendar.SECOND, newValue);
          break;
        case PRECISION_MINUTES:
          cal.set(Calendar.MINUTE, newValue);
          break;
        case PRECISION_HOURS:
          switch (format) {
          case FORMAT_12H:
            cal.set(Calendar.HOUR, newValue);
            break;
          case FORMAT_24H:
            cal.set(Calendar.HOUR_OF_DAY, newValue);
            break;
          default:
            break;
          }
        default:
          break;
        }
      }
      return cal.getTime();
    }

    /**
     * the document that overrides the default Document of the JTextField
     */

    class TimeDocument extends PlainDocument {
      public TimeDocument() {
      }

      public TimeDocument(Content c) {
        super(c);
      }

      public void clear(int start, int len) {
        try {
          super.remove(start, len);
        } catch (Exception e) {
        }
      }

      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

        if (noCheck) {
          super.insertString(offs, str, a);
          setCaretPosition(currentCaretPosition);
          noCheck = false;
          return;
        }
        if (offs >= getMaxOffset()) return;

        StringBuffer newValueBuffer = new StringBuffer();
        newValueBuffer.append(formatter.format(date));
        newValueBuffer.replace(offs, offs + 1, str);

        try {
          Date tmpDate = formatter.parse(newValueBuffer.toString());

          super.remove(0, getMaxOffset());
          super.insertString(0, formatter.format(tmpDate), a);
          date = transformDate(tmpDate);
          try {

            setCaretPosition(getJump(offs));
          } catch (IllegalArgumentException ee) {
          }

        } catch (ParseException e) {
          return;
        }
      }

      public void remove(int offs, int len) throws BadLocationException {
      }
    }
  }

  /*
   * (non-Javadoc)
   * @seejavax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
   * DocumentEvent)
   */
  public void changedUpdate(DocumentEvent e) {
    String timeTxt = textField.getText();
    try {
      Date tmpDate = textField.formatter.parse(timeTxt);
      Date oldDate = textField.date != null ? new Date(textField.date.getTime()) : null;
      textField.date = textField.transformDate(tmpDate);
      _fireTimePropertyChange(oldDate, textField.date);
    } catch (ParseException ex) {
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent
   * )
   */
  public void insertUpdate(DocumentEvent e) {
    String timeTxt = textField.getText();
    try {
      Date tmpDate = textField.formatter.parse(timeTxt);
      Date oldDate = textField.date != null ? new Date(textField.date.getTime()) : null;
      textField.date = textField.transformDate(tmpDate);
      _fireTimePropertyChange(oldDate, textField.date);
    } catch (ParseException ex) {
      ex.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent
   * )
   */
  public void removeUpdate(DocumentEvent e) {
  }

}
