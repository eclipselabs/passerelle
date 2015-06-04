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
package com.isencia.passerelle.statistics;

import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

/**
 * Exception used to indicate that some statistics data
 * is not yet available because there's not enough runtime info yet
 * to calculate it...
 * 
 * @author erwin
 *
 */
public class InsufficientDataException extends PasserelleException {
	
  private static final long serialVersionUID = 1L;

  public InsufficientDataException() {
		super(ErrorCode.INFO,null,null,null);
	}

  /**
   * @param errorCode
   * @param message
   * @param modelElement
   * @param rootException
   */
  protected InsufficientDataException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
    super(errorCode, message, modelElement, rootException);
  }
}
