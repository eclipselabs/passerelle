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

/**
 * A MessageProvider provides messages to a MessageBuffer.
 * This is typically implemented in Passerelle receivers on input ports,
 * to deliver all received messages directly in one collecting buffer per port.
 *  
 * @author erwin
 *
 */
public interface MessageProvider {
  
  /**
   * Associate this provider with the given buffer.
   * 
   * @param buffer
   */
  void setMessageBuffer(MessageBuffer buffer);
  
  /**
   * Request the MessageProvider to finish it's stuff and clean up.
   * After this call, it is assumed that the MessageProvider has removed any
   * assocation to a buffer, and has cleaned all internal state.
   * So it can be reused in a next processing cycle, after a buffer has been set again.
   */
  void requestFinish();
}
