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
package com.isencia.passerelle.message.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.PasserelleBodyPart;
import com.isencia.passerelle.message.internal.SettableMessage;

/**
 * @author erwin
 */
class MessageBuilder {
  private static Logger logger = LoggerFactory.getLogger(MessageBuilder.class);

  private final static String rootTag = "Message";
  private final static String bodyTag = "Body";
  private final static String partTag = "Part";
  private final static String partsTag = "Parts";
  private final static String headerTag = "Header";
  private final static String contentTypeTag = "Content-Type";

  /**
   * Converts a message in an XML representation
   * 
   * @throws MessagingException
   * @throws IOException
   */
  public static String buildToXML(ManagedMessage message) throws MessageException {

    Element msgRoot = new Element(rootTag);
    Document doc = new Document(msgRoot);
    Element msgBody = new Element(bodyTag);
    if (message != null && message instanceof SettableMessage) {
      try {
        SettableMessage msg = (SettableMessage) message;
        // Get all header info
        logger.debug("Add header info");
        if (msg.getAllHeaders() != null) {
          Iterator headerItr = msg.getAllHeaders().iterator();
          while (headerItr.hasNext()) {
            Header h = (Header) headerItr.next();
            Element headerElement = new Element(headerTag);
            headerElement.setAttribute("name", h.getName());
            headerElement.setAttribute("value", h.getValue());
            msgRoot.addContent(headerElement);
          }
        }
        Element partElement = new Element(partTag);
        msgBody.addContent(partElement);
        Element bodyElement = new Element(bodyTag);
        // Part Headers
        logger.debug("Add body headers");
        if (msg.getAllBodyHeaders() != null) {
          Iterator headerItr = msg.getAllBodyHeaders().iterator();
          while (headerItr.hasNext()) {
            Header h = (Header) headerItr.next();
            Element headerElement = new Element(headerTag);
            headerElement.setAttribute("name", h.getName());
            headerElement.setAttribute("value", h.getValue());
            partElement.addContent(headerElement);
          }
        }
        // Part Content
        partElement.addContent(bodyElement);
        Object bodyContent = msg.getBodyContent();
        if (bodyContent != null) {
          logger.debug("Add body content");
          if (bodyContent instanceof String) {
            bodyElement.setText((String) bodyContent);
          } else if (bodyContent instanceof Multipart) {
            MultipartContentBuilder builder = new MessageBuilder().new MultipartContentBuilder();
            Element mpElement = null;
            try {
              mpElement = builder.build((Multipart) bodyContent);
            } catch (Exception e) {
              throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Message-to-XML conversion error", message, e);
            }
            if (mpElement != null) {
              bodyElement.addContent(mpElement);
            }
          } else {
            bodyElement.setText(bodyContent.toString());
          }
        }
      } catch (IllegalDataException e) {
        // don't add message object as context, as this leads to a cycle when trying
        // to log the exception which includes a toString on the context...
        throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Illegal content for XML", message, e);
      }
    }
    msgRoot.addContent(msgBody);
    XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
    String res = xmlOut.outputString(doc);

    return res;
  }

  public static String buildToXML(Multipart mp) throws IOException, MessagingException {
    MultipartContentBuilder builder = new MessageBuilder().new MultipartContentBuilder();
    Element mpElement = builder.build(mp);
    if (mpElement == null)
      return null;
    XMLOutputter xmlOut = new XMLOutputter(Format.getCompactFormat());
    return xmlOut.outputString(mpElement);
  }

  private class MultipartContentBuilder {
    Element mpElement = new Element(partsTag);

    public Element build(Multipart mp) throws IOException, MessagingException {
      int count = 0;
      try {
        count = mp.getCount();
      } catch (MessagingException e) {
        logger.debug("Unable to get count for msg multipart");
      }
      for (int i = 0; i < count; i++) {
        BodyPart body = mp.getBodyPart(i);
        BodyPartElementBuilder bodyPartBuilder = new BodyPartElementBuilder();
        mpElement.addContent(bodyPartBuilder.build(body));
      }
      if (mpElement.getChildren().size() == 0)
        return null;
      return mpElement;
    }
  }

  private class BodyPartElementBuilder {
    Element partElement = new Element(partTag);
    Element bodyElement = new Element(bodyTag);

    public Element build(BodyPart part) throws IOException, MessagingException {
      Object o = part.getContent();

      Enumeration hdrs = part.getAllHeaders();
      while (hdrs != null && hdrs.hasMoreElements()) {
        Header hdr = (Header) hdrs.nextElement();
        Element headerElement = new Element(headerTag);
        headerElement.setAttribute("name", hdr.getName());
        headerElement.setAttribute("value", hdr.getValue());
        partElement.addContent(headerElement);
      }

      if (o instanceof String) {
        bodyElement.setText((String) o);
      } else if (o instanceof Multipart) {
        MultipartContentBuilder builder = new MultipartContentBuilder();
        bodyElement.addContent(builder.build((Multipart) o));
      } else if (o instanceof java.io.InputStream) {
        /*
         * StreamContentBuilder builder = new StreamContentBuilder(); if (part instanceof MimeBodyPart)
         * bodyElement.addContent(builder.build(((MimeBodyPart) part).getRawInputStream())); else
         * bodyElement.addContent(builder.build(o));
         */
      }
      partElement.addContent(bodyElement);
      return partElement;
    }
  }

  /**
   * Create a ManagedMessage from an XML-serialized format.
   * 
   * @param msgXML
   * @return
   * @throws MessageException
   */
  public static ManagedMessage buildFromXML(String msgXML) throws MessageException {
    SettableMessage res = new MessageContainer();

    SAXBuilder bldr = new SAXBuilder(false);
    Document msgDoc = null;
    try {
      msgDoc = bldr.build(new StringReader(msgXML));
    } catch (Exception e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error parsing XML " + msgXML, e);
    }
    Element rootElem = msgDoc.getRootElement();
    List headerElements = rootElem.getChildren(headerTag);
    if (headerElements != null && !headerElements.isEmpty()) {
      Iterator iterator = headerElements.iterator();
      while (iterator.hasNext()) {
        Element element = (Element) iterator.next();
        res.addHeader(element.getAttributeValue("name"), element.getAttributeValue("value"));
      }
    }

    Element bodyElem = rootElem.getChild(bodyTag);
    Element partElem = bodyElem.getChild(partTag);
    BodyPartBuilder bpb = new MessageBuilder().new BodyPartBuilder();
    try {
      res.setBody(bpb.build(partElem));
    } catch (Exception e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Failed to build message body from " + msgXML, e);
    }
    return res;
  }

  /**
   * Fill a message's headers and body from an XML serialized form.
   * 
   * @param message
   * @param msgXML
   * @return
   * @throws MessageException
   */
  public static ManagedMessage fillFromXML(ManagedMessage message, String msgXML) throws MessageException {
    SettableMessage res = null;
    try {
      res = (SettableMessage) message;
    } catch (ClassCastException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Message is not settable for "+msgXML, message, e);
    }

    SAXBuilder bldr = new SAXBuilder(false);
    Document msgDoc = null;
    try {
      msgDoc = bldr.build(new StringReader(msgXML));
    } catch (Exception e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error parsing XML "+msgXML, message, e);
    }
    Element rootElem = msgDoc.getRootElement();
    List headerElements = rootElem.getChildren(headerTag);
    if (headerElements != null && !headerElements.isEmpty()) {
      Iterator iterator = headerElements.iterator();
      while (iterator.hasNext()) {
        Element element = (Element) iterator.next();
        res.addHeader(element.getAttributeValue("name"), element.getAttributeValue("value"));
      }
    }

    Element bodyElem = rootElem.getChild(bodyTag);
    Element partElem = bodyElem.getChild(partTag);
    BodyPartBuilder bpb = new MessageBuilder().new BodyPartBuilder();
    try {
      res.setBody(bpb.build(partElem));
    } catch (Exception e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Failed to build message body "+msgXML, message, e);
    }
    return res;
  }

  private class BodyPartBuilder {
    Element partElement = new Element(partTag);
    Element bodyElement = new Element(bodyTag);

    public PasserelleBodyPart build(Element partElement) throws IOException, MessagingException {
      PasserelleBodyPart bodypart = new PasserelleBodyPart();
      String contentType = "text/plain";
      Element bodyElem = partElement.getChild(bodyTag);
      Element partsElem = bodyElem.getChild(partsTag);
      if (partsElem == null) {
        // BodyPart Content is no Multipart
        bodypart.setContent(bodyElem.getText(), contentType);
      } else {
        MimeMultipart mp = new MimeMultipart();
        // BodyPart Content is a Multipart
        List partElements = partsElem.getChildren(partTag);
        if (partElements != null && !partElements.isEmpty()) {
          Iterator iterator = partElements.iterator();
          while (iterator.hasNext()) {
            Element partelement = (Element) iterator.next();
            BodyPartBuilder bpb = new BodyPartBuilder();
            mp.addBodyPart(bpb.build(partelement));
          }
        }
        bodypart.setContent(mp);
      }

      List headerElements = partElement.getChildren(headerTag);
      if (headerElements != null && !headerElements.isEmpty()) {
        Iterator iterator = headerElements.iterator();
        while (iterator.hasNext()) {
          Element element = (Element) iterator.next();
          String hdrName = element.getAttributeValue("name");
          String hdrValue = element.getAttributeValue("value");
          bodypart.addHeader(hdrName, hdrValue);
          if (hdrName.equals(contentTypeTag)) {
            contentType = hdrValue;
          }
        }
      }

      // bodypart.saveChanges();
      return bodypart;
    }
  }

}