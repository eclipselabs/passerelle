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
package com.isencia.passerelle.process.actor.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.FlowUtils;
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
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.actor.activator.Activator;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.process.model.util.ProcessModelUtils;
import com.isencia.passerelle.project.repository.api.Project;
import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * An actor that starts another flow and allows to parameterise it and to forward the current process context to it.
 * <p>
 * Parameterisation can be done by selecting results of the current process that can then be injected in the new flow,
 * both as model parameters (in the Passerelle/Ptolemy sense) and as process request parameters.
 * Model parameters only get injected values when they are present on the parsed destination flow.
 * I.e. the flow designer should define the parameterisation "interface" a-priori by setting the supported model parameters,
 * and then referring to their values wherever needed, typically using Ptolemy's expression language convention like <code>$modelParamName</code>.
 * </p>
 * <p>
 * Multiple Parameters can be defined on separate lines, each with following format :
 * <ul>
 * <li> <code>dest-paramname=value</code> : inject the given value as-is into the selected flow's parameter with the given name (and set it as request parameter)</li> 
 * <li> <code>dest-paramname=#[value-item-name]</code> : inject the value of the item with given name, found in the current proces context, into the selected flow's parameter with the given name (and set it as request parameter)</li> 
 * </ul>
 * E.g. :<br/>
 * <code>device-name=motor1<br/>operation-name=#[OPERATION]</code><br/>
 * will look for parameters on the new flow with names <code>device-name</code> and <code>operation-name</code>.
 * If found, <code>device-name</code> will be set to <code>motor1</code>, and <code>operation-name</code> 
 * will be set to the value found for the item in the current process context, with name <code>OPERATION</code>.
 * </p>
 * 
 * @author erwin
 *
 */
public class Forward extends Actor {
  private static final long serialVersionUID = -123598665541852281L;

  private final static Logger LOGGER = LoggerFactory.getLogger(Forward.class);

  public static final String PROJECT_CODE = "Project code";
  public static final String SEQUENCE_CODE = "Sequence code";
  public static final String PARAMETERS = "Parameters";

  public Port input; // NOSONAR
  public Port output; // NOSONAR
  public StringParameter projectCodeParameter; // NOSONAR
  public StringParameter flowCodeParameter; // NOSONAR
  public StringParameter parameterParameter; // NOSONAR

  public Forward(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
    projectCodeParameter = new StringParameter(this, PROJECT_CODE);
    flowCodeParameter = new StringParameter(this, SEQUENCE_CODE);
    parameterParameter = new StringParameter(this, PARAMETERS);
    new TextStyle(parameterParameter, "paramsTextArea");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    if (message != null) {
      try {
        Context processContext = (Context) message.getBodyContent();

        RepositoryService repoSvc = getRepositoryService();
        Project project = null;
        Flow flow = null;

        String flowCode = ((StringToken) flowCodeParameter.getToken()).stringValue();
        String projectCode = ((StringToken) projectCodeParameter.getToken()).stringValue();

        if (StringUtils.isNotEmpty(projectCode)) {
          project = repoSvc.getProject(projectCode);
        }
        if (project != null) {
          flow = project.getFlow(flowCode);
        } else {
          flow = repoSvc.getFlow(flowCode);
        }

        if (flow != null) {
          // TODO make this a method on Flow
          flow = setUniqueName(flow);
          // TODO find another way to mark a redirection
          // as it is more a matter of the request/context than of the flow itself.
          flow = setRedirected(flow);
          
          // forward this actor's process context to the new flow process
          Parameter p = new Parameter(flow, "context", new ObjectToken(processContext));
          p.setPersistent(false);

          Map<String, String> parameterOverrides = applyParameters(processContext, flow);
          FlowManager.getDefault().executeBlockingLocally(flow, parameterOverrides);
        }
        response.addOutputMessage(output, message);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to forward to ", this, message, e);
      }
    }
  }

  private Map<String, String> applyParameters(Context processContext, Flow flow) throws IllegalActionException, IOException {
    Map<String, String> parameterOverrides = new HashMap<String, String>();
    String paramDefs = ((StringToken) parameterParameter.getToken()).stringValue();
    if (StringUtils.isNotEmpty(paramDefs)) {
      BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
      String paramDef = null;
      while ((paramDef = reader.readLine()) != null) {
        String[] paramKeyValue = paramDef.split("=");
        if (paramKeyValue.length == 2) {
          String paramName = paramKeyValue[0].trim();
          String paramValue = ProcessModelUtils.lookupValueForPlaceHolder(processContext, paramKeyValue[1].trim());
          try {
            // TODO When moving to trunk, and using new FlowProcessingService, this extra check is not needed anymore.
            // but on the edm v1.0 branch invalid parameter overrides generate exceptions!
            if(flow.getAttribute(paramName, Parameter.class) != null) {
              parameterOverrides.put(paramName, paramValue);
            }
            getEntityFactory().createAttribute(processContext.getRequest(), paramName, paramValue);
          } catch (Exception e) {
            // ignore, just means we keep on trying for the others
          }
        } 
        // else {just skip it}
      }
    }
    return parameterOverrides;
  }

  protected Flow setUniqueName(Flow flow) throws Exception {
    String flowName = FlowUtils.generateUniqueFlowName(flow.getName());
    flow.setName(flowName);
    flow.propagateValues();
    flow.propagateValue();
    return flow;
  }

  @SuppressWarnings("unchecked")
  protected Flow setRedirected(Flow flow) throws Exception {
    Map<String, String> systemParameterMap = FlowUtils.getParameterMap(flow, FlowUtils.SYSTEM_PARAMETERS);
    if(systemParameterMap!=null) {
        systemParameterMap.put(FlowUtils.REDIRECTED, "true");
    }
    return flow;
  }

  protected RepositoryService getRepositoryService() {
    return Activator.getDefault().getRepositoryService();
  }

  protected ProcessFactory getEntityFactory() {
    return ProcessFactoryTracker.getService();
  }
}
