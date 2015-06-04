package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.core.IEventLog;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class EventLogView extends ViewPart implements ISelectionListener {
  public final static String _DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.views.EventLogView"; //$NON-NLS-1$
  private IWorkbenchPart part;
  private TableViewer viewer;
  private static final Image INFO = Activator.getImageDescriptor("icons/info.gif").createImage();
  private static final Image ERROR = Activator.getImageDescriptor("icons/error.gif").createImage();
  private static final Image WARN = Activator.getImageDescriptor("icons/warn.gif").createImage();

  // We use icons

  public void createPartControl(Composite parent) {
    GridLayout layout = new GridLayout(3, false);
    parent.setLayout(layout);
    Label searchLabel = new Label(parent, SWT.NONE);
    searchLabel.setText("Search: ");
    final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
    searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    searchText.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateTableViewer(e.getSource());
      }
    });
    createViewer(parent);
  }

  private void createViewer(Composite parent) {
    viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    createColumns(parent, viewer);
    final Table table = viewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    viewer.setContentProvider(new ArrayContentProvider());
    // Get the content for the viewer, setInput will call getElements in the
    // contentProvider
    updateTableViewer();
    // Make the selection available to other views
    getSite().setSelectionProvider(viewer);
    // Set the sorter for the table

    // Layout the viewer
    GridData gridData = new GridData();
    gridData.verticalAlignment = GridData.FILL;
    gridData.horizontalSpan = 3;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalAlignment = GridData.FILL;
    viewer.getControl().setLayoutData(gridData);

    if (getSite() != null)
      getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
  }

  private void updateTableViewer(Object filter) {
    if (part instanceof PasserelleModelMultiPageEditor) {
      CompositeActor actor = ((PasserelleModelMultiPageEditor) part).getDiagram();
      if (actor != null) {
        RepositoryService repositoryService = Activator.getDefault().getRepositoryService();
        if (repositoryService != null) {

          viewer.setInput(repositoryService.getLogs(actor.toplevel().getName(), 100));
        }

      }
    }
  }

  private void updateTableViewer() {
    updateTableViewer(null);
  }

  public TableViewer getViewer() {
    return viewer;
  }

  // This will create the columns for the table
  private void createColumns(final Composite parent, final TableViewer viewer) {
    String[] titles = { "", "Message", "Id", "Creation" };
    int[] bounds = { 20, 200, 50, 100 };
    TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return null;
      }

      @Override
      public Image getImage(Object element) {
        IEventLog p = (IEventLog) element;
        if ("ERROR".equals(p.getStatus()) || "FATAL".equals(p.getStatus())) {
          return ERROR;
        } else if ("WARN".equals(p.getStatus())) {
          return WARN;
        } else {
          return INFO;
        }
      }
    });
    // First column is for the message
    col = createTableViewerColumn(titles[1], bounds[1], 1);
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        IEventLog p = (IEventLog) element;
        return p.getMessage();
      }
    });

    // Second column is for the last name
    col = createTableViewerColumn(titles[2], bounds[2], 2);
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        IEventLog p = (IEventLog) element;
        if (p.getSessionId() != null && p.getSessionId().contains(FlowUtils.FLOW_SEPARATOR)) {
          String parts[] = p.getSessionId().split(FlowUtils.FLOW_SEPARATOR);
          if (parts.length > 1) {
            return parts[1];
          } else {
            return parts[0];
          }
        }
        return p.getSessionId();
      }
    });
    // Third column is for creation ts
    col = createTableViewerColumn(titles[3], bounds[3], 3);
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        IEventLog p = (IEventLog) element;
        Date date = p.getDate();
        if (date == null) {
          return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(_DATEFORMAT);
        return formatter.format(date);
      }
    });

  }

  private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
    final TableColumn column = viewerColumn.getColumn();
    column.setText(title);
    column.setWidth(bound);
    column.setResizable(true);
    column.setMoveable(true);
    return viewerColumn;
  }

  /** * Passing the focus request to the viewer's control. */

  public void setFocus() {
    viewer.getControl().setFocus();
  }

  public void selectionChanged(IWorkbenchPart part, ISelection arg1) {
    if (part instanceof PasserelleModelMultiPageEditor) {
      this.part = part;
      updateTableViewer();
      // clear();
    }

  }

  public void clear() {

    this.part = null;

  }
}
