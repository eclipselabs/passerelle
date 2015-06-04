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
import com.isencia.message.DefaultSenderChannelHandler;
import com.isencia.message.ISenderChannelHandler;
import com.isencia.message.SenderChannel;

/**
 * MulticastSenderChannel TODO: class comment
 * 
 * @author erwin
 */
public class MulticastSenderChannel extends SenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(MulticastSenderChannel.class);

  private MulticastSocket socket = null;
  private int port = -1;
  private String groupName = "";
  private InetAddress group = null;
  private ISenderChannelHandler handler = null;

  /**
   * @param socket
   * @throws ChannelException
   */
  public MulticastSenderChannel(MulticastSocket socket) throws ChannelException {
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
  public MulticastSenderChannel(String group, int port) throws ChannelException {
    super();
    setPort(port);
    setGroupName(group);
    handler = new DefaultSenderChannelHandler(this);
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (socket == null) {
      try {
        socket = new MulticastSocket(getPort());
      } catch (IOException e) {
        logger.error("open() - Socket construction failed on port " + port, e);
        throw new ChannelException("Socket construction failed on port " + port);
      }
    }
    if (!isOpen()) {
      super.open();
      handler.open();
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    if (isOpen()) {
      super.close();
      handler.close();
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
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
   * Gets the port on which this socket will publish for datagrams.
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
  protected InetAddress getGroup() {
    return group;
  }

  protected void doSendMessage(Object message) throws Exception {
    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - entry :" + message);

    byte[] bb = message.toString().getBytes();
    DatagramPacket packet = new DatagramPacket(bb, bb.length, getGroup(), getPort());
    socket.send(packet);

    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - exit");
  }

}
