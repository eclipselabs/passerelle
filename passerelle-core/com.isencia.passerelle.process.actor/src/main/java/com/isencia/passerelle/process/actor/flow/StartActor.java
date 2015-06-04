package com.isencia.passerelle.process.actor.flow;

import java.util.Map;

import org.slf4j.MDC;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * Actor to generate a simple Request that mainly can be used as trigger for basic diagnostic sequences.
 * 
 * @author erwin
 * 
 */
public class StartActor extends Actor {

  private static final long serialVersionUID = 1L;

  public static final String APPLICATION_PARAMETERS = "Application parameters";
  public static final String MOCK_REQUEST = "Mock Request";

  public Port trigger; // NOSONAR
  public Port output; // NOSONAR

  public StringParameter applicationParameters;

  public StartActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);
    applicationParameters = new StringParameter(this, APPLICATION_PARAMETERS);
    new TextStyle(applicationParameters, "paramsTextArea");
  }

  @Override
  public void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      Map<String, String> systemParameterMap = FlowUtils.getParameterMap(toplevel(), FlowUtils.SYSTEM_PARAMETERS);
      preProcess(processManager, systemParameterMap);
      Context processingContext = processManager.getRequest().getProcessingContext();
      if (!Status.STARTED.equals(processingContext.getStatus()) && !Status.RESTARTED.equals(processingContext.getStatus())) {
        processManager.notifyStarted();
      }
      response.addOutputMessage(output, createMessage());
    } catch (Exception t) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating request", this, t);
    } finally {
      // should terminate after one request generation
      requestFinish();
    }
  }

  /**
   * Overridable method to apply extra logic on the processing context at start time.
   * 
   * @param processManager
   * @param systemParameterMap
   */
  protected void preProcess(ProcessManager processManager, Map<String, String> systemParameterMap) {
    MDC.put("requestId", processManager.getRequest().getId().toString());
  }

}
