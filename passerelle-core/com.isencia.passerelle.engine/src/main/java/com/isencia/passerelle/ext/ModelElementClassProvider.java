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

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * Can be used to get the classes for different types of model elements.
 * Most important cases are actors and directors.
 * But also dedicated port or parameter classes can be offered via implementations of this interface.
 *  
 * @author delerw
 *
 */
public interface ModelElementClassProvider {

  /**
   * Return the requested class for the requested version (if specified).
   * If this provider doesn't have this class available, it should throw a <code>ClassNotFoundException</code>.
   * (Optionally, it could also just return null, for those dvp-ers who don't like exceptions. ;-) )
   * 
   * @param className typically a fully qualified Java class name. Mandatory non-null.
   * @param versionSpec optional constraint on desired version for the class that must be provided. If null, no version constraint is imposed.
   * @return the concrete class of the <code>NamedObj</code> matching the given className.
   * @throws ClassNotFoundException if this provider can not provide the requested class for the requested version (if specified)
   * 
   * @see VersionSpecification
   */
  Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
}
