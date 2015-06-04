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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReceiverChannel;

/**
 * MulticastReceiverChannel TODO: class comment
 * 
 * @author erwin
 */
public class MulticastReceiverChannel extends ReceiverChannel {

  private final static Logger logger = LoggerFactory.getLogger(MulticastReceiverChannel.class);

  private static final int defaultByteBufferSize = 8192;
  private MulticastSocket socket = null;
  private int port = -1;
  private String groupName = "";
  private InetAddress group = null;

  /**
   * @param socket
   * @throws ChannelException
   */
  public MulticastReceiverChannel(MulticastSocket socket) throws ChannelException {
    super();
    if (socket != null) {
      this.socket = socket;
      setPort(socket.getLocalPort());
    }
  }

  /**
   * @param group
   * @param port
   * @throws ChannelException
   */
  public MulticastReceiverChannel(String group, int port) throws ChannelException {
    super();
    setPort(port);
    setGroupName(group);
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (socket == null) {
      try {
        socket = new MulticastSocket(getPort());
        socket.joinGroup(getGroup());
      } catch (UnknownHostException e) {
        logger.error("open() - Unknown multicast group " + getGroup(), e);
        throw new ChannelException("Unknown multicast group " + getGroup());
      } catch (IOException e) {
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
  public MulticastSocket getSocket() {
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

  /**
   * Gets the group.
   * 
   * @return Returns a String
   */
  public String getGroupName() {
    return groupName;
  }

  public void close() throws ChannelException {
    logger.trace("close() - entry");
    try {
      MulticastSocket socket = (MulticastSocket) getSocket();
      if (socket != null) {
        socket.leaveGroup(getGroup());
      }
      super.close();
    } catch (IOException ex) {
      logger.error("close() - Error closing sockets", ex);
    }
    logger.trace("close() - exit");
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

  /**
   * Sets the group.
   * 
   * @param group The group to set
   */
  public void setGroupName(String group) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("setGroupName() - entry :" + group);

    try {
      this.group = InetAddress.getByName(group);
    } catch (UnknownHostException e) {
      throw new ChannelException("Unknown host " + group);
    }

    this.groupName = group;

    if (logger.isTraceEnabled()) logger.trace("setGroupName() - exit");
  }

  /**
   * Sets the port.
   * 
   * @param port The port to set
   */
  public void setPort(int port) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("setPort() - entry :" + port);

    if (port < 0) {
      throw new ChannelException("Port (" + port + ") not set correctly");
    }

    this.port = port;

    if (logger.isTraceEnabled()) logger.trace("setPort() - exit");
  }

  /**
   * Returns the group.
   * 
   * @return InetAddress
   */
  public InetAddress getGroup() {
    return group;
  }

}
