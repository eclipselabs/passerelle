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

package com.isencia.passerelle.ext.impl;

import ptolemy.kernel.CompositeEntity;
import com.isencia.passerelle.ext.ClassLoadingStrategy;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * As the name says... a simple strategy implementation providing a bridge 
 * between the <code>ClassLoadingStrategy</code> approach and 
 * the usage of a plain <code>ClassLoader</code>, for loading Java classes.
 * <br/>
 * REMARK : It does not support loading actor-oriented classes! 
 * <p>
 * The only special thing is an alias lookup for class names starting with "be.isencia"
 * to look for matching "com.isencia" classes. This is to be able to load old (pre-v7)
 * Passerelle models.
 * </p>
 * 
 * @author delerw
 *
 */
public class SimpleClassLoadingStrategy implements ClassLoadingStrategy {
  
  private ClassLoader classLoader;

  /**
   * Constructor for instance that uses the default class loader, 
   * i.e. the one with which this own class was loaded.
   */
  public SimpleClassLoadingStrategy() {
    classLoader = getClass().getClassLoader();
  }

  /**
   * Enforces the usage of the given class loader.
   * 
   * @param classLoader
   */
  public SimpleClassLoadingStrategy(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @SuppressWarnings("rawtypes")
  public Class loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    Class newClass=null;
    try {
      newClass = Class.forName(className, true, classLoader);
    } catch(Exception e) {
      // if className not found and it starts with "be.", 
      // try it for alias starting with "com."
      className = className.replace("be.isencia", "com.isencia");
      newClass = Class.forName(className, true, classLoader);
    }
    return newClass;
  }

  public CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    throw new ClassNotFoundException();
  }
}
