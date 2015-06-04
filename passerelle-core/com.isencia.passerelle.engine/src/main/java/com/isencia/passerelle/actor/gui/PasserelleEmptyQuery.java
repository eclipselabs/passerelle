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

import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JLabel;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;




/**
 * Wrapper type for easy identification/maintenance 
 * of empty Passerelle Queries, i.e. without any parameter cfg fields.
 *
 * @author erwin
 */
public class PasserelleEmptyQuery extends JLabel implements IPasserelleQuery,IPasserelleComponent{
    public PasserelleEmptyQuery(String message) {
        super(message);
    }

	public String getStringValue(String name) throws NoSuchElementException, IllegalArgumentException {
		throw new  NoSuchElementException(name);
	}

	public void setStringValue(String name, String value) throws NoSuchElementException, IllegalArgumentException {
		throw new  NoSuchElementException(name);		
	}
	
	public void windowClosed(Window window, String s) {
	}

	public IPasserelleComponent getPasserelleComponent() {
		return this;
	}
	public void closed(String button) {
	}
	/**
	 * Returns true because there are no params 
	 */
	public boolean hasAutoSync() {
		return true;
	}
	
	public void addListener(IPasserelleComponentCloseListener closeListener) {
		
	}

	public Set<ParameterToWidgetBinder> getParameterBindings() {
		return Collections.EMPTY_SET;
	}
}
