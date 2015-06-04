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
 * Send channels encapsulate different outgoing protocols/technologies. A
 * channel uses an IMessageGenerator to transform the passerelle internal msg
 * format into the relevant wire-format for the channel protocol/technology.
 * Send channels can be used in 2 modes: - calling sendMessage(Object)
 * synchronously sends out the parameter message - calling messageAvailable()
 * will cause the channel to check on its providers for available messages. All
 * available messages from all its providers will be sent out.
 * 
 * @author erwin
 */
public interface ISenderChannel {

  /**
   * Opens the channel for sending out msgs.
   * 
   * @throws ChannelException
   */
  public void open() throws ChannelException;

  /**
   * @return true if in open state
   */
  public boolean isOpen();

  /**
   * Closes the channel. Any msg data that might still be waiting/buffered will
   * still be sent out. All msg data arriving after a call to close() will be
   * ignored.
   * 
   * @throws ChannelException
   */
  public void close() throws ChannelException;

  /**
   * @param message
   * @throws ChannelException
   * @throws InterruptException
   */
  public void sendMessage(Object message) throws InterruptedException, ChannelException;

  /**
   * Notifies the sender channel that a msg is available on one of its message
   * providers
   */
  public void messageAvailable();

  /**
   * Registers a new provider
   * 
   * @param IMessageProvider
   */
  public void addProvider(IMessageProvider provider);

  /**
   * Registers a collection of providers
   * 
   * @param Collection
   */
  public void addProviders(Collection<IMessageProvider> providers);

  /**
   * Returns all registered providers
   * 
   * @return Collection
   */
  public Collection<IMessageProvider> getProviders();

  /**
   * Removes the given provider from the list of registered providers.
   * 
   * @param IMessageProvider
   * @return boolean true if the provider was registered
   */
  public boolean removeProvider(IMessageProvider provider);

  /**
   * Registers a chain of interceptors that will be able to act on each message
   * that is passed into this channel. The interceptors will receive the message
   * right after it enters the channel. After that, the channel will send out
   * the message (it may have been modified by the interceptors).
   * 
   * @param interceptorChain
   */
  public void setInterceptorChainOnEnter(IMessageInterceptorChain interceptorChain);

}
