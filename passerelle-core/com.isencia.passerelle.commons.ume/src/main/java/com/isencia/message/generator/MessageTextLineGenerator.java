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
package com.isencia.message.generator;

import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message generator that delineates each outgoing message by an ending
 * line-separator according to the JVM platform.
 * 
 * @author erwin
 */
public class MessageTextLineGenerator implements IMessageGenerator {
  private static final Logger logger = LoggerFactory.getLogger(MessageTextLineGenerator.class);

  private String lineSeparator = System.getProperty("line.separator");

  private Writer writer;

  /**
   * Constructor for MessageTextLineGenerator.
   */
  public MessageTextLineGenerator() {
    super();
  }

  public MessageTextLineGenerator(String lineSeparator) {
    super();
    this.lineSeparator = lineSeparator;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageGenerator#open(java.io.Writer)
   */
  public void open(Writer writer) {
    this.writer = writer;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageGenerator#isOpen()
   */
  public boolean isOpen() {
    return writer != null;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageGenerator#close()
   */
  public void close() {
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.message.IMessageGenerator#sendMessage(java.lang.Object)
   */
  public void sendMessage(Object message) throws Exception {
    if (logger.isTraceEnabled()) logger.trace("sendMessage() - entry - msg :\n" + message);

    String res = message.toString() + lineSeparator;
    writer.write(res);
    writer.flush();

    if (logger.isTraceEnabled()) logger.trace("sendMessage() - exit - Wrote msg :\n" + res);
  }

  public IMessageGenerator cloneGenerator() {
    IMessageGenerator result = new MessageTextLineGenerator(lineSeparator);
    return result;
  }

}
