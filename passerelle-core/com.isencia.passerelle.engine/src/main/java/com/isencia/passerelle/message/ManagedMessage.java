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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The contract for messages that are managed by the Passerelle suite. Managed messages have a number of predefined
 * header fields, and some ways to manage content/payload.
 * 
 * @author erwin
 */
public interface ManagedMessage extends Serializable {

  final static String objectContentType = "application/java";

  public class SystemHeader {
    // Standard header fields for Passerelle managed messages
    // These headers are based on common principles and patterns for event/message-processing systems

    /**
     * MANDATORY header used to identify the source via which the message entered into the system, or the actor that
     * created the message inside the system.
     * 
     * Values are typically descriptive strings identifying system components, e.g. a concatenation of the model name
     * and the actor name.
     * 
     * Will normally be a single-valued header.
     */
    public final static String HEADER_SOURCE_REF = "__PSRL_SRC_REF";
    /**
     * OPTIONAL header used to maintain additional source info. Typically contains e.g. a URL for a file reader,
     * mail-box for a mail reader etc.
     * 
     * Can be a multi-valued header.
     */
    public final static String HEADER_SOURCE_INFO = "__PSRL_SRC_INFO";
    /**
     * MANDATORY header used to maintain a unique ID assigned to the message upon entering the system.
     * 
     * Will normally be a single-valued header.
     */
    public final static String HEADER_ID = "__PSRL_ID";
    /**
     * OPTIONAL header used to maintain a correlation ID, that can be used by a matcher-component to correlate this
     * message to another message. This is typically used to implement request/reply semantics in a message-based
     * system.
     * 
     * Will normally be a single-valued header.
     */
    public final static String HEADER_CORRELATION_ID = "__PSRL_CORRELATION_ID";
    /**
     * OPTIONAL header used to maintain a reference to a message sequence to which this message belongs.
     * 
     * E.g. for a file-reader, the complete set of messages generated for reading one file will be generated in a
     * sequence.
     * 
     * Single-valued.
     */
    public final static String HEADER_SEQ_ID = "__PSRL_SEQ_ID";
    /**
     * OPTIONAL header that indicates the position of a message in a sequence
     * 
     * Single-valued.
     */
    public final static String HEADER_SEQ_POS = "__PSRL_SEQ_POS";
    /**
     * OPTIONAL header that indicates whether the message is the last one in the sequence.
     * 
     * Single-valued.
     */
    public final static String HEADER_SEQ_END = "__PSRL_SEQ_END";
    /**
     * OPTIONAL header used to maintain references to other messages that are causes of this message.
     * 
     * Can be multi-valued.
     */
    public final static String HEADER_CAUSES_IDS = "__PSRL_CAUSES_IDS";
    /**
     * MANDATORY header used to maintain the timestamp of the moment that the message was received in the system, or
     * when it was generated inside the system.
     * 
     * Messages created by the system, i.e. not coming from a source actor, should normally maintain a reference to a
     * preceeding/causing message in the HEADER_CAUSES_REFS header.
     * 
     * This is a single-valued header.
     */
    public final static String HEADER_TIMESTAMP_CREATION = "__PSRL_TIMESTAMP_CREATION";
    /**
     * MANDATORY header used to maintain the version nr of the message, i.e. an indicator about how many times the
     * message was modified inside the system.
     * 
     * This is a single-valued header.
     */
    public final static String HEADER_VERSION = "__PSRL_VERSION";
    /**
     * MANDATORY header used to maintain the audit trail of the flow that the message followed in the system.
     * 
     * This is a multi-valued header, where each entry contains a timestamp and an actor reference. The method
     * <code>MessageContainer.addAuditTrail()</code> should preferably be used, in order to obtain a standard formatting
     * of the audit-trail entries.
     */
    public final static String HEADER_AUDIT_TRAIL = "__PSRL_AUDIT_TRAIL";
  }

  // Header properties for a managed message
  Long getID();

  String getSourceRef();

  String[] getSourceExtraInfo();

  Date getCreationTimeStamp();

  Long getVersion();

  AuditTrailEntry[] getAuditTrail();

  boolean isCorrelated();

  Long getCorrelationID();

  boolean isPartOfSequence();

  Long getSequenceID();

  void setSequenceID(Long seqID);

  Long getSequencePosition();

  void setSequencePosition(Long seqPos);

  boolean isSequenceEnd();

  void setSequenceEnd(boolean seqEnd);

  boolean hasCauses();

  Long[] getCauseIDs();

  void addCauseID(Long causeID);

  // system-oriented headers
  boolean hasHeader(String name);
  String getSingleHeader(String name);
  String[] getHeader(String name);
  Collection getAllHeaders() throws MessageException;

  // Content handling
  // app-oriented body headers
  boolean hasBodyHeader(String name) throws MessageException;
  String[] getBodyHeader(String name) throws MessageException;
  List getAllBodyHeaders() throws MessageException;

  void addBodyHeader(String name, String value) throws MessageException;
  void setBodyHeader(String name, String value) throws MessageException;
  void removeBodyHeader(String name) throws MessageException;

  Object getBodyContent() throws MessageException;

  String getBodyContentAsString() throws MessageException;

  void setBodyContent(Object content, String contentType) throws MessageException;

  void setBodyContentPlainText(String text) throws MessageException;

  String getBodyContentType() throws MessageException;

}
