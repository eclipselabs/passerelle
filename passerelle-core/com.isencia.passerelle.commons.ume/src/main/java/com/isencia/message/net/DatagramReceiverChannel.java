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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReceiverChannel;

/**
 * DatagramReceiverChannel TODO: class comment
 * 
 * @author erwin
 */
public class DatagramReceiverChannel extends ReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(DatagramReceiverChannel.class);

  private static final int defaultByteBufferSize = 8192;
  private DatagramSocket socket = null;
  private int port = -1;

  /**
   * @param socket
   */
  public DatagramReceiverChannel(DatagramSocket socket) {
    super();
    if (socket != null) {
      this.socket = socket;
      this.port = socket.getLocalPort();
    }
  }

  /**
   * @param port
   */
  public DatagramReceiverChannel(int port) {
    super();
    this.port = port;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (socket == null) {
      try {
        socket = new DatagramSocket(getPort());
      } catch (SocketException e) {
        logger.error("open() - Socket construction failed on port " + port, e);
        throw new ChannelException("Socket construction failed on port " + port);
      }
    }
    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /**
   * Gets the socket.
   * 
   * @return Returns a DatagramSocket
   */
  public DatagramSocket getSocket() {
    return socket;
  }

  /**
   * Gets the local port on which this socket is listening for datagrams.
   * 
   * @return Returns a int
   */
  public int getPort() {
    return port;
  }

  protected Object doGetMessage() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - entry");

    try {
      byte bb[] = new byte[defaultByteBufferSize];
      DatagramPacket packet = new DatagramPacket(bb, bb.length);
      getSocket().receive(packet);

      String msg = new String(bb, 0, packet.getLength());

      if (logger.isTraceEnabled()) logger.trace("doGetMessage() - exit : " + msg);
      return msg;
    } catch (IOException e) {
      throw new ChannelException("IOException while receiving DatagramPacket: " + e.getMessage());
    }
  }

}
