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

package com.isencia.passerelle.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.SettableMessage;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Set of static methods to get a message from an input and present it in different ways, e.g. as a std Passerelle MessageContainer object or as a plain Java
 * String.
 * 
 * @author Erwin De ley
 */
public class MessageHelper {

  private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);


  /**
   * DOCUMENT ME!
   * 
   * @param part DOCUMENT ME!
   * @return DOCUMENT ME!
   */
  public static boolean isContent(Part part) {
    try {
      if (!MessageHelper.getContentDisposition(part).getDisposition().equalsIgnoreCase(Part.INLINE)) {
        return false;
      }
    } catch (MessagingException e) {
      return false;
    }

    return true;
  }

  /**
   * Get the content dispostion of a part. The part is interogated for a valid content disposition. If the content disposition is missing, a default disposition
   * is created based on the type of the part.
   * 
   * @param part The part to interogate
   * @return ContentDisposition of the part
   * @throws MessagingException DOCUMENT ME!
   * @see javax.mail.Part
   */
  static ContentDisposition getContentDisposition(Part part) throws MessagingException {
    String header = part.getDisposition();

    try {
      if (header != null) {
        return new ContentDisposition(header);
      }
    } catch (ParseException e) {
      throw new MessagingException(e.toString());
    }

    // set default disposition based on part type
    if (part instanceof MimeBodyPart) {
      Multipart parentMultipart = ((MimeBodyPart) part).getParent();

      if (parentMultipart != null) {
        Part parentPart = parentMultipart.getParent();
        logger.debug("trying get Disposition on Parent");

        if (parentPart != null) {
          String disposition = parentPart.getDisposition();
          logger.debug("Disposition on Parent : " + disposition);

          if (disposition != null) {
            return new ContentDisposition(disposition);
          }
        }
      }

      return new ContentDisposition(Part.ATTACHMENT);
    }

    return new ContentDisposition(Part.INLINE);
  }

  /**
   * A 'safe' version of JavaMail getContentType(), i.e. don't throw exceptions. The part is interogated for a valid content type. If the content type is
   * missing or invalid, a default content type of "text/plain" is assumed, which is suggested by the MIME standard.
   * 
   * @param part The part to interogate
   * @return ContentType of the part
   * @see javax.mail.Part
   */
  static ContentType getContentType(Part part) {
    String type = null;

    try {
      type = part.getContentType();
    } catch (MessagingException e) {
    }

    if (type == null) {
      type = "text/plain"; // MIME default content type if missing
    }

    ContentType ctype = null;

    try {
      ctype = new ContentType(type.toLowerCase());
    } catch (ParseException e) {
    }

    if (ctype == null) {
      ctype = new ContentType("text", "plain", null);
    }

    return ctype;
  }

  /**
   * Selects all msg part contents whose content-type matches the typeFilter array.
   * 
   * @param msg
   * @param typeFilter
   * @return an array containing strings from the message parts matching one of the given content types
   */
  public static Object[] getFilteredContent(ManagedMessage msg, String[] typeFilter) {
    if (logger.isTraceEnabled()) {
      logger.trace("Message :" + msg + "\nTypes :" + Arrays.asList(typeFilter));
    }

    Object[] res = null;
    if (msg instanceof SettableMessage) {
      Part msgBody = ((SettableMessage) msg).getBody();

      res = _getFilteredContent(msgBody, typeFilter);
    }
    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + Arrays.asList(res));
    }

    return res;
  }

  /**
   * @param typeFilter
   * @param msgBody
   * @return
   */
  private static Object[] _getFilteredContent(Part msgBody, String[] typeFilter) {
    Object[] res = null;
    if (msgBody != null) {
      try {
        Object content = msgBody.getContent();

        if (content instanceof String) {
          if (filterContent(msgBody, typeFilter)) {
            res = new String[] { (String) content };
          } else {
            res = new String[0];
          }
        } else if (content instanceof Multipart) {
          List r = new ArrayList();
          Multipart contPart = (Multipart) content;
          int partCount = contPart.getCount();

          for (int i = 0; i < partCount; ++i) {
            Part p = contPart.getBodyPart(i);
            Object[] tmp = _getFilteredContent(p, typeFilter);

            if ((tmp != null) && (tmp.length > 0)) {
              r.add(Arrays.asList(tmp));
            }
          }

          res = r.toArray();
        }
      } catch (IOException e) {
        logger.error("", e);
      } catch (MessagingException e) {
        logger.error("", e);
      }
    }
    return res;
  }

  /**
   * Tries to get a msg from the given input port, and extract a standard Passerelle ManagedMessage from it.
   * 
   * @param handler the input port where the message must be got
   * @return ManagedMessage the message from the input port
   * @throws PasserelleException
   */
  public static ManagedMessage getMessage(PortHandler handler) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(handler.toString()); // TODO Check if correct converted
    }

    ManagedMessage res = null;

    Token token = handler.getToken();
    if (token != null) {
      res = getMessageFromToken(token);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }

    return res;
  }

  /**
   * Tries to get a msg from the given input port, and extract a standard Passerelle ManagedMessage from it.
   * 
   * @param input the input port where the message must be got
   * @return ManagedMessage the message from the input port
   * @exception PasserelleException something fails during the getting of a standard ManagedMessage from the given input
   */
  public static ManagedMessage getMessage(IOPort input) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(input.getDisplayName()); // TODO Check if correct converted
    }

    ManagedMessage res = null;

    try {
      if (input.hasToken(0)) {
        try {
          Token token = input.get(0);

          if (token != null) {
            res = getMessageFromToken(token);
          }
        } catch (NoTokenException e) {
          // do nothing, will just return null,
          // indicating that this input will deliver no more messages
        }
      }
    } catch (IllegalActionException e) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Unexpected error while reading token from port", input, e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }

    return res;
  }

  /**
   * Tries to get a StringToken from the given input port, and returns the string contained in it.
   * 
   * @param input the input port where the message must be got
   * @return String the value of the StringToken
   * @throws PasserelleException
   */
  public static String getMessageAsString(IOPort input) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(input.getDisplayName()); // TODO Check if correct converted
    }

    String res = null;

    try {
      if (input.hasToken(0)) {
        try {
          Token token = input.get(0);
          if (token != null) {
            res = TokenHelper.getStringFromToken(token);
          }
        } catch (NoTokenException e) {
          // do nothing, will just return null,
          // indicating that this input will deliver no more messages
        }
      }
    } catch (IllegalActionException e) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Unexpected error while reading token from port", input, e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }

    return res;
  }

  /**
   * Tries to get a Token from the given input port, and returns it.
   * 
   * @param input the input port where the message must be got
   * @return Token the Token received
   * @exception PasserelleException when something fails during the getting of the Token from the input port
   */
  public static Token getMessageAsToken(IOPort input) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(input.getDisplayName()); // TODO Check if correct converted
    }

    Token res = null;

    try {
      if (input.hasToken(0)) {
        try {
          res = input.get(0);
        } catch (NoTokenException e) {
          // do nothing, will just return null,
          // indicating that this input will deliver no more messages
        }
      }
    } catch (IllegalActionException e) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Unexpected error while reading token from port", input, e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }

    return res;
  }

  /**
   * @param token
   * @return
   * @throws PasserelleException
   */
  public static ManagedMessage getMessageFromToken(Token token) throws PasserelleException {
    if (logger.isTraceEnabled()) {
      logger.trace(token.toString()); // TODO Check if correct converted
    }
    ManagedMessage res = null;

    if (token == null || token.isNil())
      return null;

    try {
      if (token instanceof PasserelleToken) {
        ManagedMessage obj = ((PasserelleToken) token).getMessage();
        // ensure that we create a new object, to prevent concurrency problems
        // in models with parallel branches
        res = MessageFactory.getInstance().copyMessage(obj);
      } else {
        String tokenMessage = null;
        if (token instanceof StringToken) {
          tokenMessage = ((StringToken) token).stringValue();
        } else if (token instanceof ScalarToken) {
          tokenMessage = token.toString();
        } else if (token instanceof BooleanToken) {
          tokenMessage = token.toString();
        } else if (token instanceof ArrayToken) {
          // TODO should construct some kind of array container
          tokenMessage = token.toString();
        }
        if (tokenMessage == null || tokenMessage.length() == 0)
          return null;

        // no longer sensible, we're not doing XML generation/parsing anymore
        // between actors
        // res = MessageBuilder.buildFromXML(tokenMessage);

        // so now, just build a MessageContainer with the message as body
        res = new MessageContainer();
        res.setBodyContentPlainText(tokenMessage);
      }

    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Error building MessageContainer from token in " + token, e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + res);
    }
    return res;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param part DOCUMENT ME!
   * @return DOCUMENT ME!
   */
  static boolean isMultipart(Part part) {
    try {
      if (!MessageHelper.getContentDisposition(part).getDisposition().equalsIgnoreCase(Part.INLINE)) {
        return false;
      }
    } catch (MessagingException e) {
      return false;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param part DOCUMENT ME!
   * @param newPart DOCUMENT ME!
   * @throws MessagingException DOCUMENT ME!
   */
  public static void copyHeaders(Part part, Part newPart) throws MessagingException {
    Enumeration headers = part.getAllHeaders();

    while (headers.hasMoreElements()) {
      Header element = (Header) headers.nextElement();
      newPart.addHeader(element.getName(), element.getValue());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param part DOCUMENT ME!
   * @param typeFilter DOCUMENT ME!
   * @return DOCUMENT ME!
   */
  public static boolean filterContent(Part part, String[] typeFilter) {
    if (logger.isTraceEnabled()) {
      logger.trace("Part :" + part + "\nTypes :" + Arrays.asList(typeFilter));
    }

    ContentType ctype = MessageHelper.getContentType(part);
    boolean found = false;

    if ((typeFilter == null) || (typeFilter.length == 0)) {
      found = true;
    } else if (!MessageHelper.isContent(part)) {
      logger.debug("Not a content");
    } else {
      logger.debug("Is content");

      if (ctype != null) {
        String type = ctype.getBaseType();
        logger.debug("Content type :" + type);

        for (int i = 0; (i < typeFilter.length) && !found; i++) {
          found = type.equalsIgnoreCase(typeFilter[i]);
        }
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("exit :" + (found ? "true" : "false"));
    }

    return found;
  }

  /**
   * Gets the primary MIME content type.
   * 
   * @return Returns the primary MIME content type as a String
   * @throws ParseException
   * @throws MessageException
   */
  public static String getPrimaryContentType(ManagedMessage message) throws MessageException {
    try {
      ContentType type = new ContentType(message.getBodyContentType());
      return type.getPrimaryType();
    } catch (ParseException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid body content type "+message.getBodyContentType(), message, e);
    }
  }

  /**
   * Gets the sub MIME content type.
   * 
   * @return Returns the sub MIME content type as a String
   * @throws ParseException
   * @throws MessagingException
   */
  public static String getSubContentType(ManagedMessage message) throws MessageException {
    try {
      ContentType type = new ContentType(message.getBodyContentType());
      return type.getSubType();
    } catch (ParseException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Invalid body content type "+message.getBodyContentType(), message, e);
    }
  }

  /**
   * @param message
   * @return
   */
  public static boolean hasTextContent(ManagedMessage message) {
    try {
      return getPrimaryContentType(message).equalsIgnoreCase("text");
    } catch (Exception e) {
      logger.error("", e);
      return false;
    }
  }

}