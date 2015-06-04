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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.SenderChannel;

/**
 * DatagramSenderChannel TODO: class comment
 * 
 * @author erwin
 */
public class DatagramSenderChannel extends SenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(DatagramSenderChannel.class);

//  private static final int defaultByteBufferSize = 8192;
  private DatagramSocket socket = null;
  private int remotePort = -1;
  private String remoteHostName = "";
  private InetAddress remoteHost = null;

  /**
   * @param remoteHost
   * @param remotePort
   * @throws ChannelException
   */
  public DatagramSenderChannel(String remoteHost, int remotePort) throws ChannelException {
    super();
    setRemoteHostName(remoteHost);
    setRemotePort(remotePort);
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (socket == null) {
      try {
        socket = new DatagramSocket();
      } catch (SocketException e) {
        throw new ChannelException("Socket construction failed " + e.getMessage());
      }
    }

    if (!isOpen()) {
      super.open();
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    if (isOpen()) {
      super.close();
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
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
   * Gets the remotePort.
   * 
   * @return Returns a int
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * Sets the remotePort.
   * 
   * @param remotePort The remotePort to set
   */
  public void setRemotePort(int remotePort) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("setRemotePort() - entry :" + remotePort);

    if (remotePort < 0) {
      throw new ChannelException("Remote port (" + remotePort + ") not set correctly");
    }

    this.remotePort = remotePort;

    if (logger.isTraceEnabled()) logger.trace("setRemotePort() - exit");
  }

  /**
   * Gets the remoteHost.
   * 
   * @return Returns a String
   */
  public String getRemoteHostName() {
    return remoteHostName;
  }

  /**
   * Sets the remoteHost.
   * 
   * @param remoteHost The remoteHost to set
   * @throws ChannelException
   */
  public void setRemoteHostName(String remoteHost) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("setRemoteHostName() - entry :" + remoteHost);

    if (remoteHost == null || remotePort < 0) {
      throw new ChannelException("Remote host (" + remoteHost + ") not set correctly");
    }

    remoteHostName = remoteHost;

    try {
      this.remoteHost = InetAddress.getByName(remoteHost);
    } catch (UnknownHostException e) {
      throw new ChannelException("Unknown host " + remoteHost);
    }

    if (logger.isTraceEnabled()) logger.trace("setRemoteHostName() - exit");
  }

  /**
   * Set the host and port (where to send datagrams to) in one shot.
   * 
   * @param remoteHost
   * @param remotePort
   * @throws ChannelException
   */
  public void setDestination(String remoteHost, int remotePort) throws ChannelException {
    setRemoteHostName(remoteHost);
    setRemotePort(remotePort);
  }

  protected void doSendMessage(Object message) throws Exception {
    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - entry :" + message);

    byte[] bb = message.toString().getBytes();
    DatagramPacket packet = new DatagramPacket(bb, bb.length, getRemoteHost(), getRemotePort());
    socket.send(packet);

    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - exit");
  }

  /**
   * Returns the remoteHost.
   * 
   * @return InetAddress
   */
  protected InetAddress getRemoteHost() {
    return remoteHost;
  }

}
