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

import java.io.Serializable;

/**
 * A generic container for named values, where values can be of any Serializable type.
 * <p>
 * Many types of elements in the Passerelle process domain model are basically name/value-pairs.
 * E.g. request attributes, result items etc.
 * </p>
 * 
 * @author erwin
 * 
 */
public interface NamedValue<V extends Serializable> extends Serializable, Cloneable {

  /**
  * @return a deep clone of the instance
  */	
  NamedValue<V> clone() throws CloneNotSupportedException;
  
  /**
  * 
  * @return the name
  */
  String getName();

  /**
   * 
   * @return the value in its "raw" type/format
   */
  V getValue();

  /**
   * 
   * @return the value in a String-representation
   */
  String getValueAsString();
  /**
   * 
   * @return the scope in a String-representation
   */
  String getScope();

}
