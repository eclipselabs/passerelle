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

package com.isencia.passerelle.actor;

import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

/**
 * Exception class used to report validation errors, either
 * during the validation of the initialization of an actor,
 * or during the validation prior to an actor's fire-iteration.
 * 
 * @author erwin
 *
 */
public class ValidationException extends PasserelleException {

	/**
	 * @param message
	 * @param context
	 * @param rootException
   * @deprecated
	 */
	public ValidationException(String message, Object context, Throwable rootException) {
		super(message, context, rootException);
	}

	/**
	 * @param severity
	 * @param message
	 * @param context
	 * @param rootException
   * @deprecated
	 */
	public ValidationException(Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message, context, rootException);
	}

  /**
   * @param errorCode
   * @param message
   * @param modelElement
   * @param rootException
   */
  public ValidationException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
    super(errorCode, message, modelElement, rootException);
  }
}
