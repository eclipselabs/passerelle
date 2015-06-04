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

import ptolemy.data.expr.Parameter;

/**
 * Contract for a utility to configure parameter options.
 * Using this mechanism we can reconfigure parameter options
 * with the Passerelle config files, i.o. needing to modify the actor code.
 * 
 * The associated object allows to define any kind of custom behaviour/strategy
 * linked to a selected option.
 *
 * @author erwin
 */
public interface IOptionsFactory {
    /**
     * Option objects maintain an association between an option label
     * and an associated object that can be used for any purpose
     * when the option is selected as parameter value.
     */
    public static class Option {
        private String label;
        private Object associatedObject;
        
        protected Option(String label, Object object) {
            this.label = label;
            associatedObject = object;
        }

        public Object getAssociatedObject() {
            return associatedObject;
        }

        public String getLabel() {
            return label;
        }

        public String toString() {
            return getLabel();
        }
    }

    /**
     * Overwrites current options settings for the given parameter
     * with the ones as configured in this factory.
     * 
     * This method is called by Actor.configureParameters().
     * 
     * @param p
     */
    public void setOptionsForParameter(Parameter p);
    
    public Option getOption(Parameter p, String label);
    public Option getDefaultOption(Parameter p);
}
