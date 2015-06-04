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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.binding.ParameterToPasserelleQueryBinder;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;

import ptolemy.data.expr.Parameter;

/**
 * 
 * @author wim
 */
public class BasePasserelleQuery implements IPasserelleQuery, IPasserelleComponentCloseListener {

	private static Logger logger = LoggerFactory.getLogger(BasePasserelleQuery.class);

	private HashMap<String, ParameterToWidgetBinder> parameterBinders = new HashMap<String, ParameterToWidgetBinder>();
	private IPasserelleComponent passerelleComponent;

	public BasePasserelleQuery(Actor actor, IPasserelleComponent passerelleComponent) {
		this.passerelleComponent = passerelleComponent;
		createBindings(actor);
		fillPasserelleComponent();
		passerelleComponent.addListener(this);
	}

	public BasePasserelleQuery(Map<String, Parameter> nameToParamMapping, IPasserelleComponent passerelleComponent) {
		this.passerelleComponent = passerelleComponent;
		createBindings(nameToParamMapping);
		fillPasserelleComponent();
		passerelleComponent.addListener(this);
	}

	protected void createBindings(Map<String, Parameter> nameToParamMapping) {
		for (Map.Entry<String, Parameter> entry : nameToParamMapping.entrySet()) {
			parameterBinders.put(entry.getKey(), new ParameterToPasserelleQueryBinder(entry.getValue(), entry.getKey(),
					this));
		}
	}

	protected void createBindings(Actor actor) {
		Parameter[] params = actor.getConfigurableParameters();
		for (Parameter parameter : params) {
			parameterBinders.put(parameter.getName(), new ParameterToPasserelleQueryBinder(parameter, this));
		}
	}

	public IPasserelleComponent getPasserelleComponent() {
		return passerelleComponent;
	}

	public Set<ParameterToWidgetBinder> getParameterBindings() {
		return new HashSet(parameterBinders.values());
	}
	
	
	public void fillParameters(){
		for (ParameterToWidgetBinder parameterToWidgetBinder : parameterBinders.values()) {
			parameterToWidgetBinder.fillParameterFromWidget();
		}
	}
	
	public void fillPasserelleComponent(){
		for (ParameterToWidgetBinder parameterToWidgetBinder : parameterBinders.values()) {
			parameterToWidgetBinder.fillWidgetFromParameter();
		}
	}
	
	public void onClose(String button) {
		fillParameters();
	}

	public String getStringValue(String name) throws NoSuchElementException, IllegalArgumentException {
		try {
			return BeanUtils.getProperty(passerelleComponent, name).toString();
		} catch (Exception e) {
			logger.error("error retrieving property " + name + " from " + passerelleComponent, e);
			throw new NoSuchElementException(e.getMessage());
		}
	}

	public void setStringValue(String name, String value) throws NoSuchElementException, IllegalArgumentException {
		try {
			BeanUtils.setProperty(passerelleComponent, name, value);
		} catch (Exception e) {
			logger.error("error setting value " + name + " on property " + name + " from " + passerelleComponent, e);
			throw new NoSuchElementException(e.getMessage());
		}
	}

	/**
	 * Returns false : no automatic sync of parameters
	 */
	public boolean hasAutoSync() {
		return false;
	}
	
}