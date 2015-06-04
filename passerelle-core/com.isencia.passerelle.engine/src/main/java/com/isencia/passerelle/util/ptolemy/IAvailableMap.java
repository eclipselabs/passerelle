package com.isencia.passerelle.util.ptolemy;

import java.util.Collection;
import java.util.Map;

public interface IAvailableMap {
	
	public Map<String,String> getMap();
	
	/**
	 * If the keys are mapped, this will return something not null.
	 * @return
	 */
	public Map<String,String> getVisibleKeyChoices();
	
	/**
	 * If there are selections made which should be highlighted
	 * this method can return something non-null.
	 *
	 */
	public Collection<String> getSelectedChoices();

}
