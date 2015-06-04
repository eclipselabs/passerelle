package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.util.ptolemy.StringMapParameter;
import com.isencia.passerelle.workbench.util.DialogUtils;
import com.isencia.passerelle.workbench.util.MapUtils;

public class StringMapPropertyDescriptor extends PropertyDescriptor {
	

	private StringMapParameter param;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public StringMapPropertyDescriptor(StringMapParameter param) {
		super(param.getName(), param.getDisplayName());
        this.param        = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(final Composite parent) {
		// Create dialog for choosing
		CellEditor editor = new DialogCellEditor(parent) {

			@Override
			protected Object openDialogBox(Control container) {
				
				final String             value       = param.getExpression();
				final Map<String,String> originalMap = param.getMap();
				
				final MapDialog dialog = new MapDialog(parent.getShell(), originalMap);
				dialog.create();
				dialog.getShell().setSize(700,400);
				dialog.getShell().setText("Map values for '"+getDisplayName()+"'");
				DialogUtils.centerDialog(parent.getShell(), dialog.getShell());
				final int ok = dialog.open();
				
				final Map<String,String> sel = ok==MapDialog.OK ? dialog.getMap()
						                                        : value==null||"".equals(value) ? new HashMap<String,String>() : originalMap;
				
				final Object stringValue = ok==MapDialog.CLEAR ? ""
						                                       : MapUtils.getString(sel);
				setValue(stringValue);
				return stringValue;
			}
			
		};
		if (getValidator() != null) editor.setValidator(getValidator());
		return editor;
	}

	private class MapDialog extends Dialog {
		
		public static final int CLEAR = 108;
		
		private Map<String,String> map;
		
		MapDialog(Shell shell, final Map<String,String> originalMap) {
	        super(shell);
	        setShellStyle(SWT.MODELESS | SWT.SHELL_TRIM | SWT.BORDER);
	        map = new LinkedHashMap<String,String>(originalMap!=null?originalMap.size():3);
	        if (originalMap!=null) map.putAll(originalMap);
	    }

		protected Control createDialogArea(Composite parent) {
			
			final TableViewer mapTable = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			mapTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			mapTable.getTable().setLinesVisible(true);
			mapTable.getTable().setHeaderVisible(true);
			mapTable.setUseHashlookup(true);
			
			final Font     orig = mapTable.getTable().getFont();
			final FontData[] fd = orig.getFontData();
			final FontData   fu = new FontData(fd[0].getName(), fd[0].getHeight(), SWT.BOLD);
			final Font bold = new Font(orig.getDevice(), fu);
			
			final TableViewerColumn key = new TableViewerColumn(mapTable, SWT.LEFT, 0);
			key.setLabelProvider(new BoldColumnLabelProvider(bold) {
				public String getText(Object element) {
					final String key = ((Map.Entry<String,String>)element).getKey();
					if (param.getKeyMap()!=null) {
						return param.getKeyMap().get(key);
					}
					return key;
				}

			});
			key.getColumn().setText("Data Name");
			key.getColumn().setWidth(300);
			
			final TableViewerColumn value = new TableViewerColumn(mapTable, SWT.LEFT, 1);
			value.setLabelProvider(new BoldColumnLabelProvider(bold) {
				public String getText(Object element) {
					return ((Map.Entry<String,String>)element).getValue();
				}
			});
			value.getColumn().setText("Variable Name");
			value.getColumn().setWidth(300);

			
			mapTable.setColumnProperties(new String[]{"key","value"});
			mapTable.setCellEditors(new CellEditor[]{null, new TextCellEditor(mapTable.getTable())});
			mapTable.setCellModifier(new DoubleClickModifier(mapTable) {
				
				public Object getValue(Object element, String property) {
					if ("key".equals(property)) return ((Map.Entry<String,String>)element).getKey();
					return ((Map.Entry<String,String>)element).getValue();
				}

				
				public void modify(Object item, String property, Object value) {
					if (!"value".equals(property)) return;
					final Object element  = ((IStructuredSelection)mapTable.getSelection()).getFirstElement();
					final String key      = (String)getValue(element, "key");
					if (value!=null) {
						map.put(key, value.toString());
					} else {
						map.remove(key);
					}
					mapTable.cancelEditing();
					mapTable.refresh();
				}
			});

			mapTable.setContentProvider(new IStructuredContentProvider() {

				
				public void dispose() {}
				
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

				
				public Object[] getElements(Object inputElement) {
					return map.entrySet().toArray(new Map.Entry[map.size()]);
				}
			});
		
			mapTable.setInput(new String());
			
			return mapTable.getControl();
		}

		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			super.createButtonsForButtonBar(parent);
			
			final Button button = createButton(parent, CLEAR, "Clear", true);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setReturnCode(CLEAR);
					close();
				}			
			});
		}

		
		protected Map<String, String> getMap() {
			return map;
		}				
	};
	
	public class BoldColumnLabelProvider extends ColumnLabelProvider {
		
		private Font bold;

		public BoldColumnLabelProvider(Font bold) {
			this.bold = bold;
		}

		public Font getFont(Object element) {
			if (param.getSelected()!=null) {
				final String key = ((Map.Entry<String,String>)element).getKey();
				if (param.getSelected().contains(key)) {
					return bold;
				}
			}
			return null;
		}

		public void dispose() {
			super.dispose();
			bold.dispose();
		}
	}

}
