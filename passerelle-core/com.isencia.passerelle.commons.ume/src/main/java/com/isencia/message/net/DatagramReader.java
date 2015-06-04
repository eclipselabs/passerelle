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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.io.ByteToCharConverter;
import sun.io.ConversionBufferFullException;

/**
 * DatagramReader TODO: class comment
 * 
 * @author erwin
 */
@SuppressWarnings("deprecation")
public class DatagramReader extends Reader {

  private final static Logger logger = LoggerFactory.getLogger(DatagramReader.class);

  private ByteToCharConverter btc;

  private static final int defaultByteBufferSize = 8192;
  private byte bb[]; /*
                      * Input buffer
                      */

  private DatagramSocket socket = null;

  public DatagramReader(DatagramSocket socket) {
    this(socket, ByteToCharConverter.getDefault());
  }

  public DatagramReader(DatagramSocket socket, String enc) throws UnsupportedEncodingException {
    this(socket, ByteToCharConverter.getConverter(enc));
  }

  public DatagramReader(DatagramSocket socket, ByteToCharConverter btc) {
    super(socket);
    if (socket == null) throw new NullPointerException("socket is null");
    this.socket = socket;
    this.btc = btc;
    bb = new byte[defaultByteBufferSize];
  }

  /**
   * @param bb
   * @return
   * @throws IOException
   */
  protected int readNext(byte[] bb) throws IOException {
    DatagramPacket packet = new DatagramPacket(bb, bb.length);
    socket.receive(packet);

    logger.debug("readNext() - Received datagram packet:\n" + packet.getData());
    return packet.getLength();
  }

  /**
   * Returns the canonical name of the character encoding being used by this
   * reader. If this <code>DatagramReader</code> was created with the
   * {@link #DatagramReader(DatagramSocket, String)} constructor then the
   * returned encoding name, being canonical, may differ from the encoding name
   * passed to the constructor. May return <code>null</code> if the stream has
   * been closed.
   * 
   * @return a String representing the encoding name, or possibly
   *         <code>null</code> if the socket has been closed
   * @see <a href="../lang/package-summary.html#charenc">Character encodings</a>
   */
  public String getEncoding() {
    synchronized (lock) {
      if (btc != null)
        return btc.getCharacterEncoding();
      else
        return null;
    }
  }

  /* Buffer handling */

  private int nBytes = 0; /* -1 implies EOF has been reached */
  private int nextByte = 0;

  private void malfunction() {
    throw new InternalError("Converter malfunction (" + btc.getCharacterEncoding() + ")");
  }

  private int convertInto(char cbuf[], int off, int end) throws IOException {
    int nc = 0;
    if (nextByte < nBytes) {
      try {
        nc = btc.convert(bb, nextByte, nBytes, cbuf, off, end);
        nextByte = nBytes;
        if (btc.nextByteIndex() != nextByte) malfunction();
      } catch (ConversionBufferFullException x) {
        nextByte = btc.nextByteIndex();
        nc = btc.nextCharIndex() - off;
      }
    }
    return nc;
  }

  private int flushInto(char cbuf[], int off, int end) throws IOException {
    int nc = 0;
    try {
      nc = btc.flush(cbuf, off, end);
    } catch (ConversionBufferFullException x) {
      nc = btc.nextCharIndex() - off;
    }
    return nc;
  }

  private int fill(char cbuf[], int off, int end) throws IOException {
    int nc = 0;

    if (nextByte < nBytes) nc = convertInto(cbuf, off, end);

    // allow one extra place to put line feed
    // each datagram packet should be recognized
    // by a BufferedReader.readLine() as a separate line.
    while (off + nc < end - 1) {

      if (nBytes != -1) {
        if ((nc > 0) && !inReady()) break; /* Block at most once */
        nBytes = readNext(bb);
      }

      if (nBytes == -1) {
        nBytes = 0; /* Allow file to grow */
        nc += flushInto(cbuf, off + nc, end);
        if (nc == 0)
          return -1;
        else
          break;
      } else {
        nextByte = 0;
        nc += convertInto(cbuf, off + nc, end);
      }
    }
    cbuf[off + (nc++)] = '\n';
    return nc;
  }

  /**
   * Tell whether the underlying byte stream is ready to be read. Return false
   * for those streams that do not support available(), such as the Win32
   * console stream.
   */
  private boolean inReady() {
    return false;
  }

  /** Check to make sure that the stream has not been closed */
  private void ensureOpen() throws IOException {
    if (socket == null) throw new IOException("Socket closed");
  }

  /**
   * Read a single character.
   * 
   * @return The character read, or -1 if the end of the stream has been reached
   * @exception IOException If an I/O error occurs
   */
  public int read() throws IOException {
    char cb[] = new char[1];
    if (read(cb, 0, 1) == -1)
      return -1;
    else
      return cb[0];
  }

  /**
   * Read characters into a portion of an array.
   * 
   * @param cbuf Destination buffer
   * @param off Offset at which to start storing characters
   * @param len Maximum number of characters to read
   * @return The number of characters read, or -1 if the end of the stream has
   *         been reached
   * @exception IOException If an I/O error occurs
   */
  public int read(char cbuf[], int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }
      return fill(cbuf, off, off + len);
    }
  }

  /**
   * Tell whether this stream is ready to be read. An InputStreamReader is ready
   * if its input buffer is not empty.
   * 
   * @exception IOException If an I/O error occurs
   */
  public boolean ready() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return (nextByte < nBytes) || inReady();
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
      socket.close();
      socket = null;
      bb = null;
      btc = null;
    }
  }

}