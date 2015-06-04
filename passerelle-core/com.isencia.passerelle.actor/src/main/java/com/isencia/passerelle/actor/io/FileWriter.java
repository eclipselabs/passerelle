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
package com.isencia.passerelle.actor.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ChannelException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.message.io.FileSenderChannel;
import com.isencia.passerelle.actor.ChannelSink;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.util.StringConvertor;

/**
 * This actor writes all received msgs in a file.
 */
public class FileWriter extends ChannelSink {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(FileWriter.class);

  public final static String PATH_PARAM = "Path";
  public final static String APPEND_PARAM = "Append";
  public final static String ENCODING_PARAM = "Encoding";

  public FileParameter destinationPathParam;
  private String destinationPath = null;
  private boolean appendMode = true;
  public Parameter appendModeParam;

  public Parameter fileEncodingParam = null;
  private String fileEncoding = null;

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container The container.
   * @param name The name of this actor.
   * @exception IllegalActionException If the actor cannot be contained by the
   *              proposed container.
   * @exception NameDuplicationException If the container already has an actor
   *              with this name.
   */
  public FileWriter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    destinationPathParam = new FileParameter(this, PATH_PARAM);
    try {
      URI baseURI = new URI("file://" + StringConvertor.convertPathDelimiters(EnvironmentUtils.getApplicationRootFolder()));
      destinationPathParam.setBaseDirectory(baseURI);
    } catch (URISyntaxException e) {
      // just give up
    }
    registerConfigurableParameter(destinationPathParam);

    fileEncodingParam = new StringParameter(this, ENCODING_PARAM);
    registerConfigurableParameter(fileEncodingParam);

    appendModeParam = new Parameter(this, APPEND_PARAM, new BooleanToken(true));
    appendModeParam.setTypeEquals(BaseType.BOOLEAN);
    registerConfigurableParameter(appendModeParam);
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * @param attribute The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == fileEncodingParam) {
      try {
        setFileEncoding(fileEncodingParam.getExpression());
        getLogger().debug("{} File Encoding changed to {}", getFullName(), getFileEncoding());
      } catch (NullPointerException e) {
        // Ignore. Means that path is not a valid URL.
      }
    } else if (attribute == destinationPathParam) {
      try {
        setDestinationPath(destinationPathParam.asFile().getPath());
        getLogger().debug("{} Destination Path changed to {}", getFullName(), getDestinationPath());
      } catch (NullPointerException e) {
        // Ignore. Means that path is not a valid URL.
      }
    } else if (attribute == appendModeParam) {
      BooleanToken appendToken = (BooleanToken) appendModeParam.getToken();
      if (appendToken != null) {
        setAppendMode(appendToken.booleanValue());
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected String getFileEncoding() {
    return fileEncoding;
  }

  protected void setFileEncoding(String fileEncoding) {
    this.fileEncoding = fileEncoding;
  }

  protected ISenderChannel createChannel() {
    if (getFileEncoding() != null && getFileEncoding().length() > 0) {
      return new FileSenderChannel(new File(getDestinationPath()), getFileEncoding(), new MessageTextLineGenerator());
    }
    return new FileSenderChannel(new File(getDestinationPath()), new MessageTextLineGenerator());
  }

  /**
   * Returns the destinationPath.
   * 
   * @return String
   */
  public String getDestinationPath() {
    return destinationPath;
  }

  /**
   * Sets the destinationPath.
   * 
   * @param destinationPath The destinationPath to set
   */
  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

  public boolean isAppendMode() {
    return appendMode;
  }

  /**
   * Sets the appendMode.
   * 
   * @param appendMode The appendMode to set
   */
  public void setAppendMode(boolean appendMode) {
    this.appendMode = appendMode;
  }

  protected void openChannel(ISenderChannel ch) throws ChannelException {
    getLogger().trace("{} openChannel() - entry", getFullName());
    if (ch != null) {
      ((FileSenderChannel) ch).open(isAppendMode());
    }
    getLogger().trace("{} openChannel() - exit", getFullName());
  }
}