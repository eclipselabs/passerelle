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
package com.isencia.passerelle.actor.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.ftp.FtpSenderChannel;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.passerelle.actor.ChannelSink;

/**
 * This actor writes all received msgs to a file on an ftp-server.
 * 
 * @author Bram 
 */
public class FtpWriter extends ChannelSink {
  private static final long serialVersionUID = 1L;

  private static Logger LOGGER = LoggerFactory.getLogger(FtpWriter.class);

  // Parameters
  public Parameter serverParam = null;
  private String server = null;
  private static final String SERVER_PARAM = "Server";
  public Parameter userParam = null;
  private String user = null;
  private static final String USER_PARAM = "User";
  public Parameter passwordParam = null;
  private String password = null;
  private static final String PASSWORD_PARAM = "Password";
  public Parameter isBinaryTransferParam = null;
  private boolean isBinaryTransfer = false;
  private static final String IS_BINARY_TRANSFER_PARAM = "Binary";
  public Parameter isPassiveModeParam = null;
  private boolean isPassiveMode = true;
  private static final String IS_PASSIVE_MODE_PARAM = "Passive Mode";
  public Parameter fileParam = null;
  private String file = null;
  private static final String FILE_PARAM = "File to write";

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
  public FtpWriter(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // INIT PARAMETERS
    serverParam = new Parameter(this, SERVER_PARAM, new StringToken(""));
    serverParam.setTypeEquals(BaseType.STRING);
    userParam = new Parameter(this, USER_PARAM, new StringToken(""));
    userParam.setTypeEquals(BaseType.STRING);
    passwordParam = new Parameter(this, PASSWORD_PARAM, new StringToken(""));
    passwordParam.setTypeEquals(BaseType.STRING);
    isPassiveModeParam = new Parameter(this, IS_PASSIVE_MODE_PARAM, new BooleanToken(isPassiveMode));
    isPassiveModeParam.setTypeEquals(BaseType.BOOLEAN);

    isBinaryTransferParam = new Parameter(this, IS_BINARY_TRANSFER_PARAM, new BooleanToken(isBinaryTransfer));
    isBinaryTransferParam.setTypeEquals(BaseType.BOOLEAN);
    registerConfigurableParameter(isBinaryTransferParam);
    fileParam = new Parameter(this, FILE_PARAM, new StringToken(""));
    fileParam.setTypeEquals(BaseType.STRING);
    registerConfigurableParameter(fileParam);

    // CREATE IMAGE
    _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" width=\"40\" " + "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:white\"/>\n"
        + "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" "
        + "style=\"stroke-width:1.0;stroke:black\"/>\n" + "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n"
        + "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " + "style=\"stroke-width:1.0;stroke:grey\"/>\n" + "<circle cx=\"-2\" cy=\"-7\" r=\"4\""
        + "style=\"fill:black\"/>\n" + "<line x1=\"-15\" y1=\"-5\" x2=\"15\" y2=\"-5\" " + "style=\"stroke-width:2.0\"/>\n"
        + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"-15\" " + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"0\" y1=\"-5\" x2=\"15\" y2=\"5\" "
        + "style=\"stroke-width:2.0\"/>\n" + "<line x1=\"-15\" y1=\"10\" x2=\"0\" y2=\"10\" " + "style=\"stroke-width:1.0;stroke:gray\"/>\n"
        + "<line x1=\"0\" y1=\"10\" x2=\"0\" y2=\"-5\" " + "style=\"stroke-width:1.0;stroke:gray\"/>\n" + "</svg>\n");
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == serverParam) {
      StringToken aToken = (StringToken) serverParam.getToken();
      if ((aToken != null) && (aToken.stringValue().length() > 0)) {
        server = aToken.stringValue();
        getLogger().debug("{} Server changed to {}", getFullName(), server);
      }
    } else if (attribute == userParam) {
      StringToken aToken = (StringToken) userParam.getToken();
      if ((aToken != null) && (aToken.stringValue().length() > 0)) {
        user = aToken.stringValue();
        getLogger().debug("{} User changed to {}", getFullName(), user);
      }
    } else if (attribute == passwordParam) {
      StringToken aToken = (StringToken) passwordParam.getToken();
      if ((aToken != null) && (aToken.stringValue().length() > 0)) {
        password = aToken.stringValue();
        getLogger().debug("{} Password changed to {}", getFullName(), password);
      }
    } else if (attribute == isBinaryTransferParam) {
      BooleanToken aToken = (BooleanToken) isBinaryTransferParam.getToken();
      if (aToken != null) {
        isBinaryTransfer = aToken.booleanValue();
        getLogger().debug("{} Binary transfer changed to {}", getFullName(), isBinaryTransfer);
      }
    } else if (attribute == isPassiveModeParam) {
      BooleanToken aToken = (BooleanToken) isPassiveModeParam.getToken();
      if (aToken != null) {
        isPassiveMode = aToken.booleanValue();
        getLogger().debug("{} Passive mode changed to {}", getFullName(), isPassiveMode);
      }
    } else if (attribute == fileParam) {
      StringToken aToken = (StringToken) fileParam.getToken();
      if ((aToken != null) && (aToken.stringValue().length() > 0)) {
        file = aToken.stringValue();
        getLogger().debug("{} File changed to {}", getFullName(), file);
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected ISenderChannel createChannel() {
    ISenderChannel res = null;
    res = new FtpSenderChannel(file, server, user, password, isBinaryTransfer, isPassiveMode, new MessageTextLineGenerator());
    return res;
  }
}
