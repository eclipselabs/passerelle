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

import javax.swing.text.JTextComponent;

import ptolemy.data.expr.Parameter;

/**
 * Simple binder between a JTextField and a StringParameter
 *  
 * @author erwin
 */
public class ParameterToJTextFieldBinder extends AbstractParameterToWidgetBinder {
	
	
	public ParameterToJTextFieldBinder(Parameter boundParameter,JTextComponent boundComponent) {
		super(boundParameter, boundComponent);
	}
	
    public void fillParameterFromWidget() {
        getBoundParameter().setExpression(((JTextComponent)getBoundComponent()).getText());
    }

    public void fillWidgetFromParameter() {
        ((JTextComponent)getBoundComponent()).setText(getBoundParameter().getExpression());
    }

}
