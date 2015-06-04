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
 * Interface for objects that are able to provide messages, e.g. to a sender
 * channel.
 * 
 * @author erwin
 */
public interface IMessageProvider {

  /**
   * @return indication whether the provider is operational or not.
   */
  public boolean isOpen();

  /**
   * @return indication whether the provider has a message available or not.
   */
  public boolean hasMessage();

  /**
   * Gets a message from the server, and removes it from the provider's internal
   * message queue. If no message available, throw an exception.
   * 
   * @return Object
   * @throws NoMoreMessagesException
   */
  public Object getMessage() throws NoMoreMessagesException;
}
