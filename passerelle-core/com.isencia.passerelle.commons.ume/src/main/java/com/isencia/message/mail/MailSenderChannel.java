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
package com.isencia.message.mail;

import javax.mail.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.SenderChannel;

/**
 * MailSenderChannel TODO: class comment
 * 
 * @author dirk
 */
public class MailSenderChannel extends SenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(MailSenderChannel.class);

  /**
   * Constructor for MailSenderChannel.
   * 
   * @param generator
   */
  public MailSenderChannel() {
    super();
  }

  protected void doSendMessage(Object message) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - entry : <" + message + ">");

    try {
      Transport.send((javax.mail.Message) message);
    } catch (Exception e) {
      throw new ChannelException(e.getMessage());
    }

    if (logger.isTraceEnabled()) logger.trace("doSendMessage() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    if (isOpen()) {
      super.close();
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (!isOpen()) {
      super.open();
    }

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

}
