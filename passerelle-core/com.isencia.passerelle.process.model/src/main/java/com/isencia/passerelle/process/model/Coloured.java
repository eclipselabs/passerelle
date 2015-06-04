/**
 * 
 */
package com.isencia.passerelle.process.model;

/**
 * A colour can be set on certain entities to indicate the result of an analysis or validation.
 * <p>
 * Several use-cases can be supported, like tresholding measurement results, indicating the approval of certain obtained results after a manual control etc.
 * So the model does not make any assumptions on the exact colour names or meanings that could be applied.
 * </p>
 * @author delerw
 *
 */
public interface Coloured {
	
	/**
	 * 
	 * @return the colour marker. Can be null.
	 */
	String getColour();
	
	/**
	 * 
	 * @param colour the colour marker. Can be null to "unset" a colour.
	 */
	void setColour(String colour);

}
