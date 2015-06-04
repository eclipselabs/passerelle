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
package com.isencia.passerelle.actor.net;

/**
 * 
 * @author		erwin
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.ISenderChannel;
import com.isencia.message.generator.IMessageGenerator;
import com.isencia.message.net.SocketClientSenderChannel;
import com.isencia.passerelle.actor.ChannelSink;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.gui.IOptionsFactory.Option;
import com.isencia.passerelle.core.ErrorCode;

public class SocketClientSender extends ChannelSink {
  private static final long serialVersionUID = 1L;
  private static Logger LOGGER = LoggerFactory.getLogger(SocketClientSender.class);
  final static String MSG_GENERATOR_PARAM_NAME = "Msg End";

  protected int port = 3333;
  protected String host = "localhost";

  public Parameter portParam;
  public Parameter hostParam;
  public Parameter msgGeneratorType;

  /**
   * SocketClientSender.
   * 
   * @param container
   *          ptolemy.kernel.CompositeEntity
   * @param name
   *          java.lang.String
   * @exception ptolemy.kernel.util.IllegalActionException
   *              The exception description.
   * @exception ptolemy.kernel.util.NameDuplicationException
   *              The exception description.
   */
  public SocketClientSender(ptolemy.kernel.CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    portParam = new Parameter(this, "port", new IntToken(getPort()));
    portParam.setTypeEquals(BaseType.INT);
    hostParam = new StringParameter(this, "remote host");
    hostParam.setExpression(getHost());
    msgGeneratorType = new StringParameter(this, MSG_GENERATOR_PARAM_NAME);
    new SocketCltSndOptionsFactory(this, OPTIONS_FACTORY_CFG_NAME).setOptionsForParameter(msgGeneratorType);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * @param attribute
   *          The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == portParam) {
      IntToken portToken = (IntToken) portParam.getToken();
      if (portToken != null && portToken.intValue() > 0) {
        port = portToken.intValue();
      }
    } else if (attribute == hostParam) {
      StringToken hostToken = (StringToken) hostParam.getToken();
      if (hostToken != null && hostToken.stringValue().length() > 0) {
        host = hostToken.stringValue();
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  /**
   * @return Returns a String
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host
   *          The host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return Returns a int
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port
   *          The port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  protected ISenderChannel createChannel() throws InitializationException {
    IMessageGenerator generator = getGeneratorFromSelectedOption();
    return new SocketClientSenderChannel(getHost(), getPort(), generator);
  }

  private IMessageGenerator getGeneratorFromSelectedOption() throws InitializationException {
    IMessageGenerator generator = null;
    // we wait as long as possible before checking
    // the options factory settings, so don't do it
    // in the constructor or attributeChanged or...
    try {
      if (getOptionsFactory() == null) {
        setOptionsFactory(new SocketSvrRcvOptionsFactory(this, OPTIONS_FACTORY_CFG_NAME));
      }
      Option o = getOptionsFactory().getOption(msgGeneratorType, msgGeneratorType.getExpression());
      if (o == null) {
        o = getOptionsFactory().getDefaultOption(msgGeneratorType);
      }
      generator = ((IMessageGenerator) o.getAssociatedObject()).cloneGenerator();
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error setting Parameter options factory", this, e);
    }

    return generator;
  }

}