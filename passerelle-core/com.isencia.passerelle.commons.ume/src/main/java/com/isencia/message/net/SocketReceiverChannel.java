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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReaderReceiverChannel;
import com.isencia.message.extractor.IMessageExtractor;

/**
 * SocketReceiverChannel TODO: class comment
 * 
 * @author erwin
 */
public class SocketReceiverChannel extends ReaderReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(SocketReceiverChannel.class);

  private Socket socket = null;

  /**
   * @param socket
   * @param extractor
   */
  public SocketReceiverChannel(Socket socket, IMessageExtractor extractor) {
    super(extractor);
    if (socket != null) {
      this.socket = socket;
    }
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (getReader() != null) {
      try {
        getReader().close();
      } catch (IOException e) {
        logger.error("open() - Could not close reader", e);
      }
      setReader(null);
    }

    try {
      Reader rdr = new InputStreamReader(getSocket().getInputStream());
      setReader(rdr);
    } catch (IOException e) {
      logger.error("open() - Error getting inputstream from socket", e);
      throw new ChannelException("Error getting inputstream from socket " + e.getMessage());
    }
    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /**
   * Gets the socket.
   * 
   * @return Returns a Socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Sets the socket.
   * 
   * @param socket The socket to set
   */
  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  public void close() throws ChannelException {
    try {
      getSocket().close();
    } catch (Exception e) {
    }
    super.close();
  }
}
