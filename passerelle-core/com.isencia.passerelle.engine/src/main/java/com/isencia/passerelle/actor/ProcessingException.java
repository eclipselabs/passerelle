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
import com.isencia.passerelle.message.ManagedMessage;

/**
 * ProcessingException
 * 
 * An exception used to indicate errors during an
 * actor's processing in the fire loop.
 * 
 * @author erwin
 */
public class ProcessingException extends PasserelleException {
  private static final long serialVersionUID = 1L;

  /**
	 * Creates a new ProcessingException with NON_FATAL severity,
	 * and the given parameters.
	 * 
	 * @param message the classical message of pre-JDK1.4 exceptions
	 * @param context an object that can give additional info, e.g. input data
	 * that caused the problem (may be null)
	 * @param rootException an exception that may have caused the processing problem (may be null)
	 * @deprecated
	 */
	public ProcessingException(String message, Object context, Throwable rootException) {
		super(message,context,rootException);
	}

	/**
	 * 
	 * @param severity
	 * @param message
	 * @param context
	 * @param rootException
   * @deprecated
	 */	
	public ProcessingException(Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message,context,rootException);
	}

  /**
   * @param errorCode
   * @param message
   * @param modelElement
   * @param rootException
   */
  public ProcessingException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
    super(errorCode, message, modelElement, rootException);
  }

  /**
   * 
   * @param errorCode
   * @param message
   * @param modelElement
   * @param msgContext
   * @param rootException
   */
  public ProcessingException(ErrorCode errorCode, String message, Nameable modelElement, ManagedMessage msgContext, Throwable rootException) {
    super(errorCode, message, modelElement, msgContext, rootException);
  }
}
