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
package com.isencia.message;

/**
 * Exception class for reporting errors in receiver and sender channel
 * operations.
 * 
 * @author erwin
 */
public class ChannelException extends MessageException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor for ChannelException.
   */
  public ChannelException() {
    super();
  }

  /**
   * Constructor for ChannelException.
   * 
   * @param s
   */
  public ChannelException(String s) {
    super(s);
  }

}
