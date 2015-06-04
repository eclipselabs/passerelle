package com.isencia.passerelle.process.actor.flow;

import java.util.ArrayList;
import java.util.Collection;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

public class CollectingEvictedMessageHandler implements EvictedMessagesHandler {
  
  public Collection<ManagedMessage> evictedMessages = new ArrayList<ManagedMessage>();

  public void handleEvictedMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws PasserelleException {
    evictedMessages.add(initialMsg);
  }
}
