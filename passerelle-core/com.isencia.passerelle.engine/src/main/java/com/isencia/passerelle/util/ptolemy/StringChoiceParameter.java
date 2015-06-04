package com.isencia.passerelle.util.ptolemy;

import java.util.List;
import java.util.Map;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class StringChoiceParameter extends StringParameter {


	private IAvailableChoices availableChoices;
	private int               choiceType;
	
	/**
	 *
	 * @param container
	 * @param name
	 * @param choices
	 * @param type  - one of SWT.MULTI or SWT.SINGLE
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public StringChoiceParameter(final NamedObj    container, 
								final String       name,
								final List<String> choices,
								final int          type) throws IllegalActionException, NameDuplicationException {
		this(container, name, new IAvailableChoices() {
			
				public String[] getChoices() {
				return choices.toArray(new String[choices.size()]);
			}

			public Map<String,String> getVisibleChoices() {
				return null;
			}
		}, type);
	}
	
	/**
	 * 
	 * @param container
	 * @param name
	 * @param choices
	 * @param type  - one of SWT.MULTI or SWT.SINGLE
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public StringChoiceParameter(final NamedObj          container, 
			                     final String            name,
			                     final IAvailableChoices choices,
			                     final int type) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		this.availableChoices = choices;
		this.choiceType       = type;
	}

	/**
	 * Interface used here so that can change choices as needed.
	 */
	public String[] getChoices() {
		return availableChoices.getChoices();
	}

	public Map<String,String> getVisibleChoices() {
		return availableChoices.getVisibleChoices();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 4876127320162014865L;

	public IAvailableChoices getAvailableChoices() {
		return availableChoices;
	}

	public void setAvailableChoices(IAvailableChoices availableChoices) {
		this.availableChoices = availableChoices;
	}

	public int getChoiceType() {
		return choiceType;
	}

	public void setChoiceType(int choiceType) {
		this.choiceType = choiceType;
	}

	public String[] getValue() {
		final String    expr = getExpression();
		if (expr == null || "".equals(expr)) return null;
		final String [] vals = expr.split(","); // , in string value not supported yet
		for (int i = 0; i < vals.length; i++) {
			vals[i] = vals[i].trim();
		}
		return vals;
	}

}
