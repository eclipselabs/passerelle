package com.isencia.message.ftp;

import junit.framework.TestCase;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import com.isencia.message.ChannelException;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.extractor.TextLineMessageExtractor;

public class FtpReceiverChannelTest extends TestCase {
  
  FakeFtpServer ftpServer;
  FtpReceiverChannel ftpRcvChannel;

  protected void setUp() throws Exception {
    super.setUp();
    ftpServer = new FakeFtpServer();
    ftpServer.setServerControlPort(0);
    
    FileSystem fileSystem = new UnixFakeFileSystem();
    fileSystem.add(new FileEntry("/data/file1.txt", "abcdef\r\n1234567890"));
    ftpServer.setFileSystem(fileSystem);
    
    UserAccount userAccount = new UserAccount("pol", "pingo", "/data");
    ftpServer.addUserAccount(userAccount);
    ftpServer.start();
    
    int port = ftpServer.getServerControlPort();
    ftpRcvChannel = new FtpReceiverChannel("/data/file1.txt", "localhost", port, "pol", "pingo", false, true, new TextLineMessageExtractor());
  }

  protected void tearDown() throws Exception {
    try {
      ftpRcvChannel.close();
    } catch (Exception e) {
      // ignore; can give errors when channel was not open...
    }
    ftpServer.stop();
  }

  public void testClose() throws ChannelException {
    ftpRcvChannel.open();
    ftpRcvChannel.close();
    assertFalse("Channel should not be open", ftpRcvChannel.isOpen());
  }

  public void testOpen() throws ChannelException {
    ftpRcvChannel.open();
    assertTrue("Channel should be open", ftpRcvChannel.isOpen());
  }

  public void testGetMessages() throws ChannelException {
    ftpRcvChannel.open();
    try {
      assertEquals("abcdef", ftpRcvChannel.getMessage());
      assertEquals("1234567890", ftpRcvChannel.getMessage());
    } catch (NoMoreMessagesException e) {
      fail("Should have a msg");
    }
  }
}
