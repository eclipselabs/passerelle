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
package com.isencia.passerelle.ext.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.ext.TypeConverterProvider;
import com.isencia.passerelle.message.type.TypeConverter;
import com.isencia.properties.IHierarchicProperty;
import com.isencia.properties.PropertiesLoadingException;
import com.isencia.properties.PropertyContainerFactory;


/**
 * The default implementation of Passerelle's provider for TypeConverters.
 * 
 * It obtains the list from a properties-file, in the conf folder of a plain Passerelle desktop installation.
 * 
 * @author delerw
 *
 */
public class DefaultTypeConverterProvider implements TypeConverterProvider {

	private List<TypeConverter> converters = new ArrayList<TypeConverter>();
	private final static Logger logger = LoggerFactory.getLogger(DefaultTypeConverterProvider.class);

	static {
		try {
			PropertyContainerFactory.instance().registerPropertySource("passerelle-type-convertors.xml");
		} catch (PropertiesLoadingException e) {
			logger.warn("Could not find passerelle-type-convertors.xml. Default type converter provider disabled.");
		}
	}
	
	
	public DefaultTypeConverterProvider() {
		loadConverters();
	}

	public List<TypeConverter> getTypeConverters() {
		return converters;
	}

	/**
	 * Reads a pre-defined configuration file,
	 * and instantiates all defined converters
	 * in the chain.
	 *
	 */
	private void loadConverters() {
		IHierarchicProperty converterProps = PropertyContainerFactory.instance().get("com.isencia.passerelle.message.type.properties");
		if(converterProps!=null) {
			String[] converterNames = converterProps.getPropertyValueList("type-converters.type-converter");
			for (final String converterName : converterNames) {
				if(logger.isDebugEnabled())
					logger.debug("loadConverters() - found converter "+converterName);
				try {
					Class converterClass = Class.forName(converterName);
					TypeConverter converter = (TypeConverter) converterClass.newInstance();
					converters.add(converter);
					logger.debug("loadConverters() - succesfully registered converter "+converterName);
				} catch (ClassNotFoundException e) {
					logger.error("loadConverters() - could not load converter class "+converterName);
				} catch (InstantiationException e) {
					logger.error("loadConverters() - could not create converter object "+converterName, e);
				} catch (IllegalAccessException e) {
					logger.error("loadConverters() - could not create converter object "+converterName, e);
				} catch (ClassCastException e) {
					logger.error("loadConverters() - incorrect converter type "+converterName, e);
				}
			}
		}
	}

	public String getName() {
		return "DEFAULT";
	}
}
