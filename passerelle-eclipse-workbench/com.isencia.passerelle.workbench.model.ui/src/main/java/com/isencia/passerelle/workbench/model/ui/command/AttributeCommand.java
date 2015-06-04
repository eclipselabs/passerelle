package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ColumnViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;

import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class AttributeCommand extends Command {

	private static Logger logger = LoggerFactory
			.getLogger(AttributeCommand.class);

	private final Attribute attribute;
	private final Object newValue;
	private final Object previousValue;
	private final ColumnViewer viewer;

	public AttributeCommand(final ColumnViewer viewer, Object element,
			Object newValue) throws IllegalActionException {

		super("Set value "
				+ (element instanceof Variable ? (((Variable) element)
						.getDisplayName()
						+ " to " + getVisibleValue(newValue)) : ""));
		this.viewer = viewer;
		this.attribute = (Attribute) element;
		this.newValue = newValue;
		if (attribute instanceof StringAttribute){
			this.previousValue = ((StringAttribute) attribute).getExpression();
		}else {
			Variable var = (Variable)attribute;
			this.previousValue = (!var.isStringMode()
					&& var.getToken() != null && var.getToken() instanceof BooleanToken) ? new Boolean(
							((BooleanToken) var.getToken()).booleanValue())
			: var.getExpression();
		}
	}

	private static Object getVisibleValue(Object newValue) {
		if (newValue instanceof String && newValue.toString().length() > 32) {
			return newValue.toString().substring(0, 32) + "...";
		}
		return newValue;
	}

	public void execute() {
		setValue(newValue);
	}

	public void undo() {
		setValue(previousValue);
	}

	private void setValue(final Object value) {

		try {
			attribute.requestChange(new ModelChangeRequest(this.getClass(),
					attribute, "attribute") {
				@Override
				protected void _execute() throws Exception {
					if (value != null) {
						try {
							if (attribute instanceof StringAttribute) {
								((StringAttribute) attribute)
								.setExpression((String) value);
							}
							if (attribute instanceof Variable) {
								Variable var = (Variable) attribute;
								if (value instanceof Boolean) {
									var.setToken(new BooleanToken(
											((Boolean) value).booleanValue()));
								} else if (value instanceof Number) {
									var.setExpression(((Number) value)
											.toString());
								} else if (value instanceof String) {
									var.setExpression((String) value);
								} else {
									logger
									.error("Unrecognised value sent to Variable "
											+ attribute.getName());
									EclipseUtils.logError(null,
											"Unrecognised value sent to Variable "
													+ attribute.getName(),
													IStatus.ERROR);
								}
							}
						} catch (Exception e) {
							EclipseUtils.logError(e,
									"Error changing value of Variable "
											+ attribute.getName(),
											IStatus.ERROR);

						}
					} else {
						if (attribute instanceof StringAttribute) {
							((StringAttribute) attribute)
							.setExpression(null);
						}
						if (attribute instanceof Variable) {
							if (((Variable)attribute).isStringMode()) {
								((Variable)attribute).setExpression(null);
							}
						}
					}
					attribute.propagateValue();
				}
			});

		} catch (Exception ne) {
			logger.error("Cannot set variable value " + value, ne);
			EclipseUtils.logError(ne, "Cannot set variable value " + value,
					IStatus.ERROR);
		} finally {
			if (viewer!=null && !viewer.getControl().isDisposed()) {
				viewer.cancelEditing();
				viewer.refresh(attribute);
			}
		}

	}

}
