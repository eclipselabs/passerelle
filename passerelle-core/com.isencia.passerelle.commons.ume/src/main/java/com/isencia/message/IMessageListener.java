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
 * Interface for all objects interested in receiving messages in an observer
 * mode, not a polling mode.
 * 
 * @author erwin
 */
public interface IMessageListener {
  /**
   * Delivers a new message to the listener. Blocking call, waits until the
   * message is processed by the listener.
   */
  public void acceptMessage(Object request, IReceiverChannel source) throws InterruptedException, Exception;

  /**
   * Called by the message source to indicate that it is open for business.
   * 
   * @param source
   */
  public void sourceOpened(IReceiverChannel source);

  /**
   * Called by the message source to indicate that it is closed.
   * 
   * @param source
   */
  public void sourceClosed(IReceiverChannel source);

}
