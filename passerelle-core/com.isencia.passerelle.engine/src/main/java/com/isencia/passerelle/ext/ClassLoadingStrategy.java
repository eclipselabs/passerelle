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

package com.isencia.passerelle.ext;

import com.isencia.passerelle.validation.version.VersionSpecification;
import ptolemy.kernel.CompositeEntity;

/**
 * Strategy to be able to switch class loading (especially for actors and other model entities),
 * depending on the Passerelle runtime environment.
 * <p>
 * In a "plain" Java SE runtime, a default implementation would use simple <code>Class.forName()</code> 
 * (for Java classes) or local file-lookup (for actor-oriented classes) or similar.
 * In an OSGi-based runtime, more advanced options can be implemented to allow dynamic actor class updates, version management etc.
 * </p>
 * @author erwin
 *
 */
public interface ClassLoadingStrategy {
  
  /**
   * 
   * @param className
   * @param versionSpec
   * @return the Class for the given name
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("rawtypes")
  Class loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
  
  /**
   * 
   * @param className
   * @param versionSpec
   * @return
   * @throws ClassNotFoundException
   */
  CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
  
}
