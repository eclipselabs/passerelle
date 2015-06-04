/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.impl.util.internal.ErrorItemMarshaller;

/**
 * @author delerw
 */
@Cacheable(false)
@Entity
@DiscriminatorValue("EDMERROREVENT")
public class ContextErrorEventImpl extends ContextEventImpl implements ContextErrorEvent,Serializable {

  private final static Logger LOGGER = LoggerFactory.getLogger(ContextErrorEventImpl.class);

  private static final long serialVersionUID = 1L;

  @Transient
  private ErrorItem errorItem;

  public ContextErrorEventImpl() {
  }

  /**
   * @param context
   * @param topic
   */
  public ContextErrorEventImpl(Context context) {
    super(context, Status.ERROR.name());
  }

  /**
   * @param context
   * @param topic
   * @param message
   */
  public ContextErrorEventImpl(Context context, ErrorItem errorItem) {
    this(context);
    this.errorItem = errorItem;
    if (errorItem != null) {
      try {
        this.setMessage(marshallErrorInfo(errorItem));
      } catch (Exception e) {
        LOGGER.error("Error marshalling errorinfo", e);
      }
    }
  }

  public ErrorItem getErrorItem() {
    if (errorItem == null && getMessage() != null) {
      errorItem = unmarshallErrorInfo(getMessage());
    }
    return errorItem;
  }

  protected String marshallErrorInfo(ErrorItem errorItem) throws Exception {
    return ErrorItemMarshaller.marshallErrorInfo(errorItem);
  }

  protected ErrorItem unmarshallErrorInfo(String marshalledInfo) {
    return ErrorItemMarshaller.unmarshallErrorInfo(marshalledInfo);
  }
}
