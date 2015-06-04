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
 * TerminationException
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class TerminationException extends PasserelleException {

	/**
	 * Creates a new TerminationException with NON_FATAL severity,
	 * and the given parameters.
	 * 
	 * @param message
	 * @param context
	 * @param rootException
   * @deprecated
	 */
	public TerminationException(String message, Object context, Throwable rootException) {
		super(message, context, rootException);
	}
	/**
	 * @param severity
	 * @param message
	 * @param context
	 * @param rootException
   * @deprecated
	 */
	public TerminationException(Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message, context, rootException);
	}
  /**
   * @param errorCode
   * @param message
   * @param modelElement
   * @param rootException
   */
  public TerminationException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
    super(errorCode, message, modelElement, rootException);
  }
}
