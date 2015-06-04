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
 * Interface for message senders, implemented using one or more sender channels.
 * Depending on the message sender implementation, it could for example send out
 * each message on ALL channels, use a round-robin load-balancing algorithm,
 * route specific messages on specific channels etc.
 * 
 * @author erwin
 */
public interface IMessageSender {

  /**
   * @param message
   * @return boolean to indicate whether the sending was successful
   */
  public boolean sendMessage(Object message);

  /**
   * Adds a sender Channel.
   * 
   * @param IMessageChannel Channel
   */
  public void addChannel(ISenderChannel Channel);

  /**
   * Removes a sender Channel
   * 
   * @param channel the channel that should be removed from the sender's
   *          operations
   * @return boolean true if the Channel was registered with the sender false if
   *         it was not found
   */
  public boolean removeChannel(ISenderChannel Channel);

  /**
   * Obtain the collection of Channels registered with this sender.
   * 
   * @return Collection
   */
  public Collection<ISenderChannel> getChannels();

  /**
   * Starts the sender
   */
  public void open();

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Stops the sender
   */
  public void close();

}