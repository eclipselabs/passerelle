package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
import com.isencia.passerelle.workbench.util.DialogUtils;

public class StringChoicePropertyDescriptor extends PropertyDescriptor {
	
	private StringChoiceParameter param;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public StringChoicePropertyDescriptor(StringChoiceParameter param) {
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
				final ChoiceDialog dialog = new ChoiceDialog(parent.getShell());
				dialog.create();
				dialog.getShell().setSize(400,400);
				dialog.getShell().setText("Choose values for '"+getDisplayName()+"'");
				DialogUtils.centerDialog(parent.getShell(), dialog.getShell());
				final int ok = dialog.open();
				final String[] sel = ok==ChoiceDialog.OK ? dialog.getSelections() : param.getValue();
				final Object stringValue = getStringValue(sel);
				setValue(stringValue);
				return stringValue;
			}
			
		};
		if (getValidator() != null) editor.setValidator(getValidator());
		return editor;
	}

	protected Object getStringValue(String[] sel) {
		if (sel==null || sel.length<1 || allEmpty(sel)) return "";
		final StringBuilder buf = new StringBuilder();
		for (String string : sel) {
			buf.append(string);
			if (!string.equals(sel[sel.length-1]) && !"".equals(string)) {
				buf.append(", ");
			}
		}
		return buf.toString();
	}

	private boolean allEmpty(String[] sel) {
		for (String string : sel) {
			if (string==null)      continue;
			if ("".equals(string)) continue;
			return false;
		}
		return true;
	}

	private class ChoiceDialog extends Dialog {
		
		private List<Object> selection;
		
		ChoiceDialog(Shell shell) {
	        super(shell);
	        setShellStyle(SWT.MODELESS | SWT.SHELL_TRIM | SWT.BORDER);
	    }
		
		public String[] getSelections() {
			if (selection==null||selection.size()<1) return null;
			final String [] ret = new String[selection.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = selection.get(i)!=null  ? selection.get(i).toString() : "";
			}
			return ret;
		}

		protected Control createDialogArea(Composite parent) {
			
			final CheckboxTableViewer choiceTable = CheckboxTableViewer.newCheckList(parent, param.getChoiceType() | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			choiceTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			choiceTable.getTable().setLinesVisible(true);
			choiceTable.getTable().setHeaderVisible(false);
			
			choiceTable.setUseHashlookup(true);
			
			final Map<String,String> visMap = param.getVisibleChoices();
			if (visMap!=null) {
				choiceTable.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						return visMap.get(element);
					}
				});
			}
			choiceTable.setContentProvider(new IStructuredContentProvider() {

			
				public void dispose() {}
				
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

				
				public Object[] getElements(Object inputElement) {
					return param.getChoices();
				}
			});	
			choiceTable.setInput(new String());
			if (param.getValue()!=null) {
				choiceTable.setCheckedElements(param.getValue());
				if (selection==null) selection = new ArrayList<Object>(7);
				selection.addAll(Arrays.asList(param.getValue()));
			}
			
			choiceTable.addCheckStateListener(new ICheckStateListener() {
				
				boolean busy = false;
			
				public void checkStateChanged(CheckStateChangedEvent event) {
					
					if (busy) return;
					try {
						busy = true;
						if (selection==null) selection = new ArrayList<Object>(7);
						
				   		if (event!=null) {
			    			final Object element = event.getElement();
							if (!event.getChecked()) {
								selection.remove(element);
							} else {
								if (param.getChoiceType()==SWT.SINGLE) {
									selection.clear();
									choiceTable.setAllChecked(false);
									choiceTable.setChecked(event.getElement(), true);
								}
								if (!selection.contains(element)) {
									selection.add(element);
								}
							}
							
						} else {
							selection.clear();
						}
					} finally {
						busy = false;
					}

				}
			});
			
			return choiceTable.getControl();
		}				
	};

}
