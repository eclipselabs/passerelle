/* Copyright 2013 - iSencia Belgium NV

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

import java.util.Map;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * A simple provider that gets a list of classes to be provided in its constructor.
 * 
 * @author erwin
 * 
 */
public class DefaultModelElementClassProvider implements ModelElementClassProvider {

  private Class<? extends NamedObj>[] knownClasses;
  private Map<String, Class<? extends NamedObj>> aliasMap;

  /**
   * @param aliasMap
   * @param knownClasses
   */
  public DefaultModelElementClassProvider(Map<String, Class<? extends NamedObj>> aliasMap, Class<? extends NamedObj>... knownClasses) {
    this.knownClasses = knownClasses;
    this.aliasMap = aliasMap;
  }

  /**
   * @param knownClasses
   */
  public DefaultModelElementClassProvider(Class<? extends NamedObj>... knownClasses) {
    this.knownClasses = knownClasses;
  }

  public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    if (aliasMap != null && aliasMap.containsKey(className)) {
      return aliasMap.get(className);
    }
    for (Class<? extends NamedObj> knownClass : knownClasses) {
      if (knownClass.getName().equals(className)) {
        return knownClass;
      }
    }
    throw new ClassNotFoundException(className);
  }

}
