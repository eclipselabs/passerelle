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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.isencia.passerelle.actor.Actor;


import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A helper class to configure parameter options.
 * Using this mechanism we can reconfigure parameter options
 * with the Passerelle config files, i.o. needing to modify the actor code.
 * 
 * The associated object allows to define any kind of custom behaviour/strategy
 * linked to a selected option.
 * 
 * Implementation classes must implement initializeOptions(), and this method
 * must be invoked either by the constructor of the implemented factory, or by
 * the actor using it. It is NOT invoked automatically in this class's constructor
 * to avoid errors in the initialization sequence.
 * 
 * @todo implement some mechanism to support options for multiple parameters in an actor
 * 
 * @author erwin
 */
public abstract class OptionsFactory extends Attribute implements IOptionsFactory {
    
    // Map of maps
    // 1st level key is the parameter name
    // 2nd level name is the option name/label
    private Map options = new HashMap();
    
    // Map of defaultOptions
    // key is the parameter name
    private Map defaultOptions = new HashMap();

    /**
     * @param actor
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public OptionsFactory(Actor actor, String name) throws IllegalActionException, NameDuplicationException {
        super(actor, name);
    }
    
    protected Option addOption(String paramName, String label, Object associatedObject) {
        if(paramName==null)
            return null;
        
        Option o = new Option(label,associatedObject);
        getOptionsForParameter(paramName, true).put(label,o);
        
        return o;
    }
    
    protected Option removeOption(String paramName, String label) {
        if(paramName==null || getOptionsForParameter(paramName, false)==null)
            return null;
        
        Option o = (Option) getOptionsForParameter(paramName, false).remove(label);
        
        return o;
    }
    
    protected void setDefaultOption(String paramName, Option defaultOption) throws IllegalArgumentException {
        if(paramName==null || defaultOption==null || 
                 getOptionsForParameter(paramName, false)==null || 
                !getOptionsForParameter(paramName, false).containsKey(defaultOption.getLabel())) {
            throw new IllegalArgumentException("invalid default option "+defaultOption+" for parameter "+paramName);
        } else {
            defaultOptions.put(paramName, defaultOption);
        }
    }
    
    /**
     * Returns the default option.
     * If no default has been set, returns an arbitrary entry from the options collection.
     * If the collection is empty, returns null.
     * 
     * @return
     */
    public Option getDefaultOption(Parameter p) {
        if(p==null)
            return null;
        
        Option dO = (Option) defaultOptions.get(p.getName());
        if(dO==null && getOptionsForParameter(p.getName(), false)!=null) {
            Iterator itr = getOptionsForParameter(p.getName(), false).values().iterator();
            if(itr.hasNext()) {
                dO = (Option) itr.next();
                defaultOptions.put(p.getName(),dO);
            }
        }
        return dO;
    }

    public Option getOption(Parameter p, String label) {
        if(p==null || getOptionsForParameter(p.getName(), false)==null)
            return null;
        
        return (Option) getOptionsForParameter(p.getName(), false).get(label);
    }
    
    /**
     * Overwrites current options settings for the given parameter
     * with the ones as configured in this factory.
     * 
     * @param p
     */
    public void setOptionsForParameter(Parameter p) {
        if(p==null || getOptionsForParameter(p.getName(), false)==null)
            return;
        
        p.removeAllChoices();
        
        Iterator optionsItr = getOptionsForParameter(p.getName(), false).values().iterator();
        while (optionsItr.hasNext()) {
            Option option = (Option) optionsItr.next();
            p.addChoice(option.getLabel());
        }
        
        if(p.getExpression()==null || p.getExpression().length()==0) {
        	// no value set yet, so set default
	        Option dO = getDefaultOption(p);
	        if(dO!=null)
	            p.setExpression(dO.getLabel());
        }
    }
    
    private Map getOptionsForParameter(String parameterName, boolean createIfNotFound) {
        if(parameterName==null)
            return null;
        
        Map o = (Map)options.get(parameterName);
        if(o==null && createIfNotFound) {
            o = new HashMap();
            options.put(parameterName, o);
        }
        return o;
    }
    
    /**
     * This is where the implementation classes must register all
     * allowed options, and after that preferably also set the defaultOption.
     */
    protected abstract void initializeOptions();
    
}
