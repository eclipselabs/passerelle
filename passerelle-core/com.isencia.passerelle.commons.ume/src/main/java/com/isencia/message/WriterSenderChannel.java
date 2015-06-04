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
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.generator.IMessageGenerator;
import com.isencia.message.generator.MessageTextLineGenerator;

/**
 * WriterSenderChannel A sender channel implementation based on using a
 * java.io.Writer implementation.
 * 
 * @author erwin
 */
public class WriterSenderChannel extends SenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(WriterSenderChannel.class);

  private Writer writer = null;
  private IMessageGenerator generator = null;

  /**
   * Constructor for WriterSenderChannel.
   * 
   * @param generator
   */
  public WriterSenderChannel(IMessageGenerator generator) {
    super();

    if (generator == null) {
      setGenerator(new MessageTextLineGenerator());
    } else {
      setGenerator(generator);
    }
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.SenderChannel#doSendMessage(java.lang.Object)
   */
  protected void doSendMessage(Object message) throws Exception {
    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - entry - message :" + message);

    getGenerator().sendMessage(message);

    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannel#open()
   */
  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (!isOpen()) {
      super.open();
      getGenerator().open(getWriter());
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.ISenderChannel#close()
   */
  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    if (isOpen()) {
      super.close();
      getGenerator().close();
      if (getWriter() != null) {
        try {
          getWriter().close();
        } catch (IOException e) {
          throw new ChannelException(e.getMessage());
        }
      }
    }
    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  /**
   * Returns the generator.
   * 
   * @return IMessageGenerator
   */
  public IMessageGenerator getGenerator() {
    return generator;
  }

  /**
   * Sets the generator.
   * 
   * @param generator The generator to set
   */
  public void setGenerator(IMessageGenerator generator) {
    if (logger.isTraceEnabled()) logger.trace("setGenerator() - entry - generator :" + generator);
    this.generator = generator;
    if (logger.isTraceEnabled()) logger.trace("setGenerator() - exit");
  }

  /**
   * Gets the writer.
   * 
   * @return Returns a Writer
   */
  public Writer getWriter() {
    return writer;
  }

  /**
   * Sets the writer.
   * 
   * @param writer The writer to set
   */
  public void setWriter(Writer writer) {
    if (logger.isTraceEnabled()) logger.trace("setWriter() - entry - writer :" + writer);
    this.writer = writer;
    if (logger.isTraceEnabled()) logger.trace("setWriter() - exit");
  }

}
