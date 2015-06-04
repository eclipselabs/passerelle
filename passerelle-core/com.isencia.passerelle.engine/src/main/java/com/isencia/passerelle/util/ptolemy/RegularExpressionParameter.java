package com.isencia.passerelle.util.ptolemy;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * Parameter with defines a regular expression.
 * @author gerring
 *
 */
public class RegularExpressionParameter extends StringParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8272173562095296642L;
	
	private boolean isJustWildCard = false;

	public RegularExpressionParameter(NamedObj container, String name, boolean justWildCard)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		this.isJustWildCard = justWildCard;
	}

	public boolean isJustWildCard() {
		return isJustWildCard;
	}

	public void setJustWildCard(boolean isJustWildCard) {
		this.isJustWildCard = isJustWildCard;
	}
    
	public boolean isRegularExpression() {
		return !isJustWildCard();
	}
}
