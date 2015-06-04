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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.WriterSenderChannel;
import com.isencia.message.generator.IMessageGenerator;

/**
 * SocketClientSenderChannel TODO: class comment
 * 
 * @author erwin
 */
public class SocketClientSenderChannel extends WriterSenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(SocketClientSenderChannel.class);

  private final static int retryCountMax = 10;
  private final static int retryWaitMin = 1 * 1000; // 1 second
  private final static int retryWaitMax = 10 * 1000; // 10 seconds

  private Socket socket = null;
  private String host = null;
  private int port = -1;
  private int retryWaitStep = (retryWaitMax - retryWaitMin) / retryCountMax;

  /**
   * @param host
   * @param port
   * @param generator
   */
  public SocketClientSenderChannel(String host, int port, IMessageGenerator generator) {
    super(generator);
    this.host = host;
    this.port = port;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (getSocket() != null) {
      try {
        getSocket().close();
      } catch (IOException e) {
        logger.error("open() - Could not close socket", e);
      }
      setSocket(null);
    }
    if (getWriter() != null) {
      try {
        getWriter().close();
      } catch (IOException e) {
        logger.error("open() - Could not close reader", e);
      }
      setWriter(null);
    }

    InetAddress addr = null;
    try {
      addr = InetAddress.getByName(getHost());
    } catch (UnknownHostException e) {
      throw new ChannelException("Unkown host :" + getHost());
    }
    Socket s = null;
    int retryCount = 0;
    while (retryCount < retryCountMax && s == null) {
      try {
        s = new Socket(addr, getPort());
      } catch (ConnectException e) {
        // maybe the server is still starting,
        // so keep on retrying a couple of times
        long waitTime = retryWaitMin + retryWaitStep * retryCount;
        retryCount++;
        logger.info("open() - Socket connection to " + getHost() + ":" + getPort() + " failed." + "\nRetry count " + retryCount + "/" + retryCountMax);
        synchronized (this) {
          try {
            wait(waitTime);
          } catch (InterruptedException ex) {
            break;
          }
        }
      } catch (IOException e) {
        // more serious problem, log it and stop trying
        logger.error("open() - ", e);
        break;
      }
    }
    if (s != null) {
      Writer wrt = null;
      try {
        wrt = new OutputStreamWriter(s.getOutputStream());
      } catch (IOException e) {
        try {
          s.close();
        } catch (IOException ex) {
        }
        throw new ChannelException("Could not open writer for socket to " + getHost() + ":" + getPort());
      }
      setSocket(s);
      setWriter(wrt);
      super.open();
    } else {
      throw new ChannelException("Could not open socket to " + getHost() + ":" + getPort());
    }

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

  public void close() throws ChannelException {
    logger.trace("close() - entry");
    try {
      if (getSocket() != null) getSocket().close();
    } catch (IOException ex) {
      logger.error("close() - Error closing socket", ex);
    }

    super.close();

    logger.trace("close() - exit");
  }

  /**
   * Sets the socket.
   * 
   * @param socket The socket to set
   */
  public void setSocket(Socket socket) {
    this.socket = socket;
    setHost(socket.getInetAddress().getHostName());
    setPort(socket.getPort());
  }

  /**
   * Gets the host.
   * 
   * @return Returns a String
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the host.
   * 
   * @param host The host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Gets the port.
   * 
   * @return Returns a int
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port.
   * 
   * @param port The port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

}
