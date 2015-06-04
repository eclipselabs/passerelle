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
package com.isencia.message.requestreply;

import java.util.Collection;
import com.isencia.message.NoMoreMessagesException;

/**
 * @todo Class comment
 * @author erwin
 */
public interface IRequestReplier {
  /**
   * Checks whether the IRequestReplier has messages available.
   */
  public boolean hasMessage();

  /**
   * Gets a message from the receiver, and removes it from the receiver internal
   * message queue. Blocking method, waits until a message is available.
   * 
   * @return a message
   * @throws NoMoreMessagesException
   */
  public IMessage receiveRequest() throws NoMoreMessagesException;

  /**
   * Sends a message in response to a received request msg, which is identified
   * with the correlationID parameter. The IRequestReplier will find out itself
   * where the response must be sent to, using the correlationID as lookup key.
   * 
   * @param message
   * @return boolean to indicate whether the sending was successful
   */
  public boolean sendResponse(Object response, Object correlationID);

  /**
   * Adds a pair of receiver channel and sender channel.
   * 
   * @param channelPair
   */
  public void addChannelPair(ReceiverSenderChannelPair channelPair);

  /**
   * Removes a pair of receiver channel and sender channel.
   * 
   * @param channelPair
   * @return boolean true if the channel pair was registered false if it was not
   *         found
   */
  public boolean removeChannelPair(ReceiverSenderChannelPair channelPair);

  /**
   * Obtain the collection of channel pairs registered with this receiver.
   * 
   * @return the channel pairs
   */
  public Collection<ReceiverSenderChannelPair> getChannelPairs();

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
