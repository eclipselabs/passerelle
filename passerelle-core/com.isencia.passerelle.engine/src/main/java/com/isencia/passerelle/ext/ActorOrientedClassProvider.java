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

import ptolemy.kernel.CompositeEntity;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * @author delerw
 *
 */
public interface ActorOrientedClassProvider {

  /**
   * Returns the <code>CompositeEntity</code> providing the requested actor-oriented class definition, if this provider has it.
   * <p>
   * An actor-oriented class can have only a "simple" name, i.e. no dots, as the underlying <code>NamedObj</code> does not allow dot-separated names.
   * However, provider implementations may allow <code>className</code> values with dots. 
   * Remark that once loaded in the Passerelle/Ptolemy runtime, all actor-oriented classes are managed by their "simple" name only!
   * So, when using dot-separated "nested" names, the final part must still be unique within the complete runtime!
   * </p>
   * <p>
   * So using hierarchical class names may make sense to support organizing the classes in hierarchical structures for storage and lookup, 
   * instead of being limited to simple "linear" lists. But they do not introduce a runtime "name space" like packages do for Java classes. 
   * </p>
   * If this provider doesn't have this class available, it should throw a <code>ClassNotFoundException</code>.
   * (Optionally, it could also just return null, for those dvp-ers who don't like exceptions. ;-) )
   * 
   * @param className Mandatory, not-null. 
   * @param versionSpec optional constraint on desired version for the class that must be provided. If null, no version constraint is imposed.
   * @return the actor-oriented class matching the given className
   * @throws ClassNotFoundException if this provider can not provide the requested class for the requested version (if specified)
   */
  CompositeEntity getActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
}
