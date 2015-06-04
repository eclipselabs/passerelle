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
package com.isencia.passerelle.message;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;

/**
 * MessageException
 * 
 * 
 * @author erwin
 */
public class MessageException extends PasserelleException {

  private static final long serialVersionUID = 1L;

  /**
   * @param errorCode
   * @param message
   * @param rootException
   */
  public MessageException(ErrorCode errorCode, String message, Throwable rootException) {
    super(errorCode, message, rootException);
  }
  /**
   * @param errorCode
   * @param message
   * @param context
   * @param rootException
   */
  public MessageException(ErrorCode errorCode, String message, ManagedMessage msgContext, Throwable rootException) {
    super(errorCode, message, null, msgContext, rootException);
  }
}
