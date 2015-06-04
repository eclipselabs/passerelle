package com.isencia.passerelle.util.ptolemy;

import java.util.Map;

public interface IAvailableChoices {

	public String[] getChoices();
	
	/**
	 *
	 * @return  A map value->visible label, may be null.
	 */
	public Map<String,String> getVisibleChoices();

	
	public class Stub implements IAvailableChoices {

		public String[] getChoices() {
			return null;
		}

		public Map<String, String> getVisibleChoices() {
			return null;
		}
		
	}
}
