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
package com.isencia.passerelle.actor.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.interceptor.IMessageInterceptor;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * @author dirk To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates.
 */
public class MessageToMailMessageConverter implements IMessageInterceptor {

  private final static Logger LOGGER = LoggerFactory.getLogger(MessageToMailMessageConverter.class);

  private boolean passThrough = false;

  /**
   * Constructor for MessageToMailMessageConverter.
   */
  public MessageToMailMessageConverter(boolean passThrough) {
    super();

    this.passThrough = passThrough;
  }

  public Object accept(Object message) throws Exception {
    if (LOGGER.isTraceEnabled()) LOGGER.trace("Accepting ~" + message + "~");

    ManagedMessage managedMsg = (ManagedMessage) message;

    // Setup the session
    LOGGER.debug("Getting Mailhost");
    Properties properties = new Properties();
    String[] mailhost = managedMsg.getBodyHeader(SMTPSender.MAILHOST_HEADER);
    if (mailhost != null && mailhost.length > 0 && mailhost[0].length() > 0) {
      LOGGER.debug("Mailhost : " + mailhost[0]);
      properties.put("mail.host", mailhost[0]);
    }
    Session session = Session.getDefaultInstance(properties, null);

    Message mailMessage = new MimeMessage(session);

    // Set from address
    LOGGER.debug("Getting from addresses");
    String[] from = managedMsg.getBodyHeader(SMTPSender.FROM_HEADER);
    if (from != null && from.length > 0 && from[0].length() > 0) {
      LOGGER.debug("From : " + from[0]);
      mailMessage.setFrom(new InternetAddress(from[0]));
    }

    // Set to addresses
    LOGGER.debug("Getting to addresses");
    String[] toRecipients = managedMsg.getBodyHeader(SMTPSender.TO_HEADER);
    if (toRecipients != null && toRecipients.length > 0) {
      LOGGER.debug("To addresses count : " + toRecipients.length);
      List<InternetAddress> addresses = new ArrayList<InternetAddress>();
      for (int i = 0; i < toRecipients.length; i++) {
        String[] individualToRecipients = toRecipients[i].split(",");
        for (String individualRecipient : individualToRecipients) {
          if (individualRecipient.trim().length() > 0) {
            LOGGER.debug("Send to : " + individualRecipient.trim());
            addresses.add(new InternetAddress(individualRecipient.trim()));
          }
        }
      }
      for (InternetAddress rec : addresses) {
        mailMessage.addRecipient(javax.mail.Message.RecipientType.TO, rec);
      }
    }

    // Set cc addresses
    LOGGER.debug("Getting cc addresses");
    String[] ccRecipients = managedMsg.getBodyHeader(SMTPSender.CC_HEADER);
    if (ccRecipients != null && ccRecipients.length > 0) {
      LOGGER.debug("Cc addresses count : " + ccRecipients.length);
      List<InternetAddress> addresses = new ArrayList<InternetAddress>();
      for (int i = 0; i < ccRecipients.length; i++) {
        String[] individualCcRecipients = ccRecipients[i].split(",");
        for (String individualCcRecipient : individualCcRecipients) {
          if (individualCcRecipient.trim().length() > 0) {
            LOGGER.debug("Send cc : " + individualCcRecipient.trim());
            addresses.add(new InternetAddress(individualCcRecipient.trim()));
          }
        }
      }
      for (InternetAddress rec : addresses) {
        mailMessage.addRecipient(javax.mail.Message.RecipientType.CC, rec);
      }
    }

    // Set bcc addresses
    LOGGER.debug("Getting bcc addresses");
    String[] bccRecipients = managedMsg.getBodyHeader(SMTPSender.BCC_HEADER);
    if (bccRecipients != null && bccRecipients.length > 0) {
      LOGGER.debug("Bcc addresses count : " + bccRecipients.length);
      List<InternetAddress> addresses = new ArrayList<InternetAddress>();
      for (int i = 0; i < bccRecipients.length; i++) {
        String[] individualBccRecipients = bccRecipients[i].split(",");
        for (String individualBccRecipient : individualBccRecipients) {
          if (individualBccRecipient.trim().length() > 0) {
            LOGGER.debug("Send bcc : " + individualBccRecipient.trim());
            addresses.add(new InternetAddress(individualBccRecipient.trim()));            
          }
        }
      }
      for (InternetAddress rec : addresses) {
        mailMessage.addRecipient(javax.mail.Message.RecipientType.BCC, rec);
      }
    }

    // Set Subject
    LOGGER.debug("Getting subject");
    String[] subject = managedMsg.getBodyHeader(SMTPSender.SUBJECT_HEADER);
    if (subject != null && subject.length > 0 && subject[0].length() > 0) {
      LOGGER.debug("Set Subject : " + subject[0]);
      mailMessage.setSubject(subject[0]);
    }

    if (passThrough) {
      mailMessage.setText(managedMsg.toString());
    } else {
      Object content = managedMsg.getBodyContent();
      if (content instanceof Multipart) {
        mailMessage.setContent((MimeMultipart) content);
      } else if (content instanceof String) {
        String[] contentType = managedMsg.getBodyHeader("Content-Type");
        if (contentType == null || contentType.length == 0)
          mailMessage.setText((String) content);
        else
          mailMessage.setContent(content, contentType[0]);
      }

    }
    mailMessage.saveChanges();
    return mailMessage;
  }

}
