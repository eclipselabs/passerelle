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
package com.isencia.properties;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Interface for manipulating hierarchical properties definitions. Besides the
 * hierarchical property retrieval methods, there is also a method
 * getLinearProperties(), to obtain a traditional java.util.Properties bundle.
 * 
 * @author erwin
 */
public interface IHierarchicProperty {
  /**
   * Returns a traditional java.util.Properties representation of these
   * hierarchical properties.
   * 
   * @return java.util.Properties
   */
  Properties getLinearProperties();

  /**
   * Returns the name of this hierarchic property.
   * 
   * @return java.lang.String
   */
  String getName();

  /**
   * Returns a sub-property that has the given nested name in this hierarchy.
   * The sub-property is also IHierarchicProperty.
   * 
   * @return be.isencia.properties.IHierarchicProperty
   * @param propName java.lang.String
   */
  IHierarchicProperty getProperty(String propName);

  /**
   * Returns a list of all sub-properties that have the given nested name in
   * this hierarchy. The sub-properties are also IHierarchicProperty-s.
   * 
   * @return java.util.List
   * @param propName java.lang.String
   */
  List<IHierarchicProperty> getPropertyList(String propName);

  /**
   * Returns a map of property name/value pairs, for all properties within the
   * given scope in this hierarchy.
   * 
   * @return java.util.Map
   * @param propName java.lang.String
   */
  Map<String,String> getPropertyMap(String propName);

  /**
   * Returns the value of the property with the given nested/hierarchical name
   * in this hierarchy. Names must have the format basename.subname1.subname2
   * etc. If the name is null or empty, this method returns the value of the
   * hierarchy root.
   * 
   * @return java.lang.String
   * @param propName java.lang.String
   */
  String getPropertyValue(String propName);

  /**
   * Returns the value of the property with the given nested/hierarchical name
   * in this hierarchy. Names must have the format basename.subname1.subname2
   * etc. If the property with the given name is not defined, return a given
   * default value.
   * 
   * @return java.lang.String
   * @param propName java.lang.String
   * @param defaultValue java.lang.String
   */
  String getPropertyValue(String propName, String defaultValue);

  /**
   * Returns a list of property values, for all properties with the given name
   * in this hierarchy.
   * 
   * @return java.lang.String[]
   * @param propName java.lang.String
   */
  String[] getPropertyValueList(String propName);
}