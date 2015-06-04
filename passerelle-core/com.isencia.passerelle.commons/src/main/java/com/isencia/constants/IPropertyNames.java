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
package com.isencia.constants;

/**
 * IPropertyNames A couple of core system properties for iSencia's fwk
 * components and applications.
 * 
 * @author erwin
 */
public interface IPropertyNames {
  // a property for specifying the root folder
  // in an application's folder structure
  public final static String APP_HOME = "com.isencia.home";
  // if the property is not set, use this as default value
  // i.e. the current working directory of the Java application
  public final static String APP_HOME_DEFAULT = ".";
  // i.e. the conf folder in application's the root folder.
  public final static String APP_CFG_DEFAULT = "conf";
}
