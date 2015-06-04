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
 * An own EOFException to ensure independence from java.io.
 * 
 * @author erwin
 */
public class EndOfDataException extends MessageException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor for EOFException.
   */
  public EndOfDataException() {
    super();
  }

  /**
   * Constructor for EOFException.
   * 
   * @param s
   */
  public EndOfDataException(String s) {
    super(s);
  }

}
