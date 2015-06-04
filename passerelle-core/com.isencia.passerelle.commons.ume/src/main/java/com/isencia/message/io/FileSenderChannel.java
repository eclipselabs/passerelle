/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.message.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.WriterSenderChannel;
import com.isencia.message.generator.IMessageGenerator;

/**
 * FileSenderChannel TODO: class comment
 * 
 * @author erwin
 */
public class FileSenderChannel extends WriterSenderChannel {
  private final static Logger logger = LoggerFactory.getLogger(FileSenderChannel.class);

  private File destFile = null;
  private String encoding = null;

  /**
   * @param destFile
   * @param generator
   */
  public FileSenderChannel(File destFile, IMessageGenerator generator) {
    super(generator);
    this.destFile = destFile;
  }

  /**
   * @param destFile
   * @param generator
   */
  public FileSenderChannel(File destFile, String encoding, IMessageGenerator generator) {
    this(destFile, generator);
    this.encoding = encoding;
  }

  public void open() throws ChannelException {
    open(false);
  }

  /**
   * @param append
   * @throws ChannelException
   */
  public void open(boolean append) throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (destFile == null) throw new ChannelException("Destination file is not specified");

    try {
      if (encoding != null) {
        FileOutputStream fileOutputStream = new FileOutputStream(destFile, append);
        setWriter(new OutputStreamWriter(fileOutputStream, encoding));
      } else {
        setWriter(new FileWriter(destFile, append));
      }

    } catch (UnsupportedEncodingException e) {
      throw new ChannelException("UnsupportedEncodingException " + encoding);
    } catch (IOException e) {
      logger.error("Error opening destination file " + destFile.getAbsolutePath(), e);
      throw new ChannelException("Error opening destination file " + destFile.getAbsolutePath() + " : " + e.getMessage());
    }

    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

}
