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

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReceiverChannel;

/**
 * MailReceiverChannel TODO: class comment
 * 
 * @author dirk
 */
public class MailReceiverChannel extends ReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(MailReceiverChannel.class);

  private URLName server = null;
  private Folder folder = null;
  private Message[] messages = null;
  private int messageOffset = 0;
  private int messageCount = 0;

  private boolean messageCountSend = false;

  private int messageType = MESSAGES;

  public final static int MESSAGES = 1;
  public final static int MESSAGE_COUNT = 2;
  public final static int NEW_MESSAGE_COUNT = 3;
  public final static int UNREAD_MESSAGE_COUNT = 4;

  /**
   * @param server
   */
  public MailReceiverChannel(URLName server) {
    super();

    this.server = server;
  }

  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    messageCountSend = false;

    if (server == null) throw new ChannelException("Server not specified");

    try {
      Session session = Session.getDefaultInstance(new Properties(), null);
      folder = session.getFolder(server);
      if (folder == null) {
        logger.debug("open() - Folder : " + server.getFile() + "not found");
        return;
      }

      folder.open(Folder.READ_ONLY);
      messageOffset = 0;
      switch (messageType) {
      case MESSAGES:
        messages = folder.getMessages();
        logger.debug("open() - " + messages.length + " messages found");
        break;
      case MESSAGE_COUNT:
        messageCount = folder.getMessageCount();
        logger.debug("open() - " + messageCount + " messages found");
        break;
      case NEW_MESSAGE_COUNT:
        messageCount = folder.getNewMessageCount();
        logger.debug("open() - " + messageCount + " messages found");
        break;
      case UNREAD_MESSAGE_COUNT:
        messageCount = folder.getUnreadMessageCount();
        logger.debug("open() - " + messageCount + " messages found");
        break;

      default:
        break;
      }
    } catch (MessagingException e) {
      throw new ChannelException(e.toString());
    }

    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  public void close() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("close() - entry");

    super.close();

    try {
      logger.debug("Closing Channel");
      if (folder != null && folder.isOpen()) logger.debug("Closing Folder");
      folder.close(false);
      folder = null;
    } catch (MessagingException e) {
      logger.error(e.getMessage());
    }

    if (logger.isTraceEnabled()) logger.trace("close() - exit");
  }

  protected Object doGetMessage() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("doGetMessage() - entry");

    Object msg = null;
    try {
      switch (messageType) {
      case MESSAGES:
        if (messageOffset < messages.length) {
          msg = messages[messageOffset++];
        }
        break;
      case MESSAGE_COUNT:
      case NEW_MESSAGE_COUNT:
      case UNREAD_MESSAGE_COUNT:
        if (messageCountSend) break;
        msg = Integer.toString(messageCount);
        messageCountSend = true;
        break;

      default:
        break;
      }

      if (logger.isTraceEnabled()) logger.trace("doGetMessage() - exit : " + msg);
      return msg;
    } catch (Exception e) {
      throw new ChannelException(e.getMessage());
    }
  }

  /**
   * Returns the messageType.
   * 
   * @return int
   */
  public int getMessageType() {
    return messageType;
  }

  /**
   * Sets the messageType.
   * 
   * @param messageType The messageType to set
   */
  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

}