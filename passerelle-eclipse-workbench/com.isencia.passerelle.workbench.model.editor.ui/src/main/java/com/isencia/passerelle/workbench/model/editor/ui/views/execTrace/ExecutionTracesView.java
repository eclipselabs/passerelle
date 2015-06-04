/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.workbench.model.editor.ui.views.execTrace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;
import com.isencia.passerelle.workbench.model.ui.utils.StringUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * @author erwin
 */
public class ExecutionTracesView extends ViewPart {

  public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.ExecutionTracesView";

  private Table table;
  private TableViewer tableViewer;
  private SourceFilter filter;
  private Integer maxCount = 100;
  private boolean maxCountActive = true;

  /**
   * 
   */
  public ExecutionTracesView() {
    TimerTask task = new TimerTask() {
      public void run() {
        Display.getDefault().syncExec(new EntryAdder());
      }
    };

    Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(task, 5000, 2000);

    ExecutionTracerService.registerTracer(new TraceHandler());
  }

  @Override
  public void createPartControl(Composite parent) {
    parent.setLayout(new GridLayout(1, false));
    createFilters(parent);
    createTableViewer(parent);
    tableViewer.addFilter(filter);
    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    tableViewer.setSorter(new TimeSorter());
    tableViewer.refresh();
    getSite().setSelectionProvider(tableViewer);
    tableViewer.setInput(ENTRIES);
  }

  public void refresh() {
    tableViewer.refresh();
  }

  protected void createFilters(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(3, false));

    final Label searchLabel = new Label(container, SWT.NONE);
    searchLabel.setText("Search: ");
    final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH);
    searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    // New to support the search
    searchText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        filter.setSearchText(searchText.getText());
        tableViewer.refresh();
      }
    });
    filter = new SourceFilter();

    Composite maxCountContainer = new Composite(container, SWT.NONE);
    maxCountContainer.setLayout(new GridLayout(3, false));

    final Label maxCountLabel = new Label(maxCountContainer, SWT.NONE);
    maxCountLabel.setText("Max: ");

    final Button maxCountActiveButton = new Button(maxCountContainer, SWT.CHECK);
    maxCountActiveButton.setSelection(maxCountActive);
    maxCountActiveButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        maxCountActive = maxCountActiveButton.getSelection();
        adaptMaxCount(maxCountActive, maxCount);
      }
    });
    final Text maxCountText = new Text(maxCountContainer, SWT.BORDER);
    maxCountText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    maxCountText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        System.out.println("max count " + maxCountText.getText());
        String mcStr = maxCountText.getText();
        try {
          maxCount = Integer.parseInt(mcStr);
        } catch (Exception e) {
          if (!StringUtils.isEmpty(mcStr)) {
            maxCount = 100;
            maxCountText.setText(maxCount.toString());
          } else {
            maxCount = null;
          }
        }
        tableViewer.refresh();
      }
    });
  }

  protected void createTableViewer(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    // Create the composite
    Composite composite = new Composite(container, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

    // Add TableColumnLayout
    TableColumnLayout layout = new TableColumnLayout();
    composite.setLayout(layout);

    tableViewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    table = tableViewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn tblclmnFirst = tableViewerColumn.getColumn();
    layout.setColumnData(tblclmnFirst, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
    tblclmnFirst.setText("Timestamp");
    tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        TraceEntry te = (TraceEntry) element;
        return te.time;
      }
    });

    TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn tblclmnLast = tableViewerColumn_1.getColumn();
    // Specify width using weights
    layout.setColumnData(tblclmnLast, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
    tblclmnLast.setText("Source");
    tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        TraceEntry te = (TraceEntry) element;
        return te.source;
      }
    });

    TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
    TableColumn tblclmnTitle = tableViewerColumn_2.getColumn();
    // Specify width using weights
    layout.setColumnData(tblclmnTitle, new ColumnWeightData(4, ColumnWeightData.MINIMUM_WIDTH, true));
    tblclmnTitle.setText("Message");

    tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        TraceEntry te = (TraceEntry) element;
        return te.msg;
      }

      @Override
      public Color getBackground(Object element) {
        TraceEntry te = (TraceEntry) element;
        String cellText = te.msg;
        if (pattern.matcher(cellText).matches()) {
          return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        } else {
          return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
        }
      }
      // @Override
      // public void update(ViewerCell cell) {
      // TraceEntry te = (TraceEntry) cell.getElement();
      // String cellText = te.msg;
      // cell.setText(cellText);
      // if (pattern.matcher(cellText).matches()) {
      // cell.setBackground(color);
      // }
      // super.update(cell);
      // }
    });
  }

  final static Pattern pattern = Pattern.compile("(?i).*Error.*");

  @Override
  public void setFocus() {

  }

  protected void addTraceEntry(TraceEntry e) {
    ENTRIES.add(e);
    tableViewer.add(e);
    adaptMaxCount(maxCountActive, maxCount);
  }

  private void adaptMaxCount(boolean maxCountActive, Integer maxCount) {
    if (maxCountActive && maxCount != null && maxCount > 0 && ENTRIES.size() > maxCount) {
      int diff = ENTRIES.size() - maxCount;
      for (int i = 0; i < diff; ++i) {
        ENTRIES.remove(0);
      }
      tableViewer.refresh();
    }
  }

  private static class TimeSorter extends ViewerSorter {
    public int compare(Viewer viewer, Object e1, Object e2) {
      TraceEntry te1 = (TraceEntry) e1;
      TraceEntry te2 = (TraceEntry) e2;
      // most recent first
      return te2.time.compareTo(te1.time);
    }
  }

  static List<TraceEntry> ENTRIES = new ArrayList<TraceEntry>();
  static {
    ENTRIES.add(new TraceEntry("09AM", "pol", "waking up"));
    ENTRIES.add(new TraceEntry("10AM", "pol", "breakfast"));
    ENTRIES.add(new TraceEntry("11AM", "pingo", "making ERROR noise"));
  };

  static int ctr = 12;

  final class EntryAdder implements Runnable {
    final String[] names = new String[] { "tim", "sam", "oma", "rik" };
    final String[] msgs = new String[] { "basket", "zwemmen", "roepen Error" };

    public void run() {
      TraceEntry e = new TraceEntry((ctr++) + "AM", names[ctr % 4], msgs[ctr % 3]);
      addTraceEntry(e);
    }
  }

  final class TraceHandler implements ExecutionTracer {

    public void trace(Actor actor, String message) {
      SimpleDateFormat tsFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SS");
      String source = FlowUtils.getFullNameWithoutFlow((NamedObj) actor);
      TraceEntry e = new TraceEntry(tsFormat.format(new Date()), source, message);
      addTraceEntry(e);
    }

    public void trace(Director director, String message) {
      SimpleDateFormat tsFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SS");
      String source = FlowUtils.getFullNameWithoutFlow((NamedObj) director);
      TraceEntry e = new TraceEntry(tsFormat.format(new Date()), source, message);
      addTraceEntry(e);
    }

    public void trace(Actor source, String message, Level level) {
      trace(source, message);
    }

    public void trace(Director source, String message, Level level) {
      trace(source, message);
    }

  }
}
