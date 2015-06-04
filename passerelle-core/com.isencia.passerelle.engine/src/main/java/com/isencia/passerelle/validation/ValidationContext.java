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

package com.isencia.passerelle.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.actor.ValidationException;

/**
 * A <code>ValidationContext</code> serves as a simple container to store validation information.
 * 
 * @author erwin
 */
public class ValidationContext {

  private Map<Nameable, Set<ValidationException>> errors = new HashMap<Nameable, Set<ValidationException>>();

  /**
   * @param e
   *          the validation error that was found. It contains the validated element as its exception's model element context.
   * @return true if the ValidationException was successfully added, false if not (e.g. because it did not have a model element context)
   * @see ValidationException
   */
  public boolean addError(ValidationException e) {
    if (e != null && e.getModelElement() != null) {
      Set<ValidationException> _errs = errors.get(e.getModelElement());
      if(_errs==null) {
        _errs = new TreeSet<ValidationException>();
        errors.put(e.getModelElement(), _errs);
      }
      _errs.add(e);
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return all elements with validation errors, as determined during a validation check for this context
   */
  public Collection<Nameable> getElementsWithErrors() {
    return Collections.unmodifiableSet(errors.keySet());
  }

  /**
   * @param validatedElement
   * @return all validation errors found for the given model element, during a validation check for this context
   * Returns the errors in order from most severe to least severe, and for same severities : ordered by code.
   */
  public Collection<ValidationException> getErrors(Nameable validatedElement) {
    return Collections.unmodifiableSet(errors.get(validatedElement));
  }

  /**
   * @return true if no validation errors were found in this context; false otherwise
   */
  public boolean isValid() {
    return errors.isEmpty();
  }

}
