package com.isencia.passerelle.process.model.impl;

import com.isencia.passerelle.process.model.Case;

public class MainRequestImpl extends RequestImpl {
	private static final long serialVersionUID = 1L;

  public MainRequestImpl() {
    super();
  }

  public MainRequestImpl(Case requestCase, String initiator, String type, String correlationId, String category) {
    super(requestCase, initiator, type, correlationId, category);
  }

  public MainRequestImpl(Case requestCase, String initiator, String type, String correlationId) {
    super(requestCase, initiator, type, correlationId);
  }

  public MainRequestImpl(Case requestCase, String initiator, String type) {
    super(requestCase, initiator, type);

  }

  public MainRequestImpl(String initiator, String type, String correlationId) {
    super(initiator, type, correlationId);

  }

  public MainRequestImpl(String initiator, String type) {
    super(initiator, type);

  }

}
