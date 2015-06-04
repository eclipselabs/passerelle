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
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.impl.ProcessHandleImpl;
import com.isencia.passerelle.process.service.impl.ProcessManagerImpl;
import com.isencia.passerelle.runtime.ProcessHandle;

/**
 * @author erwin
 */
public class RequestSource extends Actor {
	private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestSource.class);

  public Port output;

  public StringParameter processTypeParameter;
  public StringParameter categoryParameter;
  public StringParameter initiatorParameter;
  public StringParameter corrIDParameter;
  public StringParameter extRefParameter;
  public StringParameter reqParamsParameter;

  public RequestSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);

    processTypeParameter = new StringParameter(this, "process type");
    processTypeParameter.setExpression("greet");
    categoryParameter = new StringParameter(this, "category");
    categoryParameter.setExpression("test");
    initiatorParameter = new StringParameter(this, "initiator");
    initiatorParameter.setExpression(getName());
    corrIDParameter = new StringParameter(this, "correlation ID");
    corrIDParameter.setExpression("123456");
    extRefParameter = new StringParameter(this, "external ref");
    extRefParameter.setExpression("call of this morning");
    reqParamsParameter = new StringParameter(this, "request parameters");
    reqParamsParameter.setExpression("hello=world\ngoodbye=moon");
    new TextStyle(reqParamsParameter, "textarea");

    _attachText(
        "_iconDescription",
        "<svg>\n<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:orange;stroke:orange\"/>\n<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n<circle cx=\"0\" cy=\"0\" r=\"10\"style=\"fill:white;stroke-width:2.0\"/>\n<line x1=\"-15\" y1=\"0\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n<line x1=\"-3\" y1=\"-3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n<line x1=\"-3\" y1=\"3\" x2=\"0\" y2=\"0\" style=\"stroke-width:2.0\"/>\n</svg>\n");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      ProcessFactory entityFactory = ProcessFactoryTracker.getService();
      String extRef = ((StringToken) extRefParameter.getToken()).stringValue();
      Case caze = entityFactory.createCase(extRef);
      String processType = ((StringToken) processTypeParameter.getToken()).stringValue();
      String category = ((StringToken) categoryParameter.getToken()).stringValue();
      String correlationID = ((StringToken) corrIDParameter.getToken()).stringValue();
      String initiator = ((StringToken) initiatorParameter.getToken()).stringValue();
      Request req = entityFactory.createRequest(caze, initiator, category, processType, correlationID);
      req.setExecutor(toplevel().getName());
      String paramDefs = reqParamsParameter.getExpression();
      BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
      String paramDef = null;
      while ((paramDef = reader.readLine()) != null) {
        String[] paramKeyValue = paramDef.split("=");
        if (paramKeyValue.length == 2) {
          entityFactory.createAttribute(req, paramKeyValue[0], paramKeyValue[1]);
        } else {
          getLogger().warn("Invalid mapping definition: " + paramDef);
        }
      }
      
      ProcessHandle handle = req.getId() == null ? new ProcessHandleImpl((Flow)toplevel()) : new ProcessHandleImpl(req.getId().toString(),(Flow)toplevel());
      ProcessManager procManager = new ProcessManagerImpl(ProcessManagerServiceTracker.getService(), handle, req);
      ProcessPersister persister = procManager.getPersister();
      boolean shouldClose = false; 
      try {
        shouldClose = persister.open(true);
        persister.persistCase(caze);
        persister.persistRequest(req);
        procManager.notifyStarted();
      } finally {
        if(shouldClose) {
          persister.close();
        }
      }
      
      ManagedMessage message = createMessage(req.getProcessingContext(), ManagedMessage.objectContentType);
      response.addOutputMessage(output, message);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error creating request", this, e);
    } finally {
      requestFinish();
    }
  }

}
