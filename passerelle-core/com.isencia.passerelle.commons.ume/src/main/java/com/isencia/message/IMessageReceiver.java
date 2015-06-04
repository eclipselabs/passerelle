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

import java.util.Collection;

/**
 * Interface for message receivers. Implementations are often asynchronous and
 * then use a blocking queue to store all incoming messages from a number of
 * IReceiverChannels, and offer a blocking getMessage() to handle the incoming
 * messages.
 * 
 * @author erwin
 */
public interface IMessageReceiver extends IMessageListener {
  /**
   * Checks whether the IMessageServer has messages available.
   */
  public boolean hasMessage();

  /**
   * Gets a message from the receiver, and removes it from the receiver internal
   * message queue. Blocking method, waits until a message is available.
   * 
   * @return a message
   * @throws NoMoreMessagesException
   */
  public Object getMessage() throws NoMoreMessagesException;

  /**
   * Adds a receiver channel.
   * 
   * @param IReceiverChannel channel
   */
  public void addChannel(IReceiverChannel channel);

  /**
   * Removes a receiver channel
   * 
   * @param channel the channel to be removed from the receiver's operations
   * @return boolean true if the channel was registered with the receiver false
   *         if it was not found
   */
  public boolean removeChannel(IReceiverChannel channel);

  /**
   * Obtain the collection of channels registered with this receiver.
   * 
   * @return Collection
   */
  public Collection<IReceiverChannel> getChannels();

  /**
   * Starts the receiver
   */
  public void open();

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Stops the receiver
   */
  public void close();

}