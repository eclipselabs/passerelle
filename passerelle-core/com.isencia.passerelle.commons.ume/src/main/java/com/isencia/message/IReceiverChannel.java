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
import com.isencia.message.interceptor.IMessageInterceptorChain;

/**
 * Receiver channels encapsulate different incoming protocols/technologies. A
 * channel uses an IMessageExtractor strategy to decide how to "chop" messages
 * from the reader/source.
 * 
 * @author erwin
 */
public interface IReceiverChannel extends Runnable {

  /**
   * Obtain the collection of IMessageListeners registered with this channel.
   * 
   * @return java.util.Collection
   */
  public Collection<IMessageListener> getListeners();

  /**
   * Adds a reference to an IMessageListener
   * 
   * @param newListener
   */
  public void addListener(IMessageListener newListener);

  /**
   * Adds a collection of IMessageListeners
   * 
   * @param newListeners
   */
  public void addListeners(Collection<IMessageListener> newListeners);

  /**
   * Removes a listener from the channel.
   * 
   * @param IMessageListener
   * @return boolean true if remove successfull, false if element not found
   */
  public boolean removeListener(IMessageListener listener);

  /**
   * Starts the channel
   * 
   * @throws ChannelException
   */
  public void open() throws ChannelException;

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Stops the channel
   * 
   * @throws ChannelException
   */
  public void close() throws ChannelException;

  /**
   * Returns the next message from the channel's data source. returns the
   * message if One was found, null otherwise.
   * 
   * @return Object.
   * @exception ChannelException
   * @exception NoMoreMessagesException
   */
  public Object getMessage() throws ChannelException, NoMoreMessagesException;

  /**
   * Registers a chain of interceptors that will be able to act on each message
   * that is passed into this channel. The interceptors will receive the message
   * right before it leaves the channel. After that, the channel will deliver
   * the message to its listeners (it may have been modified by the
   * interceptors).
   * 
   * @param interceptorChain
   */
  public void setInterceptorChainOnLeave(IMessageInterceptorChain interceptorChain);

}
