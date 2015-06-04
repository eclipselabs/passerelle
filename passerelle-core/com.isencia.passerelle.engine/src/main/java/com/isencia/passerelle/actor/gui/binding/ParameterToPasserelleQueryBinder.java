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
package com.isencia.passerelle.actor.gui.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.gui.IPasserelleQuery;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Binder between a {@link IPasserelleQuery} and a {@link Parameter}.
 * <br>
 * Typically all the parameters of 1 actor will be bound to the same {@link IPasserelleQuery} instance.
 *   
 * @author wim
 */
public class ParameterToPasserelleQueryBinder extends AbstractParameterToWidgetBinder {
	private final static Logger logger = LoggerFactory.getLogger(ParameterToPasserelleQueryBinder.class);

	/**
	 * The name used in the {@link IPasserelleQuery} to map the parameter. This will in<br>
	 * most cases be the same as the parametername, but can be different if we want to reuse<br>
	 * existing {@link IPasserelleQuery} classes.
	 */
	private String name; 
	
	public ParameterToPasserelleQueryBinder(Parameter parameter,IPasserelleQuery passerelleQuery) {
		super(parameter,passerelleQuery);
		this.name = parameter.getName();
	}
	
	public ParameterToPasserelleQueryBinder(Parameter parameter,String name,IPasserelleQuery passerelleQuery) {
		super(parameter,passerelleQuery);
		this.name = name;
	}

	
	public void fillParameterFromWidget() {
    	Parameter parameter= getBoundParameter();
    	IPasserelleQuery passerelleQuery= (IPasserelleQuery)getBoundComponent();
    	String newValue = passerelleQuery.getStringValue(name);
    	parameter.setExpression(newValue);
    	try {
			parameter.validate();
		} catch (IllegalActionException e) {
			logger.error("Error validating updated parameter "+parameter.getFullName()+" with new value "+newValue,e);
		}
    }

    public void fillWidgetFromParameter() {
    	Parameter parameter= getBoundParameter();
    	IPasserelleQuery passerelleQuery= (IPasserelleQuery)getBoundComponent();
    	passerelleQuery.setStringValue(name, parameter.getExpression());
    }

    public String getName() {
		return name;
	}
    
}
