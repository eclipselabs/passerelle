/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.process.model;

import java.util.Iterator;
import java.util.Set;


/**
 * Interface for all things that can have a collection of <code>Attribute</code>s.
 * <p>
 * An <code>AttributeHolder</code> by assumption stores the attributes by their name,
 * that should be unique within the holder. I.e. it can only contain one attribute for a given name.
 * </p>
 * @author erwin
 *
 */
public interface AttributeHolder {
  
	/**
	 * 
	 * @param name should be non-null
	 * @return the attribute with the given name, or null if not found
	 */
  Attribute getAttribute(String name);
  /**
   * 
   * @param name should be non-null
   * @return the value of the attribute with the given name, or null if not found
   */
  String getAttributeValue(String name);
  
  /**
   * Associate the attribute with this holder.
   * If the holder already has an attribute with the same name,
   * it will be replaced by this new one,
   * and the previous attribute will be returned.
   * @param attribute should be non-null
   * @return the attribute previously associated with this holder, with a same name as the new attribute.
   * Or null if there was no attribute with the same name. 
   */
  Attribute putAttribute(Attribute attribute);
  
  /**
   * 
   * @return an iterator over the names of all associated attributes of this holder
   */
  Iterator<String> getAttributeNames();
  
  /**
   * 
   * @return a read-only set of all associated attributes
   */
  Set<Attribute> getAttributes();

}
