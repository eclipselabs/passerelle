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

import com.isencia.message.ChannelException;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.interceptor.IMessageInterceptorChain;

/**
 * A sort of facade for request reply semantics on messaging channels. In
 * theory, request/reply may utilize 2 non-similar channels for receive a
 * request and sending a reply (e.g. a socket receiver channel and a file sender
 * channel). In practice this does not really make a lot of sense, and most
 * often we will receive and send msgs via similar channels. In such cases, this
 * contract should be used. It extends IReceiverChannel with some methods
 * adapted to request/reply semantics.
 * 
 * @author erwin
 */
public interface IRequestReplyChannel extends IReceiverChannel {

  /**
   * This method should be used for correct request/reply semantics. The
   * traditional IReceiverChannel.getMessage() returns just the message
   * contents, and as such there is no correlation information available for the
   * response later on... The IMessage structure provides this correlation info.
   * 
   * @return a message
   * @throws NoMoreMessagesException
   * @throws ChannelException
   */
  public IMessage receiveRequest() throws NoMoreMessagesException, ChannelException;

  /**
   * @param response
   * @param correlationID
   * @throws ChannelException
   */
  public void sendResponse(Object response, Object correlationID) throws ChannelException;

  /**
   * Registers a chain of interceptors that will be able to act on each message
   * that is passed into this channel. The interceptors will receive the message
   * right after it enters the channel. After that, the channel will send out
   * the message (it may have been modified by the interceptors).
   * 
   * @param interceptorChain
   */
  public void setInterceptorChainForResponse(IMessageInterceptorChain interceptorChain);

}
