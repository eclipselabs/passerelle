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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * JSpinField is a numeric field with 2 spin buttons to increase or decrease the
 * value.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JSpinField extends JPanel implements CaretListener, AdjustmentListener, ActionListener {
  /**
   * Default JSpinField constructor.
   */
  public JSpinField() {
    super();
    min = 0;
    max = 100;
    value = 0;
    darkGreen = new Color(0, 150, 0);

    setLayout(new BorderLayout());
    textField = new JTextField(Integer.toString(value)) {
      public boolean isFocusTraversable() {
        return false;
      }
    };
    textField.addCaretListener(this);
    textField.addActionListener(this);
    add(textField, BorderLayout.CENTER);
    scrollBar = new JScrollBar(Adjustable.VERTICAL, 0, 0, 0, 100) {
      public boolean isFocusTraversable() {
        return false;
      }
    };
    scrollBar.setPreferredSize(new Dimension(scrollBar.getPreferredSize().width, textField.getPreferredSize().height));
    scrollBar.setMinimum(min);
    scrollBar.setMaximum(max);
    scrollBar.setValue(max + min - value);
    scrollBar.setVisibleAmount(0);

    scrollBar.addAdjustmentListener(this);
    add(scrollBar, BorderLayout.EAST);
  }

  protected void setValue(int newValue, boolean updateTextField, boolean updateScrollbar) {
    int oldValue = value;

    if (newValue < min)
      value = min;
    else if (newValue > max)
      value = max;
    else
      value = newValue;

    if (updateTextField) {
      textField.setText(Integer.toString(value));
      textField.setForeground(Color.black);
    }

    if (updateScrollbar) scrollBar.setValue(max + min - value);

    firePropertyChange("value", oldValue, value);
  }

  /**
   * Sets the value. This is a bound property.
   * 
   * @see #getValue
   * @param newValue the new value
   */
  public void setValue(int newValue) {
    setValue(newValue, true, true);
  }

  /**
   * Returns the value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets the minimum value.
   * 
   * @see #getMinimum
   * @param newMinimum the new minimum value
   */
  public void setMinimum(int newMinimum) {
    min = newMinimum;
    scrollBar.setMinimum(min);
  }

  /**
   * Returns the minimum value.
   */
  public int getMinimum() {
    return min;
  }

  /**
   * Sets the maximum value.
   * 
   * @see #getMaximum
   * @param newMaximum the new maximum value
   */
  public void setMaximum(int newMaximum) {
    max = newMaximum;
    scrollBar.setMaximum(max);
  }

  /**
   * Returns the maximum value.
   */
  public int getMaximum() {
    return max;
  }

  /**
   * Sets the font property.
   * 
   * @param font the new font
   */
  public void setFont(Font font) {
    if (textField != null) textField.setFont(font);
  }

  /**
   * Sets the foreground color.
   * 
   * @param fg the new foreground
   */
  public void setForeground(Color fg) {
    if (textField != null) textField.setForeground(fg);
  }

  /**
   * After any user input, the value of the textfield is proofed. Depending on
   * being an integer, the value is colored green or red.
   */
  public void caretUpdate(CaretEvent e) {
    try {
      int testValue = Integer.valueOf(textField.getText()).intValue();

      if ((testValue >= min) && (testValue <= max)) {
        // textField.setForeground(darkGreen);
        setValue(testValue, false, false);
      } else {
        textField.setForeground(Color.red);
      }
    } catch (Exception ex) {
      if (ex instanceof NumberFormatException) textField.setForeground(Color.red);

      // Ignore all other exceptions, e.g. illegal state exception
    }
    textField.repaint();
  }

  /**
   * The 2 buttons are implemented with a JScrollBar.
   */
  public void adjustmentValueChanged(AdjustmentEvent e) {
    setValue(max + min - e.getValue(), true, false);
  }

  /**
   * After any user input, the value of the textfield is proofed. Depending on
   * being an integer, the value is colored green or red. If the textfield is
   * green, the enter key is accepted and the new value is set.
   */
  public void actionPerformed(ActionEvent e) {
    if (textField.getForeground().equals(darkGreen)) setValue(Integer.valueOf(textField.getText()).intValue());
  }

  /**
   * Creates a JFrame with a JSpinField inside and can be used for testing.
   */
  static public void main(String[] s) {
    JFrame frame = new JFrame("JSpinField");
    frame.getContentPane().add(new JSpinField());
    frame.pack();
    frame.setVisible(true);
  }

  protected JTextField textField;
  protected JScrollBar scrollBar;
  private Color darkGreen;
  private int min;
  private int max;
  private int value;
}
