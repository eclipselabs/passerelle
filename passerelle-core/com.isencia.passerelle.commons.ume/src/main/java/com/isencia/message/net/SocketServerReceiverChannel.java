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
package com.isencia.message.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.IMessageReceiver;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.MessageReceiver;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.ReceiverChannel;
import com.isencia.message.extractor.IMessageExtractor;

/**
 * SocketServerReceiverChannel TODO: class comment
 * 
 * @author erwin
 */
public class SocketServerReceiverChannel extends ReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(SocketServerReceiverChannel.class);

  private ServerSocket sSocket = null;
  private IMessageExtractor extractor = null;
  private IMessageReceiver receiver = null;

  /**
   * @param sSocket
   * @param extractor
   */
  public SocketServerReceiverChannel(ServerSocket sSocket, IMessageExtractor extractor) {
    super();
    if (sSocket != null) {
      setSocket(sSocket);
    }
    setExtractor(extractor);
    receiver = new MessageReceiver();
  }

  /**
   * Returns the collection of subchannels, each entry being a
   * SocketReceiverChannel.
   * 
   * @return java.util.Collection
   */
  public Collection<IReceiverChannel> getSubChannels() {
    return receiver.getChannels();
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
        try {
          newExtractor = (IMessageExtractor) getExtractor().getClass().newInstance();
          // create and open new channel around new client socket
          SocketReceiverChannel ch = new SocketReceiverChannel(s, newExtractor);
          receiver.addChannel(ch);
          // start the channel in "push" mode,
          // so the receiver can accumulate all received msgs
          ch.start();
        } catch (IllegalAccessException e) {
          logger.error("run() - Unable to create new channel", e);
          try {
            s.close();
          } catch (IOException ex) {
          }
          ;
        } catch (InstantiationException e) {
          logger.error("run() - Unable to create new channel", e);
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
    receiver.close();
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

  protected Object doGetMessage() throws ChannelException, NoMoreMessagesException {
    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - entry");

    Object msg = receiver.getMessage();

    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - exit : " + msg);

    return msg;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    super.open();
    receiver.open();
    start();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

}
