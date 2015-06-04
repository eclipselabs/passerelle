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

package com.isencia.passerelle.actor.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.util.RuntimeStreamReader;
import com.isencia.util.RuntimeStreamReader.RuntimeStreamListener;
import com.isencia.util.commandline.EnvCommandline;

/**
 * Executes a configurable shell command when receiving a trigger message.
 * 
 * @author erwin
 */
public class CommandExecutor extends Actor {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);
  public static final String COMMAND_HEADER = "Command";
  public static final String TRIGGER_PORT = "trigger";
  public static final String COMMAND_PARAMETER = "command";
  public static final String PARAMETERS_PARAMETER = "params";

  public Parameter commandParameter;
  public Parameter paramsParameter;
  public Port trigger;
  public Port cmdOut;
  public Port cmdErr;

  private RuntimeStreamListener cmdOutListener;
  private RuntimeStreamListener cmdErrListener;

  private PortHandler triggerHandler;
  private String defaultSourcePath;
  private boolean triggerConnected;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public CommandExecutor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    commandParameter = new StringParameter(this, COMMAND_PARAMETER);
    commandParameter.setExpression("");
    registerConfigurableParameter(commandParameter);
    paramsParameter = new StringParameter(this, PARAMETERS_PARAMETER);
    paramsParameter.setExpression("");
    registerConfigurableParameter(paramsParameter);

    trigger = PortFactory.getInstance().createInputPort(this, TRIGGER_PORT, null);
    cmdOut = PortFactory.getInstance().createOutputPort(this, "cmdOut");
    cmdErr = PortFactory.getInstance().createOutputPort(this, "cmdErr");
    cmdOutListener = new CommandOutputListener(cmdOut);
    cmdErrListener = new CommandOutputListener(cmdErr);

    _attachText("_iconDescription", "<svg>\n"
        + "<rect x=\"-20\" y=\"-20\" width=\"40\" "
        + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" "
        + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" "
        + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n"
        + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        +
        // body
        "<line x1=\"-9\" y1=\"-16\" x2=\"-12\" y2=\"-8\" "
        + "style=\"stroke-width:2.0\"/>\n"
        +
        // backwards leg
        "<line x1=\"-11\" y1=\"-7\" x2=\"-16\" y2=\"-7\" "
        + "style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-13\" y1=\"-8\" x2=\"-15\" y2=\"-8\" "
        + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-16\" y1=\"-7\" x2=\"-16\" y2=\"-5\" "
        + "style=\"stroke-width:1.0\"/>\n"
        +
        // forward leg
        "<line x1=\"-11\" y1=\"-11\" x2=\"-8\" y2=\"-8\" "
        + "style=\"stroke-width:1.5\"/>\n"
        + "<line x1=\"-8\" y1=\"-8\" x2=\"-8\" y2=\"-6\" "
        + "style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-8\" y1=\"-5\" x2=\"-6\" y2=\"-5\" "
        + "style=\"stroke-width:1.0\"/>\n"
        +
        // forward arm
        "<line x1=\"-10\" y1=\"-14\" x2=\"-7\" y2=\"-11\" "
        + "style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-7\" y1=\"-11\" x2=\"-5\" y2=\"-14\" "
        + "style=\"stroke-width:1.0\"/>\n"
        +
        // backward arm
        "<line x1=\"-11\" y1=\"-14\" x2=\"-14\" y2=\"-14\" "
        + "style=\"stroke-width:1.0\"/>\n"
        + "<line x1=\"-14\" y1=\"-14\" x2=\"-12\" y2=\"-11\" "
        + "style=\"stroke-width:1.0\"/>\n"
        +
        // cmd field
        "<rect x=\"-15\" y=\"-3\" width=\"28\" " + "height=\"12\" style=\"fill:white;stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-14\" y1=\"-2\" x2=\"13\" y2=\"-2\" " + "style=\"stroke-width:1.5;stroke:grey\"/>\n" + "<line x1=\"-14\" y1=\"-2\" x2=\"-14\" y2=\"10\" "
        + "style=\"stroke-width:1.5;stroke:grey\"/>\n" + "<line x1=\"15\" y1=\"-2\" x2=\"15\" y2=\"11\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-15\" y1=\"11\" x2=\"15\" y2=\"11\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<text x=\"-15\" y=\"5\" style=\"font-size:8\"> cmd </text>\n" + "</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if ((attribute == commandParameter) || (attribute == paramsParameter)) {
      StringToken commandToken = (StringToken) commandParameter.getToken();
      StringToken paramsToken = (StringToken) paramsParameter.getToken();
      if ((commandToken != null) && (commandToken.stringValue().length() > 0)) {
        String cmd = commandToken.stringValue();
        defaultSourcePath = cmd + " " + paramsToken.stringValue();
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void doFire() throws ProcessingException {
    ManagedMessage msg = null;
    String[] sourcePath = null;

    if (triggerConnected) {
      getLogger().debug("{} - Waiting for trigger", getFullName());

      Token token = triggerHandler.getToken();
      if (token == null) {
        requestFinish();
      } else if (token.isNil()) {
        // a NIL token is not a trigger!
        return;
      } else {
        try {
          msg = MessageHelper.getMessageFromToken(token);
        } catch (PasserelleException e) {
          throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Exception while reading message from "+token, this, e);
        }
        getLogger().debug("{} - Received msg : {}", getFullName(), getAuditTrailMessage(msg, trigger));
      }
    }
    if (!isFinishRequested()) {
      try {
        // Check for command in header
        if ((msg != null) && msg.hasBodyHeader(COMMAND_HEADER)) {
          sourcePath = msg.getBodyHeader(COMMAND_HEADER);
        }
      } catch (MessageException e) {
        // just log it for completeness sake
        getLogger().error("", e);
      }

      if ((sourcePath == null) || (sourcePath.length == 0)) {
        sourcePath = new String[] { defaultSourcePath };
      }

      if ((sourcePath != null) && (sourcePath.length > 0)) {
        try {
          EnvCommandline commandline = new EnvCommandline(sourcePath[0]);
          commandline.setWorkingDirectory(System.getProperty("user.dir"));

          getAuditLogger().info("Executing {}", commandline.toString());

          Object errorStreamLock = new Object();
          Object outputStreamLock = new Object();

          Process process = commandline.execute();
          synchronized (errorStreamLock) {
            synchronized (outputStreamLock) {
              // any output message ?
              RuntimeStreamReader outputStream = new RuntimeStreamReader(outputStreamLock, process.getInputStream(), RuntimeStreamReader.Type.output,
                  cmdOutListener);
              // any error message ?
              RuntimeStreamReader errorStream = new RuntimeStreamReader(errorStreamLock, process.getErrorStream(), RuntimeStreamReader.Type.error,
                  cmdErrListener);
              outputStream.start();
              errorStream.start();
              outputStreamLock.wait();
            }
            errorStreamLock.wait();
          }

          int exitValue = process.waitFor();
          if (exitValue != 0) {
            // this indicates an error exit from the executed command
            // then this actor generates an error msg with the exit value
            throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Exit : " + exitValue + " for command : " + sourcePath[0], this, msg, null);
          }
        } catch (ProcessingException e) {
          throw e;
        } catch (Exception e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Unable to execute command : " + sourcePath[0], this, msg, e);
        }
      }
    }
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();

    triggerConnected = trigger.getWidth() > 0;

    if (triggerConnected) {
      triggerHandler = createPortHandler(trigger);
      triggerHandler.start();
    }
  }

  protected boolean doPostFire() throws ProcessingException {
    boolean res = triggerConnected;
    if (res) {
      res = super.doPostFire();
    }
    return res;
  }

  /**
   * Receives lines from the out or err stream from a running process.
   */
  private class CommandOutputListener implements RuntimeStreamListener {
    private Port outputPort;

    public CommandOutputListener(Port outputPort) {
      this.outputPort = outputPort;
    }

    public void acceptLine(String newLine) {
      ManagedMessage message = createMessage();
      try {
        message.setBodyContentPlainText(newLine);
        sendOutputMsg(outputPort, message);
      } catch (MessageException e) {
        // should never happen...
        getLogger().error("Error setting msg content for " + newLine, e);
      } catch (Exception e) {
        getLogger().error("Error sending msg for " + newLine, e);
      }
    }
  }
}