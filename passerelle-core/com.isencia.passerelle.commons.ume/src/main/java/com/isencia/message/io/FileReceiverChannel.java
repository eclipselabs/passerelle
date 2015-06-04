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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.message.ChannelException;
import com.isencia.message.ReaderReceiverChannel;
import com.isencia.message.extractor.IMessageExtractor;

/**
 * FileReceiverChannel TODO: class comment
 * 
 * @author erwin
 */
public class FileReceiverChannel extends ReaderReceiverChannel {
  private final static Logger logger = LoggerFactory.getLogger(FileReceiverChannel.class);

  private File source = null;
  private String encoding = null;

  /**
   * @param srcFile
   * @param extractor
   */
  public FileReceiverChannel(File srcFile, IMessageExtractor extractor) {
    super(extractor);
    source = srcFile;
  }

  public FileReceiverChannel(File srcFile, String srcEncoding, IMessageExtractor extractor) {
    super(extractor);
    source = srcFile;
    encoding = srcEncoding;
  }

  /*
   * @see IReceiverChannel#open()
   */
  public void open() throws ChannelException {
    if (logger.isTraceEnabled()) logger.trace("open() - entry");

    if (source == null) throw new ChannelException("Source file is null");

    try {
      Reader reader = null;
      if (encoding != null) {
        FileInputStream fileInputStream = new FileInputStream(source);
        reader = new InputStreamReader(fileInputStream, encoding);
      } else {
        reader = new FileReader(source);
      }
      setReader(reader);
    } catch (FileNotFoundException e) {
      throw new ChannelException("Source file " + source.getAbsolutePath() + " does not exist");
    } catch (UnsupportedEncodingException e) {
      throw new ChannelException("UnsupportedEncodingException " + encoding);
    }

    super.open();

    if (logger.isTraceEnabled()) logger.trace("open() - exit");
  }

  /**
   * Gets the source info, being the absolute path of the source file.
   * 
   * @return String
   */
  public String getSourceInfo() {
    return source.getAbsolutePath();
  }

}
