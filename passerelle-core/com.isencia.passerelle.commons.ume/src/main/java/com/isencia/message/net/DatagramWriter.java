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

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.io.CharToByteConverter;
import sun.io.ConversionBufferFullException;

/**
 * Writer implementation wrapping a DatagramSocket.send(). Based on char-to-byte
 * conversion code as in java.io.OutputStreamWriter.
 * 
 * @author erwin
 */
@SuppressWarnings("deprecation")
public class DatagramWriter extends Writer {
  private final static Logger logger = LoggerFactory.getLogger(DatagramWriter.class);

  private DatagramSocket socket = null;
  private CharToByteConverter ctb;

  private static final int defaultByteBufferSize = 8192;
  /* bb is a temporary output buffer into which bytes are written. */
  private byte bb[];
  /* nextByte is where the next byte will be written into bb */
  private int nextByte = 0;
  /* nBytes is the buffer size = defaultByteBufferSize in this class */
  private int nBytes = 0;

  private InetAddress remoteHost = null;
  private int remotePort = -1;

  public DatagramWriter(DatagramSocket socket) {
    this(socket, CharToByteConverter.getDefault());
  }

  public DatagramWriter(DatagramSocket socket, String enc) throws UnsupportedEncodingException {
    this(socket, CharToByteConverter.getConverter(enc));
  }

  public DatagramWriter(DatagramSocket socket, CharToByteConverter ctb) {
    super(socket);
    if (socket == null) throw new NullPointerException("socket is null");
    this.socket = socket;
    this.ctb = ctb;
    bb = new byte[defaultByteBufferSize];
    nBytes = defaultByteBufferSize;
  }

  /**
   * Returns the canonical name of the character encoding being used by this
   * writer. If this <code>DatagramWriter</code> was created with the
   * {@link #DatagramWriter(DatagramSocket, String)} constructor then the
   * returned encoding name, being canonical, may differ from the encoding name
   * passed to the constructor. May return <code>null</code> if the socket has
   * been closed.
   * 
   * @return a String representing the encoding name, or possibly
   *         <code>null</code> if the socket has been closed
   * @see <a href="../lang/package-summary.html#charenc">Character encodings</a>
   */
  public String getEncoding() {
    synchronized (lock) {
      if (ctb != null)
        return ctb.getCharacterEncoding();
      else
        return null;
    }
  }

  /** Check to make sure that the socket has not been closed */
  private void ensureOpen() throws IOException {
    if (socket == null) throw new IOException("Socket closed");
  }

  /**
   * Write a single character.
   * 
   * @exception IOException If an I/O error occurs
   */
  public void write(int c) throws IOException {
    char cbuf[] = new char[1];
    cbuf[0] = (char) c;
    write(cbuf, 0, 1);
  }

  /**
   * Write a portion of an array of characters.
   * 
   * @param cbuf Buffer of characters
   * @param off Offset from which to start writing characters
   * @param len Number of characters to write
   * @exception IOException If an I/O error occurs
   */
  public void write(char cbuf[], int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return;
      }
      int ci = off, end = off + len;
      boolean bufferFlushed = false;
      while (ci < end) {
        boolean bufferFull = false;
        try {
          nextByte += ctb.convertAny(cbuf, ci, end, bb, nextByte, nBytes);
          ci = end;
        } catch (ConversionBufferFullException x) {
          int nci = ctb.nextCharIndex();
          if ((nci == ci) && bufferFlushed) {
            /*
             * If the buffer has been flushed and it still does not hold even
             * one character
             */
            throw new CharConversionException("Output buffer too small");
          }
          ci = nci;
          bufferFull = true;
          nextByte = ctb.nextByteIndex();
        }
        if ((nextByte >= nBytes) || bufferFull) {
          writeNext(bb, 0, nextByte);
          nextByte = 0;
          bufferFlushed = true;
        }
      }
    }
  }

  /**
   * Write a portion of a string.
   * 
   * @param str A String
   * @param off Offset from which to start writing characters
   * @param len Number of characters to write
   * @exception IOException If an I/O error occurs
   */
  public void write(String str, int off, int len) throws IOException {
    /* Check the len before creating a char buffer */
    if (len < 0) throw new IndexOutOfBoundsException();

    char cbuf[] = new char[len];
    str.getChars(off, off + len, cbuf, 0);
    write(cbuf, 0, len);
  }

  /**
   * Flush the output buffer to the underlying byte stream, without flushing the
   * byte stream itself. This method is non-private only so that it may be
   * invoked by PrintStream.
   */
  void flushBuffer() throws IOException {
    synchronized (lock) {
      ensureOpen();

      for (;;) {
        try {
          nextByte += ctb.flushAny(bb, nextByte, nBytes);
        } catch (ConversionBufferFullException x) {
          nextByte = ctb.nextByteIndex();
        }
        if (nextByte == 0) break;
        if (nextByte > 0) {
          writeNext(bb, 0, nextByte);
          nextByte = 0;
        }
      }
    }
  }

  /**
   * Flush the datagram.
   * 
   * @exception IOException If an I/O error occurs
   */
  public void flush() throws IOException {
    synchronized (lock) {
      flushBuffer();
    }
  }

  /**
   * Close the socket.
   * 
   * @exception IOException If an I/O error occurs
   */
  public void close() throws IOException {
    synchronized (lock) {
      if (socket == null) return;
      flush();
      socket.close();
      socket = null;
      bb = null;
      ctb = null;
    }
  }

  protected void writeNext(byte[] bb, int off, int len) throws IOException {
    if (remoteHost == null || remotePort < 0) {
      throw new IOException("Remote host (" + remoteHost + ") and/or port (" + remotePort + ") not set correctly");
    }
    DatagramPacket packet = new DatagramPacket(bb, off, len, remoteHost, remotePort);
    logger.trace("Sending packet\n" + packet + "\nto socket\n" + socket);
    socket.send(packet);
  }

  /**
   * Gets the remoteHost.
   * 
   * @return Returns a InetAddress
   */
  public InetAddress getRemoteHost() {
    return remoteHost;
  }

  /**
   * Sets the remoteHost.
   * 
   * @param remoteHost The remoteHost to set
   */
  public void setRemoteHost(InetAddress remoteHost) {
    this.remoteHost = remoteHost;
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
  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

}