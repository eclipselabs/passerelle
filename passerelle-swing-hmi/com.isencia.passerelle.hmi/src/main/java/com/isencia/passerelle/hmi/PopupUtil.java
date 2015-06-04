package com.isencia.passerelle.hmi;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class PopupUtil {

  private static final long serialVersionUID = 1L;

  public static void showError(Component container, String resourceMessage) {
    showError(container, resourceMessage, null);
  }

  public static void showError(Component container, String resourceMessage, String additionalInfo) {
    showOptionDialog(container, HMIMessages.getString(resourceMessage) + (additionalInfo != null ? "\nInfo:" + additionalInfo : ""), HMIMessages
        .getString("error"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
  }

  public static void showInfo(Component container, String resourceMessage) {
    showInfo(container, resourceMessage, null);
  }

  public static void showInfo(Component container, String resourceMessage, String additionalInfo) {
    showOptionDialog(container, HMIMessages.getString(resourceMessage) + (additionalInfo != null ? "\nInfo:" + additionalInfo : ""), HMIMessages
        .getString("info"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
  }

  public static void showWarning(Component container, String resourceMessage) {
    showWarning(container, resourceMessage, null);
  }

  public static void showWarning(Component container, String resourceMessage, String additionalInfo) {
    showOptionDialog(container, HMIMessages.getString(resourceMessage) + (additionalInfo != null ? "\nInfo:" + additionalInfo : ""), HMIMessages
        .getString("warning"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
  }

  public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType, int width, int height) {
    return showOptionDialog(parentComponent, message, title, optionType, messageType, null, null, null, width, height);
  }

  public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options,
      Object initialValue) {
    int height = 120;
    int width = 300;
    if (message instanceof String) {
      // check nr of lines to calculate required height
      String msg = (String) message;
      int nrLines = 1;
      int maxLineLength = 20;
      String[] lines = msg.split("\n");
      nrLines = lines.length;
      for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        maxLineLength = (maxLineLength > line.length()) ? maxLineLength : line.length();
      }
      if (nrLines > 2) height = 50 * nrLines;
      if (maxLineLength > 30) width = maxLineLength * 8;
    }
    return showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue, width, height);
  }

  public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options,
      Object initialValue, int width, int height) {

    JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);

    pane.setInitialValue(initialValue);

    JDialog dialog = pane.createDialog(parentComponent, title);

    // dialog.setSize(width,height);

    pane.selectInitialValue();
    dialog.setLocationRelativeTo(parentComponent);
    dialog.setVisible(true);

    Object selectedValue = pane.getValue();

    if (selectedValue == null) return JOptionPane.CLOSED_OPTION;
    if (options == null) {
      if (selectedValue instanceof Integer) return ((Integer) selectedValue).intValue();
      return JOptionPane.CLOSED_OPTION;
    }
    for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
      if (options[counter].equals(selectedValue)) return counter;
    }
    return JOptionPane.CLOSED_OPTION;
  }

}
