/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.actor.gui;

import java.util.NoSuchElementException;
import java.util.Set;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;


/**
 * Interface for the Contoller class of the MVC implementation of parameter editing in Passerelle
 * 
 * @author wim
 * 
 */
public interface IPasserelleQuery {
		
	/**
	 * Get the value for a parameter with the specified name
	 * 
	 * @param name
	 * @return
	 * @throws NoSuchElementException
	 * @throws IllegalArgumentException
	 */
	String getStringValue(String name) throws NoSuchElementException, IllegalArgumentException;

	
	/**
	 * Set the value for a parameter with the specified name
	 * 
	 * @param name
	 * @param value
	 * @throws NoSuchElementException
	 * @throws IllegalArgumentException
	 */
	void setStringValue(String name,String value)throws NoSuchElementException, IllegalArgumentException;
	
	/**
	 * Return the View component
	 * @return
	 */
	IPasserelleComponent getPasserelleComponent();
	
	
	/**
	 * Returns the mappings to the parameters
	 * @return
	 */
	Set<ParameterToWidgetBinder> getParameterBindings();
	
	/**
	 * Indicates if the Query has a system to keep the Parameter of the Actor and the values in the
	 * <br>
	 * {@link IPasserelleComponent} in sync automatically  
	 * @return
	 */
	boolean hasAutoSync();

}
