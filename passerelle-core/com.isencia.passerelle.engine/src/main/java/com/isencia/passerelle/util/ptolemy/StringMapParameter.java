package com.isencia.passerelle.util.ptolemy;

import java.util.Collection;
import java.util.Map;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class StringMapParameter extends StringParameter {


	private IAvailableMap     choices;
	
	
	/**
	 * 
	 * @param container
	 * @param name
	 * @param choices
	 * @param type  - one of SWT.MULTI or SWT.SINGLE
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public StringMapParameter(final NamedObj      container, 
			                  final String        name,
			                  final IAvailableMap choices) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		this.choices = choices;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4876127320162014865L;

	public IAvailableMap getAvailableMap() {
		return choices;
	}

	public void setAvailableMap(IAvailableMap availableChoices) {
		this.choices = availableChoices;
	}

	public Map<String, String> getMap() {
		return choices.getMap();
	}
	public Map<String, String> getKeyMap() {
		return choices.getVisibleKeyChoices();
	}
	public Collection<String> getSelected() {
		return choices.getSelectedChoices();
	}
}
