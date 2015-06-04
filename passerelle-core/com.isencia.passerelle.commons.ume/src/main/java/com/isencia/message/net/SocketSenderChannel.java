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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.WriterSenderChannel;
import com.isencia.message.generator.IMessageGenerator;

/**
 * SocketSenderChannel TODO: class comment
 * 
 * @author erwin
 */
public class SocketSenderChannel extends WriterSenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(SocketSenderChannel.class);

  private Socket socket = null;

  /**
   * @param socket
   * @param generator
   */
  public SocketSenderChannel(Socket socket, IMessageGenerator generator) {
    super(generator);
    if (socket != null) {
      this.socket = socket;
    }
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (getWriter() != null) {
      try {
        getWriter().close();
      } catch (IOException e) {
        logger.error("open() - Could not close reader", e);
      }
      setWriter(null);
    }

    try {
      Writer rdr = new OutputStreamWriter(socket.getOutputStream());
      setWriter(rdr);
    } catch (IOException e) {
      logger.error("open() - Error getting outputstream from socket", e);
      throw new ChannelException("Error getting outputstream from socket " + e.getMessage());
    }
    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /**
   * Gets the socket.
   * 
   * @return Returns a DatagramSocket
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
