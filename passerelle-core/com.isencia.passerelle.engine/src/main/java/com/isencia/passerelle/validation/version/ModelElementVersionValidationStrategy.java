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

package com.isencia.passerelle.validation.version;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.actor.ValidationException;

/**
 * Basic contract for validating the version of an element of a Passerelle model.
 * <p>
 * Depending on the concrete implementation, extra information may be needed
 * to perform the validation. E.g. a valid minimal or maximal version specification,
 * or a range of acceptable versions etc. This must be set on the strategy's implementation
 * instance in a dedicated way, matching the concrete needs.
 * </p>
 * @author erwin
 *
 */
public interface ModelElementVersionValidationStrategy {

  /**
   * 
   * @param versionedElement
   * @param versionToBeValidated
   * @throws ValidationException when the element's version does not match the
   * environment's requirements.
   */
  void validate(NamedObj versionedElement, VersionSpecification versionToBeValidated) throws ValidationException;
}
