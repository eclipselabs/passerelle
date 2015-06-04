/* Copyright 2014 - iSencia Belgium NV

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
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.message.ChannelException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.message.io.FileSenderChannel;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.interceptor.MessageToTextConverter;

/**
 * A new implementation of a FileWriter actor which forwards received messages on its output port (i.e. is not a Sink)
 * and is based on the v5 Actor API. It also allows to configure the path and filename separately.
 * <p>
 * The path/folder should exist. The file should be writeable.
 * </p>
 * @author erwin
 */
public class FileWriter2 extends Actor {
  private static final long serialVersionUID = 8208654039640443614L;
  private static final Logger LOGGER = LoggerFactory.getLogger(FileWriter2.class);

  public final static String PATH_PARAM = "Path";
  public final static String FILE_PARAM = "File";
  public final static String APPEND_PARAM = "Append";
  public final static String ENCODING_PARAM = "Encoding";

  private ISenderChannel sendChannel = null;

  public FileParameter destinationPathParam;
  public StringParameter destinationFileNameParam;
  public Parameter appendModeParam;
  public StringParameter fileEncodingParam = null;

  public Port input;
  public Port output;

  public FileWriter2(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    destinationPathParam = new FileParameter(this, PATH_PARAM);
    new Parameter(destinationPathParam, "allowDirectories", BooleanToken.TRUE);
    new Parameter(destinationPathParam, "allowFiles", BooleanToken.FALSE);
    registerConfigurableParameter(destinationPathParam);

    destinationFileNameParam = new StringParameter(this, FILE_PARAM);

    fileEncodingParam = new StringParameter(this, ENCODING_PARAM);
    fileEncodingParam.setExpression("UTF-8");
    fileEncodingParam.addChoice("UTF-8");
    fileEncodingParam.addChoice("UTF-16");
    fileEncodingParam.addChoice("ISO-LATIN-1");

    registerConfigurableParameter(fileEncodingParam);

    appendModeParam = new Parameter(this, APPEND_PARAM, new BooleanToken(true));
    appendModeParam.setTypeEquals(BaseType.BOOLEAN);
    registerConfigurableParameter(appendModeParam);

    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" +

        "<circle cx=\"0\" cy=\"0\" r=\"10\"" + "style=\"fill:white;stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"0\" x2=\"15\" y2=\"0\" "
        + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"12\" y1=\"-3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"12\" y1=\"3\" x2=\"15\" y2=\"0\" " + "style=\"stroke-width:2.0\"/>\n" + "</svg>\n");
  }

  @Override
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();
    try {
      File folder = destinationPathParam.asFile();
      if (folder == null) {
        throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "File path not specified", this, null);
      } else if(!folder.exists()) {
        throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "File path " + folder + " not found", this, null);
      } else {
        // try to get a write access to the destination file
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(new File(folder, destinationFileNameParam.stringValue()));
        } catch (Exception e) {
          throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Impossible to open file for writing", this, e);
        } finally {
          if(fos!=null) {
            try { fos.close(); } catch (Exception e) {/*ignore*/} //NOSONAR
          }
        }
      }
    } catch (IllegalActionException e) {
      throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading file params", this, e);
    }
  }

  // actor life-cycle methods

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    ISenderChannel res = null;
    try {
      res = createChannel();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not created correctly.", this, e);
    }
    if (res == null) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not created correctly.", this, null);
    } else {
      // just to make sure...
      try {
        closeChannel(getChannel());
      } catch (ChannelException e) {
        throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Sender channel not initialized correctly.", this, e);
      }
      sendChannel = res;
      sendChannel.setInterceptorChainOnEnter(createInterceptorChain());
    }
  }

  protected boolean doPreFire() throws ProcessingException {
    boolean res = super.doPreFire();
    try {
      ISenderChannel channel = getChannel();
      if (!channel.isOpen()) {
        openChannel(channel);
        getLogger().debug("{} - Opened : {}", getFullName(), channel);
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "File writing channel not opened correctly.", this, e);
    }
    res = res && super.doPreFire();
    return res;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    if (message != null) {
      try {
        getChannel().sendMessage(message);
        response.addOutputMessage(output, message);
      } catch (InterruptedException e) {
        // do nothing, just means we've got to stop
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error sending msg on channel", this, message, e);
      }
    } else {
      requestFinish();
    }
  }

  @Override
  protected void doStop() {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new RuntimeException(new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "File writing channel not closed correctly.", this, e));
    }
    super.doStop();
  }

  protected void doWrapUp() throws TerminationException {
    try {
      closeChannel(getChannel());
      getLogger().debug("{} - Closed : {}", getFullName(), getChannel());
    } catch (ChannelException e) {
      throw new TerminationException(ErrorCode.ACTOR_EXECUTION_ERROR, "File writing channel not closed correctly.", this, e);
    }
    super.doWrapUp();
  }

  // parameter value shortcut methods

  protected String getFileEncoding() throws IllegalActionException {
    return fileEncodingParam.stringValue();
  }

  // protected String getDestinationPath() throws IllegalActionException {
  // return destinationPathParam.stringValue();
  // }

  protected String getDestinationFileName() throws IllegalActionException {
    return destinationFileNameParam.stringValue();
  }

  protected boolean isAppendMode() throws IllegalActionException {
    return ((BooleanToken) appendModeParam.getToken()).booleanValue();
  }

  // channel mgmt methods

  protected ISenderChannel createChannel() throws IllegalActionException {
    if (getFileEncoding() != null && getFileEncoding().length() > 0) {
      return new FileSenderChannel(new File(destinationPathParam.asFile(), getDestinationFileName()), getFileEncoding(), new MessageTextLineGenerator());
    }
    return new FileSenderChannel(new File(destinationPathParam.asFile(), getDestinationFileName()), new MessageTextLineGenerator());
  }

  /**
   * This method can be overridden to define some custom mechanism to convert outgoing passerelle messages into some
   * desired format. The method is called by this base class, only when it is not configured in "pass-through" mode. By
   * default, creates an interceptor chain containing a simple message-body-to-text conversion.
   * 
   * @return
   */
  protected IMessageInterceptorChain createInterceptorChain() {
    IMessageInterceptorChain interceptors = new MessageInterceptorChain();
    interceptors.add(new MessageToTextConverter());
    return interceptors;
  }

  /**
   * Overridable method to allow modification of channel closing.
   * 
   * @param ch
   * @throws ChannelException
   */
  protected void closeChannel(ISenderChannel ch) throws ChannelException {
    if ((ch != null) && ch.isOpen()) {
      getLogger().trace("{} closeChannel() - entry", getFullName());
      ch.close();
      getLogger().trace("{} closeChannel() - exit", getFullName());
    }
  }

  protected void openChannel(ISenderChannel ch) throws ChannelException, IllegalActionException {
    if (ch != null) {
      getLogger().trace("{} openChannel() - entry", getFullName());
      ((FileSenderChannel) ch).open(isAppendMode());
      getLogger().trace("{} openChannel() - exit", getFullName());
    }
  }

  /**
   * @return the sender channel to write to the configured file.
   */
  protected ISenderChannel getChannel() {
    return sendChannel;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

}
