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
package com.isencia.message.extractor;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.EndOfDataException;

/**
 * An extractor that chops messages when the stream is closed. E.g. with
 * sockets: when the client drops the connection.
 * 
 * @author erwin
 */
public class StreamClosedMsgExtractor implements IMessageExtractor {

  private static final Logger logger = LoggerFactory.getLogger(StreamClosedMsgExtractor.class);
  private static int defaultBuffSize = 256;
  private StringBuffer buffer = null;
  private char prevChar;
  private Reader reader;

  public StreamClosedMsgExtractor() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageExtractor#getMessage()
   */
  public synchronized Object getMessage() {
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - entry");
    }
    if (!initialize()) {
      if (logger.isTraceEnabled()) {
        logger.trace("getMessage() - exit - not yet initialized! Returning null.");
      }
      return null;
    }

    while (true) {
      char c = '\uFFFF';
      try {
        c = readNextChar();
        prevChar = c;
        buffer.append(c);
      } catch (EndOfDataException e) {
        if (prevChar == '\uFFFF') {
          return null;
        } else
          break;
      } catch (Exception e) {
        return null;
      }
    }

    String message = buffer.toString();
    if (logger.isTraceEnabled()) {
      logger.trace("getMessage() - exit - result :" + message);
    }
    return message;

  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageExtractor#isOpen()
   */
  public boolean isOpen() {
    return reader != null;
  }

  /**
   * @return
   * @throws EndOfDataException
   * @throws Exception
   */
  private char readNextChar() throws EndOfDataException, Exception {
    try {
      int res = reader.read();

      if (res == -1) throw new EndOfDataException();

      return (char) res;
    } catch (EOFException e) {
      throw new EndOfDataException(e.getMessage());
    } catch (IOException e) {
      // hope it is because the channel has been closed, so same as
      // end of data !?
      throw new EndOfDataException(e.getMessage());
    } catch (NullPointerException e) {
      throw new Exception("No reader specified");
    }
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageExtractor#close()
   */
  public void close() {
    if (logger.isTraceEnabled()) {
      logger.trace("close() - entry");
    }

    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        logger.error("close() - Error closing reader", e);
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("close() - exit");
    }
  }

  /**
   * @return
   */
  private boolean initialize() {
    if (logger.isTraceEnabled()) {
      logger.trace("initialize() - entry");
    }
    // clear buffer
    buffer = new StringBuffer(defaultBuffSize);
    prevChar = '\uFFFF';

    if (logger.isTraceEnabled()) {
      logger.trace("initialize() - exit");
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageExtractor#open(java.io.Reader)
   */
  public synchronized void open(Reader reader) {
    if (logger.isTraceEnabled()) {
      logger.trace("open() - entry");
    }
    this.reader = reader;
    if (logger.isTraceEnabled()) {
      logger.trace("open() - exit");
    }
  }

  public IMessageExtractor cloneExtractor() {
    IMessageExtractor result = new StreamClosedMsgExtractor();
    return result;
  }

}