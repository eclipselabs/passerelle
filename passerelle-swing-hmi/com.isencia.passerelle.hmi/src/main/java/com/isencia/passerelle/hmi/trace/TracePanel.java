/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.SearchPredicate;
import org.jdesktop.swingx.search.SearchFactory;

import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.util.MyTableModel;

/**
 * Simple implementation...
 * 
 * @author erwin.de.ley@isencia.be
 */
public class TracePanel extends JPanel {

  private final static SimpleDateFormat tsFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SS");
  private MyTableModel myTraceTableModel;
  private JXTable traceTable;

  private int maxNrLines = 100;
  private JTextField nbLineTextField;
  private JTextField filterDateTextField;
  private JTextField filterSourceTextField;
  private JTextField filterMessageTextField;

  // private Filter dateFilter = new PatternFilter();
  // private Filter sourceFilter = new PatternFilter();
  // private Filter messageFilter = new PatternFilter();
  private RowSorter<? extends TableModel> defaultRowSorter = null;
  private ColorHighlighter dateHighlight;
  private ColorHighlighter sourceHighlight;
  private ColorHighlighter messageHighlight;
  private JPanel commandPanel;
  private JButton clearButton;
  private JButton printButton;
  private JButton findButton;
  private JRadioButton filterButton;
  private JRadioButton highlightButton;
  private boolean isFiltering = true;

  /**
	 *
	 */
  public TracePanel() {
    super(new BorderLayout());

    final JScrollPane jScrollPane = new JScrollPane(getTraceTable());
    // jScrollPane = new JScrollPane();
    // jScrollPane.setViewportView(getTraceTable());
    add("Center", jScrollPane);

    add("South", getCommandPanel());

    // add a default highligther for errors
    final ColorHighlighter errorHighlight = new ColorHighlighter(HighlightPredicate.ALWAYS, new Color(1f, 0.2f, 0.2f, 1f), null);
    final HighlightPredicate error = new SearchPredicate(Pattern.compile("Error|error|ERROR|- desc:|- origin:|- reason:|- severity:"));
    errorHighlight.setHighlightPredicate(error);
    final ColorHighlighter warningHighlight = new ColorHighlighter(HighlightPredicate.ALWAYS, new Color(1f,0.8f, 0f, 1f), null);
    final HighlightPredicate warning = new SearchPredicate(Pattern.compile("Warning|warning|WARNING"));
    warningHighlight.setHighlightPredicate(warning);
    getTraceTable().addHighlighter(errorHighlight);
    getTraceTable().addHighlighter(warningHighlight);

    setSize(400, 200);
  }

  private JPanel getCommandPanel() {
    if (commandPanel == null) {
      // FlowLayout layout = new FlowLayout();
      final VerticalLayout layout = new VerticalLayout();
      // layout.setHgap(3);
      // layout.setVgap(3);
      commandPanel = new JPanel(layout);
      final FlowLayout layout2 = new FlowLayout();
      layout2.setHgap(10);
      final JPanel leftPanel = new JPanel(layout2);

      leftPanel.add(getPrintButton());
      leftPanel.add(getFindButton());
      leftPanel.add(getClearButton());
      leftPanel.add(new JLabel("Nb Lines"));
      leftPanel.add(getNbLinesTextField());

      final JPanel rightPanel = new JPanel(new FlowLayout());
      filterDateTextField = createFilterTextField();
      filterSourceTextField = createFilterTextField();
      filterMessageTextField = createFilterTextField();

      rightPanel.add(new JLabel("Date Filter"));
      rightPanel.add(filterDateTextField);
      rightPanel.add(new JLabel("Source Filter"));
      rightPanel.add(filterSourceTextField);
      rightPanel.add(new JLabel("Message Filter"));
      rightPanel.add(filterMessageTextField);

      final JRadioButton f = getFilterButton();
      final JRadioButton h = getHighlightButton();
      final ButtonGroup group = new ButtonGroup();
      group.add(f);
      group.add(h);

      rightPanel.add(f);
      rightPanel.add(h);

      commandPanel.add(leftPanel);
      commandPanel.add(rightPanel);
    }
    return commandPanel;
  }

  private JTextField createFilterTextField() {
		final JTextField result = new JTextField(16);
    result.setAction(createFilterAction());
    return result;
  }

  private void executeFilterAction() {
    if (dateHighlight != null) {
      getTraceTable().removeHighlighter(dateHighlight);
    }
    if (sourceHighlight != null) {
      getTraceTable().removeHighlighter(sourceHighlight);
    }
    if (messageHighlight != null) {
      getTraceTable().removeHighlighter(messageHighlight);
    }
    if (isFiltering) {
      Collection filterList = new ArrayList(3);

      filterList.add(RowFilter.regexFilter(filterSourceTextField.getText(), 1, 1));
      filterList.add(RowFilter.regexFilter(filterDateTextField.getText(), 1, 0));
      filterList.add(RowFilter.regexFilter(filterMessageTextField.getText(), 1, 2));

      RowFilter filter = RowFilter.andFilter(filterList);
      // dateFilter = new PatternFilter(filterDateTextField.getText(), 1, 0);
      // messageFilter = new PatternFilter(filterMessageTextField.getText(), 1,
      // 2);
      // final Filter[] filters = new Filter[] { dateFilter, sourceFilter,
      // messageFilter };
      // final FilterPipeline pipeline = new FilterPipeline(filters);
      TableRowSorter<MyTableModel> tableRowSorter = new TableRowSorter<MyTableModel>(getMyTraceTableModel());
      tableRowSorter.setRowFilter(filter);
      getTraceTable().setRowSorter(tableRowSorter);

    } else {
      // getTraceTable().setFilters(null);
      getTraceTable().setRowSorter(defaultRowSorter);

      if (!filterDateTextField.getText().trim().equals("")) {
        dateHighlight = new ColorHighlighter(HighlightPredicate.NEVER, new Color(1f, 0.2f, 0.2f, 1f), null);
        final HighlightPredicate pDate = new SearchPredicate(Pattern.compile(filterDateTextField.getText()), -1, 0);
        dateHighlight.setHighlightPredicate(pDate);
        getTraceTable().addHighlighter(dateHighlight);
      }
      if (!filterSourceTextField.getText().trim().equals("")) {
        sourceHighlight = new ColorHighlighter(HighlightPredicate.NEVER, new Color(1f, 0.2f, 0.2f, 1f), null);
        final HighlightPredicate pSource = new SearchPredicate(Pattern.compile(filterSourceTextField.getText()), -1, 1);
        sourceHighlight.setHighlightPredicate(pSource);
        getTraceTable().addHighlighter(sourceHighlight);
      }

      if (!filterMessageTextField.getText().trim().equals("")) {
        messageHighlight = new ColorHighlighter(HighlightPredicate.NEVER, new Color(1f, 0.2f, 0.2f, 1f), null);
        final HighlightPredicate pMessage = new SearchPredicate(Pattern.compile(filterMessageTextField.getText()), -1, 2);
        messageHighlight.setHighlightPredicate(pMessage);
        getTraceTable().addHighlighter(messageHighlight);
      }
    }
  }

  private Action createFilterAction() {
    final Action result = new AbstractAction() {
      public void actionPerformed(final ActionEvent e) {
        executeFilterAction();
      }
    };
    return result;
  }

  private JTextField getNbLinesTextField() {
    if (nbLineTextField == null) {
      nbLineTextField = new JTextField(new FixedLengthDocument(6), Integer.toString(maxNrLines), 6);
      nbLineTextField.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          maxNrLines = Integer.parseInt(nbLineTextField.getText());
          if (maxNrLines < 10) {
            maxNrLines = 10;
            nbLineTextField.setText(Integer.toString(maxNrLines));
          }
          while (myTraceTableModel.getRowCount() >= maxNrLines) {
            myTraceTableModel.removeRow(0);
          }
        }
      });
    }
    return nbLineTextField;
  }

  private JButton getClearButton() {
    if (clearButton == null) {
      final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/isencia/passerelle/hmi/resources/eraser.gif")));
      clearButton = new JButton(icon);
      clearButton.setToolTipText("Clear Log");
      clearButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          // TODO Auto-generated method stub
          reset();
        }
      });
    }
    return clearButton;
  }

  private JRadioButton getFilterButton() {
    if (filterButton == null) {
      filterButton = new JRadioButton("Filter");
      filterButton.setSelected(isFiltering);
      filterButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          isFiltering = true;
          executeFilterAction();
        }
      });
    }
    return filterButton;
  }

  private JRadioButton getHighlightButton() {
    if (highlightButton == null) {
      highlightButton = new JRadioButton("Highlight");
      highlightButton.setSelected(!isFiltering);
      highlightButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          isFiltering = false;
          executeFilterAction();
        }
      });

    }
    return highlightButton;
  }

  private JButton getPrintButton() {
    final JXTable logPanel = traceTable;
    if (printButton == null) {
      // printButton = new JButton("Print Log");
      final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/isencia/passerelle/hmi/resources/print_edit.gif")));
      printButton = new JButton(icon);
      printButton.setToolTipText("Print Log");
      // printButton.setIcon(new Icon)
      printButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          // PrintUtilities.printComponent(logPanel);
          try {
            logPanel.print();
          } catch (final PrinterException e1) {
            PopupUtil.showError(new TextArea(), "impossible to print", e1.getMessage());
            e1.printStackTrace();
          }
        }
      });

    }
    return printButton;
  }

  private JButton getFindButton() {
    final JXTable logPanel = traceTable;
    if (findButton == null) {
      final ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/isencia/passerelle/hmi/resources/search.gif")));
      findButton = new JButton(icon);
      findButton.setToolTipText("Search Log");
      findButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          SearchFactory.getInstance().showFindInput(logPanel, logPanel.getSearchable());
        }
      });

    }
    return findButton;
  }

  private JXTable getTraceTable() {
    if (traceTable == null) {
      traceTable = new JXTable(getMyTraceTableModel()) {
        @Override
        public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int vColIndex) {
          final Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
          if (c instanceof JComponent) {
            final JComponent jc = (JComponent) c;
            jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));
          }
          return c;
        }
      };

      traceTable.getTableHeader().setReorderingAllowed(true);
      traceTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

      // resize columns
      final TableColumn dateColumn = traceTable.getColumnModel().getColumn(0);
      dateColumn.setPreferredWidth(10);
      final TableColumn sourceColumn = traceTable.getColumnModel().getColumn(1);
      sourceColumn.setPreferredWidth(10);
      traceTable.setColumnControlVisible(true);
      traceTable.setShowHorizontalLines(false);
      traceTable.setRolloverEnabled(true);
      traceTable.getModel().addTableModelListener(new TableModelListener() {
        public void tableChanged(final TableModelEvent e) {
          if (e.getType() == TableModelEvent.INSERT) {
            // On execute le code plus tard :
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                traceTable.scrollRectToVisible(traceTable.getCellRect(e.getLastRow(), 0, false));
              }
            });

          }
        }
      });

      traceTable.addHighlighter(HighlighterFactory.createSimpleStriping());
      traceTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, Color.GRAY, Color.WHITE));

      traceTable.setPreferredScrollableViewportSize(new Dimension(400, 200));
      defaultRowSorter = traceTable.getRowSorter();
    }
    return traceTable;
  }

  private MyTableModel getMyTraceTableModel() {
    if (myTraceTableModel == null) {
      // Initialize table components
      final String[] columnNames = { "Date", "Source", "Message" };

      myTraceTableModel = new MyTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(final int row, final int col) {
          return false;
        }
      };
    }
    return myTraceTableModel;
  }

  public void addTraceMessage(final String source, final String message) {
    while (myTraceTableModel.getRowCount() >= maxNrLines) {
      myTraceTableModel.removeRow(0);
    }

    final Object[] currentTraceObject = { tsFormat.format(new Date()), source, message };
    myTraceTableModel.addRow(currentTraceObject);
  }

  public void reset() {
    while (myTraceTableModel.getRowCount() > 0) {
      myTraceTableModel.removeRow(0);
    }
  }

	/**
	 * 
	 * get the value contains in a cell
	 * 
	 * @param row
	 *            the row number of the cell
	 * @param column
	 *            the column number of the cell
	 * @return the value contains at the cell(row,column)
	 * @throws ArrayOfBoundsException
	 *             raise an ArrayOfBoundsException if row or column does not exist.
	 */

	public String getValueAt(final int row, final int column) {
		return (String) myTraceTableModel.getValueAt(row, column);
	}

	/**
	 * the row number in the table
	 * 
	 * @return the row number in the table
	 */

	public int getRowNumber() {
		return myTraceTableModel.getRowCount();
	}

  private static class FixedLengthDocument extends PlainDocument {
    private final int maxLength;

    public FixedLengthDocument(final int maxLength) {
      this.maxLength = maxLength;
    }

    @Override
    public void insertString(final int offset, final String str, final AttributeSet a) throws BadLocationException {
      if (str != null && str.length() + getLength() <= maxLength) {
        try {
          Integer.parseInt(str);
          super.insertString(offset, str, a);
        } catch (final NumberFormatException e) {

        }
      }
    }
  }

}
