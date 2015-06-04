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
 /*
 * Created on 4 avr. 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.isencia.passerelle.actor.gui.binding;

import ptolemy.data.expr.Parameter;

/**
 * 
 * AbstractParameterToWidgetBinder
 * 
 * @author erwin
 */
public abstract class AbstractParameterToWidgetBinder implements ParameterToWidgetBinder {

    private Parameter boundParameter;
    private Object boundComponent;
    
    public AbstractParameterToWidgetBinder() {
	}
    
    public AbstractParameterToWidgetBinder(Parameter boundParameter, Object boundComponent) {
		this.boundParameter = boundParameter;
		this.boundComponent = boundComponent;
	}
	public Object getBoundComponent() {
        return boundComponent;
    }
    public void setBoundComponent(Object boundComponent) {
        this.boundComponent = boundComponent;
    }
    public void setBoundParameter(Parameter boundParameter) {
        this.boundParameter = boundParameter;
    }
    public Parameter getBoundParameter() {
        return boundParameter;
    }
}
