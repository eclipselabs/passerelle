/* Copyright 2012 - iSencia Belgium NV

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
package com.isencia.passerelle.testsupport.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

@SuppressWarnings("serial")
public class TextSource extends Actor {
  private final static Logger LOGGER = LoggerFactory.getLogger(TextSource.class);

  public Port output;
  public StringParameter textParameter;
  public String[] messageContents;

  public TextSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    textParameter = new StringParameter(this, "values");
    textParameter.setExpression("Hello,Goodbye");
    registerConfigurableParameter(textParameter);
  }
  
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      String tokenMessage = ((StringToken) textParameter.getToken()).stringValue();
      messageContents = tokenMessage.split(",");
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading configured msg contents", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      long count = request.getIterationCount();
      if(count<=messageContents.length) {
        String tokenMessage = messageContents[(int)count-1];
        ManagedMessage outputMsg = createMessage();
        outputMsg.setBodyContentPlainText(tokenMessage);
        response.addOutputMessage(output, outputMsg);
      }
      // By already checking here if we've reached the end of the msg contents list,
      // we can prevent a next actor cycle that would not generate any more outputs anyway
      // in the conditional block above.
      if(count>=messageContents.length) {
        requestFinish();
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error constructing message from text parameter", textParameter, e);
    }
  }
}
