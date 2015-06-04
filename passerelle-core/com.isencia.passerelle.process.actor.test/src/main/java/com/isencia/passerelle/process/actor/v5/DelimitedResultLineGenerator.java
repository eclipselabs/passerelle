/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.actor.v5;

import java.util.Arrays;

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
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * This actor allows to select a configurable set of result items from a received processing Context, and to generate a
 * line concatenating their values, separated by a configurable delimiter and ordered in the same order as their
 * configured names.
 * <p>
 * Reserved names and their meaning are :
 * <ul>
 * <li>refID : the context's case's ID (a.k.a. reference ID)</li>
 * <li>requestID : the context's request ID</li>
 * <li>processType : the context's request's process type</li>
 * </ul>
 * </p>
 * 
 * @author erwin
 */
public class DelimitedResultLineGenerator extends Actor {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(DelimitedResultLineGenerator.class);

  private static final String REFID_NAME = "refID";
  private static final String REQUESTID_NAME = "requestID";
  private static final String PROCESSTYPE_NAME = "processType";

  public Port input;
  public Port output;

  public StringParameter delimiterParameter;
  private String delimiter = ";";
  public StringParameter resultNamesParameter;
  private String[] resultNames = new String[] { "NA", "refID" };

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DelimitedResultLineGenerator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    delimiterParameter = new StringParameter(this, "delimiter");
    delimiterParameter.setExpression(delimiter);

    String resultNamesStr = Arrays.toString(resultNames);
    resultNamesParameter = new StringParameter(this, "result item names");
    // chop leading [ and trailing ]
    resultNamesParameter.setExpression(resultNamesStr.substring(1, resultNamesStr.length() - 1));

    registerConfigurableParameter(delimiterParameter);
    registerConfigurableParameter(resultNamesParameter);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      delimiter = ((StringToken)delimiterParameter.getToken()).stringValue();
      String resultNamesDef = ((StringToken)resultNamesParameter.getToken()).stringValue();
      resultNames = resultNamesDef.split(",");
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading parameter(s)", this, e);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {

    ManagedMessage message = request.getMessage(input);
    if (message == null) {
      return;
    }

    try {
      Context processContext = null;
      if (message.getBodyContent() instanceof Context) {
        processContext = (Context) message.getBodyContent();
      } else {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No context present in msg", this, message, null);
      }

      StringBuilder buffer = new StringBuilder();
      for (String resultName : resultNames) {
        if (REFID_NAME.equalsIgnoreCase(resultName)) {
          buffer.append(processContext.getRequest().getCase().getId() + delimiter);
        } else if (REQUESTID_NAME.equalsIgnoreCase(resultName)) {
          buffer.append(processContext.getRequest().getId() + delimiter);
        } else if (PROCESSTYPE_NAME.equalsIgnoreCase(resultName)) {
          buffer.append(processContext.getRequest().getType() + delimiter);
        } else {
          String resultValue = processContext.lookupValue(resultName);
          buffer.append(resultValue + delimiter);
        }
      }

      String msg = buffer.toString();

      ExecutionTracerService.trace(this, msg);

      ManagedMessage resultMsg = createMessageFromCauses(message);
      resultMsg.setBodyContent(msg, "text/plain");
      response.addOutputMessage(output, resultMsg);

    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error constructing delimited result line", this, message, e);
    }
  }

}
