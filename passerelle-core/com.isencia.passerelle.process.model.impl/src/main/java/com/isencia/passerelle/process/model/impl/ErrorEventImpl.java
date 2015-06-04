package com.isencia.passerelle.process.model.impl;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Status;

@Cacheable(false)
@Entity
@DiscriminatorValue("ERROREVENT")
public class ErrorEventImpl extends ContextEventImpl implements ContextErrorEvent {
	private static final long serialVersionUID = 1L;
	
@Transient
  private ErrorItem errorItem;
  
  protected ErrorEventImpl() {

  }

  public ErrorEventImpl(Context context) {
    super(context, Status.ERROR.name());
  }

  /**
   * @param context
   * @param topic
   * @param message
   */
  public ErrorEventImpl(Context context, ErrorItem errorItem) {
    this(context);
    this.errorItem = errorItem;
  }

  public ErrorItem getErrorItem() {
    // TODO implement me
    return null;
  }

}
