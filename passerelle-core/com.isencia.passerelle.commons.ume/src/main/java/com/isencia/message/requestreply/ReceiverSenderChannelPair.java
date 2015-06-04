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
import com.isencia.message.ChannelException;
import com.isencia.message.IMessageListener;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.ISenderChannel;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.interceptor.IMessageInterceptorChain;

/**
 * Simple container for a pair of a receiver and a sender channel. Useful, e.g.
 * for request-reply semantics, where we need to be able to maintain knowledge
 * about which sender channel to use for sending a reply to a msg received via a
 * certain receiver channel.
 * 
 * @author erwin
 */
public class ReceiverSenderChannelPair implements IRequestReplyChannel {
  private IReceiverChannel rcvChannel;
  private ISenderChannel sndChannel;

  public ReceiverSenderChannelPair(IReceiverChannel rcvChannel, ISenderChannel sndChannel) {
    super();
    this.rcvChannel = rcvChannel;
    this.sndChannel = sndChannel;
  }

  protected IReceiverChannel getReceiverChannel() {
    return rcvChannel;
  }

  protected ISenderChannel getSenderChannel() {
    return sndChannel;
  }

  public void open() throws ChannelException {
    getReceiverChannel().open();
    try {
      getSenderChannel().open();
    } catch (ChannelException e) {
      // try to close the receiver again
      try {
        getReceiverChannel().close();
      } catch (ChannelException e1) {
      }
      throw e;
    }
  }

  public void close() throws ChannelException {
    ChannelException e = null;
    try {
      getReceiverChannel().close();
    } catch (ChannelException e1) {
      e = e1;
    }
    // even in case of error for rcv ch
    // still try to close send ch
    getSenderChannel().close();

    if (e != null) throw e;
  }

  public void sendResponse(Object response, Object correlationID) throws ChannelException {
    try {
      getSenderChannel().sendMessage(response);
    } catch (InterruptedException e) {
      throw new ChannelException("Interrupted :" + e.getMessage());
    }
  }

  public void setInterceptorChainForResponse(IMessageInterceptorChain interceptorChain) {
    getSenderChannel().setInterceptorChainOnEnter(interceptorChain);
  }

  public Collection<IMessageListener> getListeners() {
    return getReceiverChannel().getListeners();
  }

  public void addListener(IMessageListener newListener) {
    // only relevant for rcv channel
    getReceiverChannel().addListener(newListener);
  }

  public void addListeners(Collection<IMessageListener> newListeners) {
    getReceiverChannel().addListeners(newListeners);
  }

  public boolean removeListener(IMessageListener listener) {
    // only relevant for rcv channel
    return getReceiverChannel().removeListener(listener);
  }

  public boolean isOpen() {
    return getReceiverChannel().isOpen();
  }

  public IMessage receiveRequest() throws NoMoreMessagesException, ChannelException {

    return new RequestMessage(getMessage());
  }

  /**
   * BEWARE: this method returns just the message contents, no correlation info.
   */
  public Object getMessage() throws ChannelException, NoMoreMessagesException {
    return getReceiverChannel().getMessage();
  }

  public void setInterceptorChainOnLeave(IMessageInterceptorChain interceptorChain) {
    getReceiverChannel().setInterceptorChainOnLeave(interceptorChain);
  }

  public void run() {
    getReceiverChannel().run();
  }
}
