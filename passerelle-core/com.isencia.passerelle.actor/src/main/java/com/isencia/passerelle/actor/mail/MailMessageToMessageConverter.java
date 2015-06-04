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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.interceptor.IMessageInterceptor;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.PasserelleBodyPart;

/**
 * @author dirk
 */
public class MailMessageToMessageConverter implements IMessageInterceptor {

  private final static Logger LOGGER = LoggerFactory.getLogger(MailMessageToMessageConverter.class);

  private String[] contentTypes = null;
  private boolean attachInMessage = false;

  public Object accept(Object message) throws Exception {
    LOGGER.trace("Accepting ~" + message.toString() + "~");
    ManagedMessage container = null;
    if (message != null) {
      container = MessageFactory.getInstance().createMessage();
      MimeMessage mailMessage = (MimeMessage) message;

      @SuppressWarnings("unchecked")
      Enumeration<Header> hdrEnum = mailMessage.getAllHeaders();
      while (hdrEnum.hasMoreElements()) {
        Header header = hdrEnum.nextElement();
        LOGGER.debug("Header {} = {}", header.getName(), header.getValue());
        container.addBodyHeader(header.getName(), header.getValue());
      }

      // return immediately if only header info is required
      if (contentTypes == null && !attachInMessage)
        return container;

      Object content = mailMessage.getContent();

      if (content instanceof String) {
        if (contentTypes != null) {
          if (MessageHelper.filterContent(mailMessage, contentTypes)) {
            container.setBodyContentPlainText((String) content);
          }
        }
      } else if (content instanceof Multipart) {
        MultipartContentBuilder builder = new MultipartContentBuilder();
        Multipart mp = builder.build((Multipart) content, contentTypes, attachInMessage);
        if (mp != null) {
          ((MessageContainer) container).setBodyContent(mp);
        }
      } else if (content instanceof InputStream)
        LOGGER.debug("Content is Inputstream");
    }
    return container;
  }

  private class MultipartContentBuilder {
    public Multipart build(Multipart mp, String[] contentTypeFilter, boolean attachments) throws IOException, MessagingException {
      Multipart newMp = new MimeMultipart();
      int count = mp.getCount();
      LOGGER.debug("Building Multipart, found " + count + " parts");
      for (int i = 0; i < count; i++) {
        BodyPart body = mp.getBodyPart(i);
        BodyPartContentBuilder bodyPartBuilder = new BodyPartContentBuilder();
        Part part = bodyPartBuilder.build(body, contentTypeFilter, attachments);
        if (part != null) {
          // cast from Part to BodyPart iso PasserelleBodyPart
          newMp.addBodyPart((BodyPart) part);
        }
      }
      // If no bodyparts where added, just return null
      if (newMp.getCount() == 0)
        return null;
      return newMp;
    }
  }

  private class BodyPartContentBuilder {
    public Part build(Part part, String[] contentTypeFilter, boolean attachments) throws IOException, MessagingException {
      Object content = part.getContent();
      if (content instanceof String) {
        boolean isAttach = !MessageHelper.isContent(part);
        if (isAttach) {
          if (!attachments)
            return null;
          else {
            return part;
          }
        }
        if (!MessageHelper.filterContent(part, contentTypeFilter)) {
          LOGGER.debug("Not a supported type");
          return null;
        }
        return part;
      } else if (content instanceof InputStream) {
        boolean isAttach = !MessageHelper.isContent(part);
        if (isAttach) {
          if (!attachments)
            return null;
          else {
            PasserelleBodyPart newPart = new PasserelleBodyPart();
            newPart.setText("");
            MessageHelper.copyHeaders(part, newPart);
            newPart.saveChanges();
            return newPart;
          }
        }
        if (!MessageHelper.filterContent(part, contentTypeFilter)) {
          return null;
        }
      } else if (content instanceof Multipart) {
        MultipartContentBuilder builder = new MultipartContentBuilder();
        Multipart mp = builder.build((Multipart) content, contentTypeFilter, attachments);
        if (mp != null && mp.getCount() > 0) {
          PasserelleBodyPart newPart = new PasserelleBodyPart();
          newPart.setText("");
          MessageHelper.copyHeaders(part, newPart);
          newPart.setContent(mp);
          newPart.saveChanges();
          return newPart;
        }
      }
      return null;
    }
  }

  /**
   * Returns the contentTypes.
   * 
   * @return String[]
   */
  public String[] getContentTypes() {
    return contentTypes;
  }

  /**
   * Sets the contentTypes.
   * 
   * @param contentTypes
   *          The contentTypes to set
   */
  public void setContentTypes(String[] contentTypes) {
    this.contentTypes = contentTypes;
  }

  /**
   * Returns the attachInMessage.
   * 
   * @return boolean
   */
  public boolean isAttachInMessage() {
    return attachInMessage;
  }

  /**
   * Sets the attachInMessage.
   * 
   * @param attachInMessage
   *          The attachInMessage to set
   */
  public void setAttachInMessage(boolean attachInMessage) {
    this.attachInMessage = attachInMessage;
  }

}
