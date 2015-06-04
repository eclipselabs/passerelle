package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.isencia.passerelle.util.ptolemy.RegularExpressionParameter;

public class RegularExpressionDescriptor extends PropertyDescriptor {
	
	private RegularExpressionParameter param;

	@Override
	public String getDisplayName() {
		return super.getDisplayName();
	}

	/**
	 * @param id
	 * @param displayName
	 */
	public RegularExpressionDescriptor(RegularExpressionParameter param) {
		super(param.getName(), param.getDisplayName());
		this.param        = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPr
	 * opertyEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createPropertyEditor(Composite parent) {
		
		final TextCellEditor editor = new TextCellEditor(parent);
		editor.setValidator(new ICellEditorValidator() {
		
			public String isValid(Object value) {
				final Text control = (Text)editor.getControl();
				control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				if (value instanceof String) {
					final String regexOrWildCardPattern = (String)value;
					if (isLegalExpression(regexOrWildCardPattern)) {
							control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_BLUE));
					}  else {
						control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
					return null;
				}
				return "Invalid - must be string";
			}
		});
		return editor;
	}

	protected boolean isLegalExpression(String regexOrWildCardPattern) {
		if (regexOrWildCardPattern==null || "".equals(regexOrWildCardPattern)) return false;
		if (param.isRegularExpression()) {
			try {
				Pattern.compile(regexOrWildCardPattern);
				return true;
			} catch (PatternSyntaxException ne) {
				return false;
			}
		} else {
			return true;
		}
	};

}
