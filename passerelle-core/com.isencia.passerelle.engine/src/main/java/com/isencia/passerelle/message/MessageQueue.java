package com.isencia.passerelle.message;

import ptolemy.actor.process.TerminateProcessException;
import com.isencia.passerelle.actor.TerminationException;

/**
 * A minimal interface for a message queue used typically inside an Actor 
 * that is a MessageBuffer.
 * 
 * @author erwin
 *
 */
public interface MessageQueue {

  int INFINITE_CAPACITY = -1;
      
  int getCapacity();
  
  boolean isEmpty();
  
  int size();
  
  void put(MessageInputContext ctxt) throws InterruptedException, TerminationException;
  
  MessageInputContext poll() throws InterruptedException, TerminateProcessException;
  
  void clear();

}
