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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
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
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.impl.ProcessManagerImpl;

/**
 * A batch-oriented source actor generating a lot of <code>Requests</code> in a sequence of Passerelle messages. It
 * creates the requests in the parent <code>Case</code> as present in a parent <code>Request</code> as received on the
 * input port.
 * <p>
 * The requests are initialized with default parameter values from a definition matrix in
 * <code>requestParamValuesParameter</code>, where each row can contain a list of comma-separated values for the
 * parameters defined in <code>requestParamNamesParameter</code>. <br/>
 * When the parent <code>Request</code> contains a value-list for a given parameter name however, this one is used. I.e.
 * specific value lists can be set on the parent <code>Request</code>, overriding the default ones configured in this
 * actor.
 * </p>
 * <p>
 * For the format :<br/>
 * E.g. i.c.o. a single parameter "NA" in <code>requestParamNamesParameter</code>,
 * <code>requestParamValuesParameter</code> could contain one long line of hundreds of comma-separated NAs.
 * </p>
 * This layout is chosen as in the web and RCP editors, long multi-line text areas are difficult to use...
 * 
 * @author erwin
 */
public class BatchRequestSequenceSource extends Actor {
  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(BatchRequestSequenceSource.class);

  public Port input;
  public Port output;

  public StringParameter processTypeParameter;
  public StringParameter categoryParameter;
  public StringParameter initiatorParameter;

  public Parameter requestThrottleIntervalParameter;

  public StringParameter requestParamNamesParameter;
  public StringParameter requestParamValuesParameter;

  private Request parentRequest;
  private Queue<Map<String, String>> requestAttrsQueue = new LinkedBlockingQueue<Map<String, String>>();
  private Long seqID;
  private Long seqPos;
  private Long throttleInterval = 5000L;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public BatchRequestSequenceSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, PortMode.PUSH, null);
    output = PortFactory.getInstance().createOutputPort(this);

    requestThrottleIntervalParameter = new Parameter(this, "throttle interval (s)", new IntToken(5));
    processTypeParameter = new StringParameter(this, "processType");
    processTypeParameter.setExpression("CustExpTrial");
    categoryParameter = new StringParameter(this, "category");
    categoryParameter.setExpression("Customer Experience");
    initiatorParameter = new StringParameter(this, "initiator");
    initiatorParameter.setExpression(getName());

    // the names and values should be specified as comma-separated strings
    requestParamNamesParameter = new StringParameter(this, "Request parameter names");
    requestParamNamesParameter.setExpression("NA");
    requestParamValuesParameter = new StringParameter(this, "Request parameter values");
    requestParamValuesParameter.setExpression("021234567,027654321");
    new TextStyle(requestParamValuesParameter, "paramsTextArea");

    _attachText(
        "_iconDescription",
        "<svg>\n<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:orange;stroke:orange\"/>\n<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n<circle cx=\"0\" cy=\"0\" r=\"10\"style=\"fill:white;stroke-width:2.0\"/>\n<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      parentRequest = null;
      requestAttrsQueue.clear();
      seqPos = 0L;
      seqID = MessageFactory.getInstance().createSequenceID();

      throttleInterval = ((IntToken) requestThrottleIntervalParameter.getToken()).longValue() * 1000;
      throttleInterval = (throttleInterval < 1000L) ? 1000L : throttleInterval;
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading actor parameters", this, e);
    }
  }

  public void offer(com.isencia.passerelle.message.MessageInputContext ctxt) throws com.isencia.passerelle.core.PasserelleException {
    if ((parentRequest == null) && (input.getName().equals(ctxt.getPortName()))) {
      ManagedMessage message = ctxt.getMsg();
      if (message == null) {
        return;
      }
      try {
        if (message.getBodyContent() instanceof Context) {
          Context processContext = (Context) message.getBodyContent();
          parentRequest = processContext.getRequest();
          setReqAttrMaps(parentRequest);
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Invalid message content, no context found", this, message, e);
      }
    }
    super.offer(ctxt);
  }

  protected void setReqAttrMaps(Request parentRequest) throws InitializationException, IOException {
    String[] paramNames = requestParamNamesParameter.getExpression().split(",");
    String paramValueDefs = requestParamValuesParameter.getExpression();

    List<Map<String, String>> requestAttributesList = null;

    BufferedReader reader = new BufferedReader(new StringReader(paramValueDefs));
    int paramNr = paramNames.length;
    // For each parameter name, read a line from the param values text area
    // but also check if the parent request does not contain an overridden value list.
    // Then check the length of the value list and, if all's well,
    // set the values for each corresponding request attribute map.
    for (int i = 0; i < paramNr; ++i) {
      String paramName = paramNames[i].trim();
      String paramDef = reader.readLine();
      if (paramDef == null) {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Missing values for parameter " + paramName + " and next ones", this, null);
      }
      Attribute overriddenParamDef = parentRequest.getAttribute(paramName);
      String[] paramValues = null;
      if (overriddenParamDef == null) {
        paramValues = paramDef.split(",");
      } else {
        paramValues = overriddenParamDef.getValueAsString().split(",");
      }
      if (requestAttributesList == null) {
        requestAttributesList = new ArrayList<Map<String, String>>(paramValues.length);
        for (int j = 0; j < paramValues.length; ++j) {
          requestAttributesList.add(new HashMap<String, String>());
        }
      }
      if (requestAttributesList.size() == paramValues.length) {
        for (int j = 0; j < paramValues.length; ++j) {
          Map<String, String> requestAttributes = requestAttributesList.get(j);
          String paramValue = paramValues[j].trim();
          requestAttributes.put(paramName, paramValue);
        }
      } else {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Incompatible parameter value count for " + paramName + ". Expected "
            + requestAttributesList.size() + " but was " + paramValues.length, this, null);
      }
    }

    for (Map<String, String> requestAttributes : requestAttributesList) {
      requestAttrsQueue.offer(requestAttributes);
    }
    if (requestAttrsQueue.isEmpty()) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "No requests generated at all", this, null);
    }

  }

  /**
   * return true once the parentRequest has been set
   */
  @Override
  protected boolean doPreFire() throws ProcessingException {
    super.doPreFire();
    return parentRequest != null;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest processRequest, ProcessResponse processResponse) throws ProcessingException {
    ManagedMessage message = processRequest.getMessage(input);
    try {
      Context processContext = null;
      if (message.getBodyContent() instanceof Context) {
        processContext = (Context) message.getBodyContent();
      } else {
        throw new ProcessingException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "No context present in msg", this, message, null);
      }
      Request request = processContext.getRequest();
      ProcessManager processManager = ProcessManagerServiceTracker.getService().getProcessManager(request);
      if (requestAttrsQueue.isEmpty()) {
        processManager.notifyEvent("Batch generation done", null);
        requestFinish();
      } else {
        ProcessFactory entityFactory = processManager.getFactory();
        Request req = null;
        Map<String, String> requestAttributes = requestAttrsQueue.poll();
        try {
          String processType = ((StringToken) processTypeParameter.getToken()).stringValue();
          String initiator = ((StringToken) initiatorParameter.getToken()).stringValue();

          req = entityFactory.createRequest(parentRequest.getCase(), initiator, parentRequest.getCategory(), processType, parentRequest.getCorrelationId());
          for (Entry<String, String> reqAttr : requestAttributes.entrySet()) {
            entityFactory.createAttribute(req, reqAttr.getKey(), reqAttr.getValue());
          }
          processManager.notifyEvent("Batch generated request", Long.toString(req.getId()));
          new ProcessManagerImpl(ProcessManagerServiceTracker.getService(), req);
          ProcessPersister persister = processManager.getPersister();
          boolean shouldClose = false;
          try {
            shouldClose = persister.open(true);
            persister.persistRequest(req);
          } finally {
            if (shouldClose) {
              persister.close();
            }
          }
        } catch (Exception e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to persist request " + requestAttributes, this, e);
        }
        try {
          boolean isSeqEnd = requestAttrsQueue.size() == 0;
          ManagedMessage outputMessage = MessageFactory.getInstance().createMessageInSequence(seqID, seqPos++, isSeqEnd, getStandardMessageHeaders());
          outputMessage.setBodyContent(req.getProcessingContext(), ManagedMessage.objectContentType);
          processResponse.addOutputMessage(output, outputMessage);
          // trigger ourselves again
          offer(new MessageInputContext(0, input.getName(), outputMessage));
        } catch (Exception e) {
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to send request " + req.getId(), this, e);
        } finally {
          try {
            Thread.sleep(throttleInterval);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error in actor processing", this, e);
    }
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    return super.doPostFire() || !(requestAttrsQueue.isEmpty());
  }

  protected void triggerNextIteration() throws IllegalActionException {
    getDirector().fireAtCurrentTime(this);
  }
}
