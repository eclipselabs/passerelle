package com.isencia.message;

import junit.framework.TestCase;

public class MessageReceiverTest extends TestCase {

  MessageReceiver msgReceiver = null;

  protected void tearDown() throws Exception {
    msgReceiver.close();
    msgReceiver = null;
  }

  public void testAddChannel() {
    msgReceiver = new MessageReceiver();
    MockReceiverChannel channel = new MockReceiverChannel();
    msgReceiver.addChannel(channel);
    assertEquals("Should contain 1 channel", 1, msgReceiver.getChannels().size());
    msgReceiver.removeChannel(channel);
    assertEquals("Shouldn't have any channels", 0, msgReceiver.getChannels().size());
  }

  public void testOpen() {
    msgReceiver = new MessageReceiver();
    assertFalse("Should not be open", msgReceiver.isOpen());
    msgReceiver.open();
    assertTrue("Should be open", msgReceiver.isOpen());
  }

  public void testClose() {
    msgReceiver = new MessageReceiver();
    msgReceiver.open();
    assertTrue("Should be open", msgReceiver.isOpen());
    msgReceiver.close();
    assertFalse("Should not be open", msgReceiver.isOpen());
  }

  public void testSourceClosedAutoCloseRcvr() throws ChannelException {
    msgReceiver = new MessageReceiver(true);
    MockReceiverChannel channel = new MockReceiverChannel();
    msgReceiver.addChannel(channel);
    msgReceiver.open();
    assertTrue("Should be open", msgReceiver.isOpen());
    channel.close();
    waitabit();
    assertFalse("Should not be open", msgReceiver.isOpen());
  }

  public void testSourceClosedNoAutoCloseRcvr() throws ChannelException {
    msgReceiver = new MessageReceiver(false);
    MockReceiverChannel channel = new MockReceiverChannel();
    msgReceiver.addChannel(channel);
    msgReceiver.open();
    assertTrue("Should be open", msgReceiver.isOpen());
    channel.close();
    waitabit();
    assertTrue("Should be open", msgReceiver.isOpen());
  }

  public void testAcceptAndHasMessage() {
    msgReceiver = new MessageReceiver();
    assertFalse("Shouldn't have a msg", msgReceiver.hasMessage());
    String message = "hello";
    msgReceiver.acceptMessage(message, null);
    assertTrue("Should have a msg", msgReceiver.hasMessage());
  }

  public void testAcceptAndGetMessage() {
    msgReceiver = new MessageReceiver();
    String message = "hello";
    msgReceiver.acceptMessage(message, null);
    try {
      Object rcvdMessage = msgReceiver.getMessage();
      assertEquals("Should have the accepted message", message, rcvdMessage);
    } catch (NoMoreMessagesException e) {
      fail("Should have the accepted message");
    }
  }
  
  public void testPushChannelGetMessage() {
    msgReceiver = new MessageReceiver();
    MockReceiverChannel channel = new MockReceiverChannel("hello", "world");
    msgReceiver.addChannel(channel);
    msgReceiver.open();
    channel.releaseMsg();
    waitabit();
    msgReceiver.close();
    assertTrue("Should have a msg", msgReceiver.hasMessage());
  }

  protected void waitabit() {
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
    }
  }
}
