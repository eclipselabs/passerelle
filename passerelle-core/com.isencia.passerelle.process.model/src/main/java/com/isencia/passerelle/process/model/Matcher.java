/**
 * 
 */
package com.isencia.passerelle.process.model;

/**
 * Generic interface to define matching conditions, 
 * that can be used e.g. to filter elements in aggregates.
 * 
 * @see ResultBlock.getMatchingItems()
 * 
 * @author erwin
 *
 */
public interface Matcher<T> {
	
	boolean matches(T thing);

}
