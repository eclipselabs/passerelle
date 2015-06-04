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
package com.isencia.message.net.requestreply;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.ReceiverChannel;
import com.isencia.message.extractor.IMessageExtractor;
import com.isencia.message.generator.IMessageGenerator;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.net.SocketReceiverChannel;
import com.isencia.message.net.SocketSenderChannel;
import com.isencia.message.requestreply.IMessage;
import com.isencia.message.requestreply.IRequestReplier;
import com.isencia.message.requestreply.IRequestReplyChannel;
import com.isencia.message.requestreply.ReceiverSenderChannelPair;
import com.isencia.message.requestreply.RequestReplier;

/**
 * SocketServerRequestReplier TODO: class comment
 * 
 * @author erwin
 */
public class SocketServerRequestReplier extends ReceiverChannel implements IRequestReplyChannel {
  private final static Logger logger = LoggerFactory.getLogger(SocketServerRequestReplier.class);

  private IMessageInterceptorChain interceptorChainForResponse = null;

  private ServerSocket sSocket = null;
  private IMessageExtractor extractor = null;
  private IMessageGenerator generator = null;
  private IRequestReplier reqReplier = null;

  /**
   * @param sSocket
   * @param extractor
   */
  public SocketServerRequestReplier(ServerSocket sSocket, IMessageExtractor extractor, IMessageGenerator generator) {
    super();
    if (sSocket != null) {
      setSocket(sSocket);
    }
    setExtractor(extractor);
    setGenerator(generator);
    reqReplier = new RequestReplier();
  }

  /**
   * Gets the socket.
   * 
   * @return Returns a Socket
   */
  public ServerSocket getSocket() {
    return sSocket;
  }

  /**
   * Sets the socket.
   * 
   * @param socket The socket to set
   */
  public void setSocket(ServerSocket sSocket) {
    this.sSocket = sSocket;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run() {
    if (logger.isTraceEnabled()) logger.trace("run() - entry");
    for (;;) {
      Socket s = null;
      try {
        s = getSocket().accept();
      } catch (SocketException e) {
        logger.info("run() - " + e.getMessage() + ". Terminating...");
        break;
      } catch (IOException e) {
        logger.info("run() - " + e.getMessage() + ". Terminating...");
        break;
      }
      logger.info("run() - Accepted connection request. Socket: " + s);
      if (isInterrupted()) {
        break;
      } else {
        IMessageExtractor newExtractor = null;
        IMessageGenerator newGenerator = null;
        try {
          newExtractor = (IMessageExtractor) getExtractor().cloneExtractor();
          newGenerator = (IMessageGenerator) getGenerator().cloneGenerator();
          // create and open new channel around new client socket
          SocketReceiverChannel rcvCh = new SocketReceiverChannel(s, newExtractor);
          SocketSenderChannel sndCh = new SocketSenderChannel(s, newGenerator);
          sndCh.setInterceptorChainOnEnter(interceptorChainForResponse);

          ReceiverSenderChannelPair chPair = new ReceiverSenderChannelPair(rcvCh, sndCh);
          reqReplier.addChannelPair(chPair);
          chPair.open();

          // start the channel in "push" mode,
          // so the receiver can accumulate all received msgs
          rcvCh.start();
        } catch (ChannelException e) {
          logger.error("run() - Unable to start new channel", e);
          try {
            s.close();
          } catch (IOException ex) {
          }
          ;
        }
      }
    }
    // close server socket
    try {
      getSocket().close();
    } catch (IOException e) {
    }

    if (logger.isTraceEnabled()) logger.trace("run() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    super.close();
    reqReplier.close();
    interrupt();
    try {
      getSocket().close();
    } catch (IOException e) {
      logger.error("close() - Error closing server socket", e);
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  /**
   * Returns the extractor.
   * 
   * @return IMessageExtractor
   */
  public IMessageExtractor getExtractor() {
    return extractor;
  }

  /**
   * Sets the extractor.
   * 
   * @param extractor The extractor to set
   */
  public void setExtractor(IMessageExtractor extractor) {
    this.extractor = extractor;
  }

  protected IMessageGenerator getGenerator() {
    return generator;
  }

  protected void setGenerator(IMessageGenerator generator) {
    this.generator = generator;
  }

  public IMessage receiveRequest() throws NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("receiveRequest() - entry");

    IMessage msg = reqReplier.receiveRequest();

    if (logger.isTraceEnabled()) logger.trace("receiveRequest() - exit : " + msg);

    return msg;
  }

  /**
   * BEWARE: this method returns just the message contents, no correlation info.
   */
  protected Object doGetMessage() throws ChannelException, NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - entry");

    Object msg = reqReplier.receiveRequest().getMessage();

    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - exit : " + msg);

    return msg;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    super.open();
    reqReplier.open();
    start();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public void sendResponse(Object response, Object correlationID) throws ChannelException {
    reqReplier.sendResponse(response, correlationID);
  }

  public void setInterceptorChainForResponse(IMessageInterceptorChain interceptorChain) {
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnEnter() - entry - chain :" + interceptorChain);
    this.interceptorChainForResponse = interceptorChain;
    if (logger.isTraceEnabled()) logger.trace("setInterceptorChainOnEnter() - exit");
  }

}
