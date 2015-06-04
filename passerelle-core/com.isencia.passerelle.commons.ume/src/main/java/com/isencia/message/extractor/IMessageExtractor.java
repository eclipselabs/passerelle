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
package com.isencia.message.extractor;

import java.io.Reader;

/**
 * Interface for objects responsible for evaluating sequences of data elements
 * and extracting messages out of them, based on some specific strategy. REMARK:
 * NOT THREAD_SAFE! Do NOT use an extractor instance concurrently!
 * 
 * @author erwin
 */
public interface IMessageExtractor {

  /**
   * Starts the extraction process
   */
  public void open(Reader reader);

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Consecutive calls to getMessage() will sequentially feed character data to
   * the strategy's data buffer. The return object will be null for all
   * intermediate calls, and will contain the complete message when an end of
   * message is found.
   * 
   * @return java.lang.Object
   */
  public Object getMessage();

  /**
   * Stops the extraction process
   */
  public void close();

  /**
   * Creates a clone of the given extractor. In order to avoid all fancy clone()
   * issues, we just use our own method name...
   * 
   * @return
   */
  public IMessageExtractor cloneExtractor();

}