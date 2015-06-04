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
package com.isencia.message.generator;

import java.io.Writer;

/**
 * Interface for objects responsible for packaging individual messages in a
 * manner fit for a specific outgoing protocol/technology.
 * 
 * @author erwin
 */
public interface IMessageGenerator {

  /**
   * Open/initialize the generator
   */
  public void open(Writer writer);

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Close/cleanup the generator
   */
  public void close();

  /**
   * Accept a smsg, convert it into the right format for the outgoing
   * protocol/technology and write it out.
   * 
   * @param Object message The std passerelle msg
   */
  public void sendMessage(Object message) throws Exception;

  /**
   * Creates a clone of the given generator. In order to avoid all fancy clone()
   * issues, we just use our own method name...
   * 
   * @return
   */
  public IMessageGenerator cloneGenerator();

}
