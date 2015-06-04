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
 * Exception class, used to indicate problems during loading a hierarchical
 * properties definition.
 * 
 * @author erwin
 */
public class PropertiesLoadingException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * PropertiesLoadingException constructor with a message.
   * 
   * @param s java.lang.String an error message
   */
  public PropertiesLoadingException(String s) {
    super(s);
  }

  /**
   * PropertiesLoadingException constructor with a message and a nested
   * exception.
   * 
   * @param s java.lang.String an error message
   * @param t java.lang.Throwable a nested exception
   */
  public PropertiesLoadingException(String s, Throwable t) {
    super(s);
  }

}