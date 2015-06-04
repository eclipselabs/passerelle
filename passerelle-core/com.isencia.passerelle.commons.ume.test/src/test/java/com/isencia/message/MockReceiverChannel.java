package com.isencia.message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * A mock implementation of a receiver channel, with a configurable array of sample msgs.
 * 
 * @author erwin
 */
public class MockReceiverChannel extends ReceiverChannel {
  public String[] messages;
  public int msgCounter;
  public BlockingQueue<Boolean> msgReleaseTokens = new LinkedBlockingQueue<Boolean>();
  
  public MockReceiverChannel(String... messages) {
    this.messages = messages;
  }
  public void releaseMsg() {
    msgReleaseTokens.offer(Boolean.TRUE);
  }
  @Override
  public Object doGetMessage() throws ChannelException, NoMoreMessagesException {
    if(msgCounter >= messages.length) {
      throw new NoMoreMessagesException();
    } else {
      try {
        msgReleaseTokens.take();
      } catch (InterruptedException e) {
        throw new ChannelException(e.getMessage());
      }
      return messages[msgCounter++];
    }
  }
  
  @Override
  public void open() throws ChannelException {
    super.open();
    start();
  }
  
  @Override
  public void close() throws ChannelException {
    super.close();
    interrupt();
  }
}
