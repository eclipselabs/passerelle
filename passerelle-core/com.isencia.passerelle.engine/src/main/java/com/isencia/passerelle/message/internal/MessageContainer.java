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
package com.isencia.passerelle.message.internal;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.AuditTrailEntry;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.xml.XmlMessageHelper;
import com.isencia.util.ArrayUtil;
import com.isencia.util.MapOfLists;



/**
 * A Passerelle MessageContainer provides features to store and handle
 * complex message structures with headers, body, attachments etc.
 * 
 * It corresponds to a message hierarchy like<br>
 * 
 * <code>
 * Message<br>
 * .Header ...<br>
 * ...<br>
 * .Body<br>
 * ..Part<br>
 * ...Header ...<br>
 * ...<br>
 * ...Parts<br>
 * ....Part<br>
 * .....Header ...<br>
 * ...<br>
 * .....Body<br>
 * ...<br>
 * ...../Body<br>
 * ..../Part<br>
 * ....Part<br>
 * .....Body<br>
 * ...<br>
 * ...../Body<br>
 * ..../Part<br>
 * .../Parts<br>
 * ../Part<br>
 * ./Body<br>
 * /Message<br>
 * </code>
 * 
 * where different body-parts can contain alternate representations (e.g. using different MIME types), attachments etc.
 * 
 * The structure is designed to support simple messages and also the most complex e-mail messages.
 * 
 * The message contains top-level header attributes, typically used to add transport-related information for Passerelle.
 * A set of add/set/get methods is provided for easier treatment of these predefined headers.
 * 
 * The body (and/or its parts) may also contain own header info, meant to contain data related to the handling of that specific
 * body (part).
 * 
 * @author        erwin
 */
public class MessageContainer implements ManagedMessage, SettableMessage {
  private static Logger LOGGER = LoggerFactory.getLogger(MessageContainer.class);

  // collection of headers (i.e. name/value pairs)
  protected MapOfLists headers = null;

  // the data content
  private PasserelleBodyPart body = null;

  public MessageContainer() {
    headers = new MapOfLists();
    body = new PasserelleBodyPart();
    try {
      // Initialize the body content
      body.setText("");
      body.setDisposition(MimeBodyPart.INLINE);
    } catch (MessagingException e) {
      // should never happen
      LOGGER.error("", e);
    }
  }

  /**
   * Returns a copy of this message container. Header collections and body are copied in a shallow way. I.e. all header
   * collections are copies, their entries are reused. Since javax.mail.Header is immutable, this is sufficient.
   * 
   * @return MessageContainer
   */
  public MessageContainer copy() throws MessageException {
    MessageContainer res = new MessageContainer();
    res.headers = this.headers.copy();
    Long msgVersion = getVersion();
    msgVersion = (msgVersion!=null) ? (msgVersion + 1) : 1;
    res.setHeader(ManagedMessage.SystemHeader.HEADER_VERSION, msgVersion.toString());
    res.setBodyContent(getBodyContent(), getBodyContentType());
    Iterator<Header> bodyHdrItr = getAllBodyHeaders().iterator();
    while (bodyHdrItr.hasNext()) {
      Header aHeader = bodyHdrItr.next();
      res.setBodyHeader(aHeader.getName(), aHeader.getValue());
    }
    return res;
  }

  public List<Header> getAllBodyHeaders() throws MessageException {
    try {
      List<Header> col = new ArrayList<Header>();
      Enumeration<?> hdrEnum = body.getAllHeaders();
      if (hdrEnum == null)
        return col;
      while (hdrEnum.hasMoreElements()) {
        col.add((Header) hdrEnum.nextElement());
      }
      return col;
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error getting msg headers", this, e);
    }
  }

  public Collection<Header> getAllHeaders() {
    return headers.values();
  }

  public void setBody(PasserelleBodyPart body) {
    this.body = body;
  }

  public PasserelleBodyPart getBody() {
    return body;
  }

  public void setBodyContent(Multipart part) throws MessageException {
    try {
      body.setContent(part);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error setting msg body", this, e);
    }
  }

  public void setBodyContentPlainText(String content) throws MessageException {
    try {
      body.setContent(content, "text/plain");
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error setting msg body with plain text", this, e);
    }
  }

  public void setBodyContent(Object content, String contentType) throws MessageException {
    try {
      body.setContent(content, contentType);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error setting msg body with content type "+contentType, this, e);
    }
  }

  public Object getBodyContent() throws MessageException {
    if (body == null)
      return null;

    try {
      return body.getContent();
    } catch (Exception e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error getting msg body", this, e);
    }
  }

  public String getBodyContentAsString() throws MessageException {
    try {
      Object content = getBodyContent();
      if (content == null)
        return null;

      if (content instanceof String)
        return (String) content;
      else if (content instanceof Multipart) {
        return XmlMessageHelper.getXMLFromMessageContent((Multipart) content);
      } else if (content.getClass().isArray()) {
        return ArrayUtil.toString(content, "", "", System.getProperty("line.separator"), "");
      } else
        return content.toString();
    } catch (MessageException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error getting msg body", this, e);
    }
  }

  public String getBodyContentType() throws MessageException {
    try {
      return body.getContentType();
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error getting msg content type", this, e);
    }
  }

  public void setBodyHeader(String name, String value) throws MessageException {
    try {
      body.setHeader(name, value);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error setting msg header "+name, this, e);
    }
  }

  public String[] getBodyHeader(String name) throws MessageException {
    try {
      return body.getHeader(name);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error getting msg header "+name, this, e);
    }
  }

  public void setHeader(String name, String value) {
    Collection<?> hdrs = (Collection<?>) headers.remove(name);

    if (hdrs != null)
      hdrs.clear();

    headers.put(name, new Header(name, value));
  }

  @Override
  public String getSingleHeader(String name) {
    String[] hdrs = getHeader(name);
    return ((hdrs!=null)&&(hdrs.length>0)) ? hdrs[0] : null;
  }
  
  public String[] getHeader(String name) {
    ArrayList<String> values = new ArrayList<String>();
    Collection<?> c = (Collection<?>) headers.get(name);

    if (c == null || c.size() == 0)
      return null;

    Iterator<?> i = c.iterator();

    while (i.hasNext()) {
      values.add(((Header) i.next()).getValue());
    }

    return (String[]) values.toArray(new String[0]);
  }

  public void addBodyHeader(String name, String value) throws MessageException {
    try {
      body.addHeader(name, value);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error adding msg header "+name, this, e);
    }
  }

  public void addHeader(String name, String value) {
    headers.put(name, new Header(name, value));
  }

  public boolean hasBodyHeader(String name) throws MessageException {
    String[] headers;
    try {
      headers = body.getHeader(name);
      return headers != null;
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error checking msg header "+name, this, e);
    }
  }

  public boolean hasHeader(String name) {
    return headers.containsKey(name);
  }

  public void removeBodyHeader(String name) throws MessageException {
    try {
      body.removeHeader(name);
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error removing msg header "+name, this, e);
    }
  }

  public void removeHeader(String name) {
    Collection<?> hdrs = (Collection<?>) headers.remove(name);

    if (hdrs != null)
      hdrs.clear();
  }

  /**
   * @throws MessageException
   */
  public void saveChanges() throws MessageException {
    try {
      getBody().saveChanges();
    } catch (MessagingException e) {
      throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error saving msg body", this, e);
    }
  }

  /*
   * @see Object#toString()
   */
  public String toString() {
    try {
      return XmlMessageHelper.getXMLFromMessage(this);
    } catch (MessageException e) {
      LOGGER.error("", e);
      return "";
    }
  }

  public Long getID() {
    return getSingleHeaderLongValue(SystemHeader.HEADER_ID);
  }

  public String getSourceRef() {
    return getSingleHeaderStringValue(SystemHeader.HEADER_SOURCE_REF);
  }

  public String[] getSourceExtraInfo() {
    return getMultiHeaderStringValue(SystemHeader.HEADER_SOURCE_INFO);
  }

  public Date getCreationTimeStamp() {
    return getSingleHeaderDateValue(SystemHeader.HEADER_TIMESTAMP_CREATION);
  }

  public Long getVersion() {
    return getSingleHeaderLongValue(SystemHeader.HEADER_VERSION);
  }

  public AuditTrailEntry[] getAuditTrail() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isCorrelated() {
    return (getCorrelationID() != null);
  }

  public Long getCorrelationID() {
    return getSingleHeaderLongValue(SystemHeader.HEADER_CORRELATION_ID);
  }

  public boolean isPartOfSequence() {
    return (getSequenceID() != null);
  }

  public Long getSequenceID() {
    return getSingleHeaderLongValue(SystemHeader.HEADER_SEQ_ID);
  }

  public void setSequenceID(Long seqID) {
    setHeader(SystemHeader.HEADER_SEQ_ID, seqID.toString());
  }

  public Long getSequencePosition() {
    return getSingleHeaderLongValue(SystemHeader.HEADER_SEQ_POS);
  }

  public void setSequencePosition(Long seqPos) {
    setHeader(SystemHeader.HEADER_SEQ_POS, seqPos.toString());
  }

  public boolean isSequenceEnd() {
    return getSingleHeaderBooleanValue(SystemHeader.HEADER_SEQ_END).booleanValue();
  }

  public void setSequenceEnd(boolean seqEnd) {
    setHeader(SystemHeader.HEADER_SEQ_END, Boolean.toString(seqEnd));
  }

  public boolean hasCauses() {
    return (getCauseIDs() != null && getCauseIDs().length > 0);
  }

  public Long[] getCauseIDs() {
    return getMultiHeaderLongValue(SystemHeader.HEADER_CAUSES_IDS);
  }

  public void addCauseID(Long causeID) {
    if (causeID != null)
      addHeader(SystemHeader.HEADER_CAUSES_IDS, causeID.toString());
  }

  // PRIVATE METHODS TO READ TYPED VALUES FROM HEADERS ========================================
  private Boolean getSingleHeaderBooleanValue(String headerName) {
    Boolean res = Boolean.FALSE;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      // just take the first one
      try {
        res = new Boolean(headerValues[0]);
      } catch (NumberFormatException e) {
        LOGGER.warn("Header " + headerName + " contains illegal value " + headerValues[0]);
      }
    }
    return res;
  }

  private Long getSingleHeaderLongValue(String headerName) {
    Long res = null;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      // just take the first one
      try {
        res = new Long(headerValues[0]);
      } catch (NumberFormatException e) {
        LOGGER.warn("Header " + headerName + " contains illegal value " + headerValues[0]);
      }
    }
    return res;
  }

  private Long[] getMultiHeaderLongValue(String headerName) {
    Long[] res = null;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      for (int i = 0; i < headerValues.length; ++i) {
        res = new Long[headerValues.length];
        try {
          res[i] = new Long(headerValues[i]);
        } catch (NumberFormatException e) {
          LOGGER.warn("Header " + headerName + " contains illegal value " + headerValues[i]);
        }
      }
    }
    return res;
  }

  private String getSingleHeaderStringValue(String headerName) {
    String res = null;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      // just take the first one
      res = headerValues[0];
    }
    return res;
  }

  private String[] getMultiHeaderStringValue(String headerName) {
    String[] res = null;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      res = new String[headerValues.length];
      System.arraycopy(headerValues, 0, res, 0, headerValues.length);
    }
    return res;
  }

  private Date getSingleHeaderDateValue(String headerName) {
    Date res = null;
    String[] headerValues = getHeader(headerName);
    if (headerValues != null && headerValues.length > 0) {
      // just take the first one
      String dateHeader = headerValues[0];
      try {
        // use the std date format to parse the timestamp
        res = MessageFactory.timestampFormat.parse(dateHeader);
      } catch (ParseException e) {
        LOGGER.warn("Header " + headerName + " contains illegal value " + headerValues[0]);
      }
    }
    return res;
  }
}