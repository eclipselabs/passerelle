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
package com.isencia.passerelle.core;

import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * PasserelleException Base class for all exceptions in Passerelle.
 * 
 * @author erwin
 */
public class PasserelleException extends Exception implements Comparable<PasserelleException> {
  private static final long serialVersionUID = 1L;

  /**
   * @deprecated use ErrorCodes instead
   */
  public static enum Severity {
    NON_FATAL, FATAL;
  }

  // deprecated since v8.3, but still keeping it around for a while for backwards compatibility
  private Severity severity;
  // should by preference contain a ManagedMessage, to facilitate in-model error handling continuations
  // in that case, since v8.3, the msg will also be maintained in msgContext
  private Object context;

  /**
   * the main msg that was being processed when the exception was generated. in general, an actor's processing can depend on multiple received messages. but in
   * many cases one of the received messages can be designated as "the most important one". this one could then be selected for alternative flow continuations
   * ico errors.
   */
  private ManagedMessage msgContext;
  /**
   * the model element to which the exception is related, or has generated it. typically an actor.
   */
  private Nameable modelElement;

  private ErrorCode errorCode;

  // the place where the full msg is kept stored once it has been built,
  // after the first call to getMessage()
  private String detailedMessage;

  /**
   * @param message
   * @param context
   * @param rootException
   * @deprecated use the constructors with ErrorCodes instead
   */
  public PasserelleException(String message, Object context, Throwable rootException) {
    super(message, rootException);
    if (context instanceof ManagedMessage) {
      this.msgContext = (ManagedMessage) context;
    } else if (context instanceof Nameable) {
      this.modelElement = (Nameable) context;
    } else {
      this.context = context;
    }
    this.errorCode = ErrorCode.ERROR;
    this.severity = Severity.NON_FATAL;
  }

  /**
   * @param severity
   *          can not be null
   * @param message
   * @param context
   * @param rootException
   * @deprecated use the constructors with ErrorCodes instead
   */
  public PasserelleException(Severity severity, String message, Object context, Throwable rootException) {
    this(message, context, rootException);
    this.severity = severity;
    switch (severity) {
    case FATAL:
      errorCode = ErrorCode.FATAL;
      break;
    case NON_FATAL:
    default:
      errorCode = ErrorCode.ERROR;
    }
  }

  /**
   * @param errorCode
   *          can not be null
   * @param modelElement
   *          the element where the error was raised. Typically an actor, or the parent flow ico non-actor-related errors.
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, Nameable modelElement, Throwable rootException) {
    super(errorCode != null ? errorCode.getDescription() : null);
    if (errorCode == null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    if(rootException!=null) {
      initCause(rootException);
    }
    this.errorCode = errorCode;
    this.modelElement = modelElement;
    if (ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }

  /**
   * @param errorCode
   *          can not be null
   * @param message
   *          containing extra info/description for the error
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, String message, Throwable rootException) {
    super(message);
    if (errorCode == null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    if(rootException!=null) {
      initCause(rootException);
    }
    this.errorCode = errorCode;
    if (ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }

  /**
   * @param errorCode
   *          can not be null
   * @param message
   *          containing extra info/description for the error
   * @param modelElement
   *          the element where the error was raised.
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, String message, Nameable modelElement, Throwable rootException) {
    super(message);
    if (errorCode == null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    if(rootException!=null) {
      initCause(rootException);
    }
    this.errorCode = errorCode;
    this.modelElement = modelElement;
    if (ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }

  /**
   * @param errorCode
   *          can not be null
   * @param message
   *          containing extra info/description for the error
   * @param modelElement
   *          the element where the error was raised.
   * @param msgContext
   * @param rootException
   */
  public PasserelleException(ErrorCode errorCode, String message, Nameable modelElement, ManagedMessage msgContext, Throwable rootException) {
    super(message);
    if (errorCode == null) {
      throw new IllegalArgumentException("error code can not be null");
    }
    if(rootException!=null) {
      initCause(rootException);
    }
    this.errorCode = errorCode;
    this.modelElement = modelElement;
    this.msgContext = msgContext;
    if (ErrorCode.Severity.FATAL.equals(errorCode.getSeverity())) {
      this.severity = Severity.FATAL;
    } else {
      this.severity = Severity.NON_FATAL;
    }
  }

  /**
   * @return the msgContext
   */
  public ManagedMessage getMsgContext() {
    return msgContext;
  }

  /**
   * @return the modelElement
   */
  public Nameable getModelElement() {
    return modelElement;
  }

  /**
   * @return the context object that was specified for this exception (can be null)
   * @deprecated use getMsgContext and getModelElement
   */
  public Object getContext() {
    return context != null ? context : (msgContext != null ? msgContext : modelElement);
  }

  /**
   * @return the error code of this exception
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * @return the root exception that caused this exception (can be null)
   * @deprecated getCause()
   */
  public Throwable getRootException() {
    return getCause();
  }

  /**
   * @return the severity of the exception
   */
  public Severity getSeverity() {
    return severity;
  }

  /**
   * @return a string with the full info about the exception, incl severity, context etc.
   */
  public String getMessage() {
    if (detailedMessage == null) {
      StringBuilder msgBldr = new StringBuilder(getErrorCode() + " - " + super.getMessage());
      msgBldr.append("\n - Context:");
      boolean ctxtDetailsFound = false;
      if (modelElement != null) {
        msgBldr.append("\n\t -- element:" + modelElement);
      }
      if (msgContext != null) {
        msgBldr.append("\n\t -- message:" + msgContext);
      }
      if (!ctxtDetailsFound && context != null) {
        msgBldr.append("\n\t -- " + context);
      }
      if(getCause()!=null) {
        msgBldr.append("\n - RootException:" + getCause());
      }
      detailedMessage = msgBldr.toString();
    }
    return detailedMessage;
  }

  /**
   * @return just the simple message, as passed in the exception's constructor
   */
  public String getSimpleMessage() {
    return super.getMessage();
  }

  /**
   * Compares the exceptions' ErrorCode severities and in second priority also their actual numerical code.
   * Does an inverted order, i.e. to sort by "most-important" first.
   */
  public int compareTo(PasserelleException o) {
    if(o == this)
      return 0;
    if(o == null)
      return -1;
    int res =  - getErrorCode().getSeverity().compareTo(o.getErrorCode().getSeverity());
    if(res == 0) {
      res = getErrorCode().getCode().compareTo(o.getErrorCode().getCode());
    }
    return res;
  }
}
