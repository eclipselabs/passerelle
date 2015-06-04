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
package com.isencia.message;

import java.io.IOException;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.extractor.IMessageExtractor;
import com.isencia.message.extractor.TextLineMessageExtractor;

/**
 * ReaderReceiverChannel An implementation of a receiver channel based on using
 * a java.io.Reader implementation.
 * 
 * @author erwin
 */
public class ReaderReceiverChannel extends ReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(ReaderReceiverChannel.class);

  private IMessageExtractor extractor = null;
  private Reader reader;

  /**
   * Constructor for ReaderReceiverChannel.
   * 
   * @param extractor
   */
  public ReaderReceiverChannel(IMessageExtractor extractor) {
    super();

    if (extractor == null) {
      setExtractor(new TextLineMessageExtractor());
    } else {
      setExtractor(extractor);
    }
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ReceiverChannel#doGetMessage()
   */
  protected Object doGetMessage() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - entry");
    Object msg = null;

    try {
      msg = getExtractor().getMessage();
    } catch (Exception e) {
      throw new ChannelException(e.toString());
    }

    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - exit - Return msg : " + msg);

    return msg;
  }

  /**
   * Gets the reader.
   * 
   * @return Returns a Reader
   */
  public Reader getReader() {
    return reader;
  }

  /**
   * Sets the reader.
   * 
   * @param reader The reader to set
   */
  public void setReader(Reader reader) {
    if (logger.isTraceEnabled()) logger.trace("setReader() - entry - reader :" + reader);
    this.reader = reader;
    if (logger.isTraceEnabled()) logger.trace("setReader() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IReceiverChannel#close()
   */
  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    super.close();

    getExtractor().close();

    if (getReader() != null) {
      try {
        getReader().close();
      } catch (IOException e) {
        throw new ChannelException(e.getMessage());
      }
    }
    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IReceiverChannel#open()
   */
  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");
    super.open();
    getExtractor().open(getReader());
    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /**
   * Returns the extractor.
   * 
   * @return IMessageExtractor
   */
  public IMessageExtractor getExtractor() {
    return extractor;
  }

  /**
   * Sets the extractor.
   * 
   * @param extractor The extractor to set
   */
  public void setExtractor(IMessageExtractor extractor) {
    if (logger.isTraceEnabled()) logger.trace("setExtractor() - entry - extractor :" + extractor);
    this.extractor = extractor;
    if (logger.isTraceEnabled()) logger.trace("setExtractor() - exit");
  }

}
