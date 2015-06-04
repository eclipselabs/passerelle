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

/**
 * Interface for manipulating hierarchical properties definitions. Besides the
 * hierarchical property retrieval methods, there is also a method
 * getLinearProperties(), to obtain a traditional java.util.Properties bundle.
 * 
 * @author erwin
 */
public interface IPropertyContainer extends IHierarchicProperty {
  /**
   * Returns the name of the source where the property settings were obtained.
   * 
   * @return java.lang.String
   */
  String getSourceName();

  /**
   * Load the hierarchical property definitions from the given Reader.
   * 
   * @param inp java.io.Reader
   */
  void loadProperties(java.io.Reader inp) throws PropertiesLoadingException;

  /**
   * Sets the name of the source where the property settings were obtained.
   * 
   * @param sourceName java.lang.String
   */
  void setSourceName(String sourceName);
}