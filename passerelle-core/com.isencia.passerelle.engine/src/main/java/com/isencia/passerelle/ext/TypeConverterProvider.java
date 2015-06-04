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
package com.isencia.passerelle.ext;

import java.util.List;
import com.isencia.passerelle.message.type.TypeConverter;


/**
 * Contract for OSGi services (or other impls) that define the 
 * ordered list of TypeConverters to be used by a Passerelle engine impl.
 * 
 * Remark that the intention is that the returned list of TypeConverters can be modified
 * at runtime. I.e. someone can add to/remove from/reorder it and from then onwards Passerelle
 * will be using the modified list!
 * 
 * Access to the currently active TypeConverterProvider implementation depends on the environment.
 * It is always possible via <code>TypeConversionChain.getConverterProvider()</code>.
 * Within an OSGi environment, the implementation will need to be available as an OSGi service,
 * so a service tracker can be used to access it.
 * 
 * @author delerw
 *
 */
public interface TypeConverterProvider {
	
	/**
	 * 
	 * @return some descriptive name of this provider, typically used for logging purposes
	 */
	String getName();

	/**
	 * 
	 * @return the ordered list of type converters to use for message passing
	 * between different actor types, in the current Passerelle engine.
	 */
	List<TypeConverter> getTypeConverters();
}
