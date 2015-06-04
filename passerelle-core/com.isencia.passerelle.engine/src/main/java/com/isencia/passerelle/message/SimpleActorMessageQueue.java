package com.isencia.passerelle.message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import ptolemy.actor.Actor;
import ptolemy.actor.process.TerminateProcessException;
import com.isencia.passerelle.actor.InitializationException;

/**
 * Instances of this class can be used to manage the actor's internal buffer to handle messages from PUSH input ports. This implementation simply provides an
 * unlimited and non-blocking message queue. It can not be a Ptolemy receiver itself, as it's not contained in a Port, but in an Actor...
 * 
 * @author erwin
 */
public class SimpleActorMessageQueue implements MessageQueue {

  private Actor actor;
  private Queue<MessageInputContext> messages;
  private boolean terminate;

  public SimpleActorMessageQueue(Actor actor) throws InitializationException {
    this.actor = actor;
    messages = new ConcurrentLinkedQueue<MessageInputContext>();
  }

  public int getCapacity() {
    return INFINITE_CAPACITY;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public boolean isEmpty() {
    return messages.isEmpty();
  }

  public void setCapacity(int capacity) throws InitializationException, IllegalArgumentException {
  }

  public void put(MessageInputContext ctxt) throws InterruptedException {
    if (!terminate) {
      messages.add(ctxt);
    } else {
      throw new TerminateProcessException("Process terminated.");
    }
  }

  public MessageInputContext poll() throws InterruptedException, TerminateProcessException {
    MessageInputContext result = messages.poll();
    if (terminate && result == null) {
      throw new TerminateProcessException("");
    }
    return result;
  }

  /**
   * Clear the state variables for this queue.
   */
  public void clear() {
    messages.clear();
  }
}
