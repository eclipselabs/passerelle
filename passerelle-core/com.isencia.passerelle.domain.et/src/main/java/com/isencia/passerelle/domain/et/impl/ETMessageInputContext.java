package com.isencia.passerelle.domain.et.impl;

import java.util.ArrayList;
import java.util.List;
import com.isencia.passerelle.domain.et.SendEvent;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageInputContext;

public class ETMessageInputContext extends MessageInputContext {

  private List<SendEvent> sendEvents = new ArrayList<SendEvent>();

  public ETMessageInputContext(SendEvent sendEvent, int portIndex, String portName, ManagedMessage msg) {
    super(portIndex, portName, msg);
    this.sendEvents.add(sendEvent);
  }

  public List<SendEvent> getSendEvents() {
    return sendEvents;
  }

  @Override
  public boolean merge(MessageInputContext other) {
    boolean result = super.merge(other);
    if (result && other instanceof ETMessageInputContext) {
      this.sendEvents.addAll(((ETMessageInputContext) other).getSendEvents());
    }
    return result;
  }

  @Override
  public void setProcessed(boolean processed) {
    super.setProcessed(processed);
    for (SendEvent sendEvent : sendEvents) {
      sendEvent.setProcessed(processed);
    }
  }

}
