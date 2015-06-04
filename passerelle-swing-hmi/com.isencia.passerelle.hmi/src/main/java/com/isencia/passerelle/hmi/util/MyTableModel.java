package com.isencia.passerelle.hmi.util;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {
  public MyTableModel(String[] string, int rowCount) {
    super(string, rowCount);
  }

  public Class getColumnClass(int c) {
    try {
      return getValueAt(0, c).getClass();
    } catch (Throwable t) {
      // somehow the above code always gives ArrayIndexOutOfBounds during
      // initial screen construction/
      // something tries to get the class of col 0 of row 0 even before a row is
      // present...
      // then we just return Object!
      return Object.class;
    }
  }

  public boolean isCellEditable(int row, int col) {
    // Note that the data/cell address is constant,
    // no matter where the cell appears onscreen.

    if (col < 1) {
      return false;
    } else {
      return true;
    }

  }
}