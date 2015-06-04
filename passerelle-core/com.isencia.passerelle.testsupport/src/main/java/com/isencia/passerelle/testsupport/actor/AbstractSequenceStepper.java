package com.isencia.passerelle.testsupport.actor;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageInputContext;

/**
 * <p>
 * This actor buffers a sequence of steps that must be sent out one-by-one, each time after getting a trigger.
 * </p>
 * <p>
 * The values that must be sent out can be constructed in different ways, specific for a concrete implementation.
 * </p>
 * 
 * @author erwin
 */
public abstract class AbstractSequenceStepper extends Actor {

  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractSequenceStepper.class);

  public static final String START_PORT_NAME = "start";
  public static final String NEXT_PORT_NAME = "next";
  public static final String END_PORT_NAME = "end";
  public static final String OUTPUT_PORT_NAME = "output";

  // input ports
  public Port startPort;
  public Port nextPort;
  // output ports
  public Port outputPort;
  public Port endPort;

  private BlockingQueue<Step> stepQueue = new LinkedBlockingQueue<Step>();

  /**
   * @param container
   * @param name
   * @throws ptolemy.kernel.util.IllegalActionException
   * @throws ptolemy.kernel.util.NameDuplicationException
   */
  public AbstractSequenceStepper(final CompositeEntity container, final String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    startPort = PortFactory.getInstance().createInputPort(this, START_PORT_NAME, null);
    nextPort = PortFactory.getInstance().createInputPort(this, NEXT_PORT_NAME, PortMode.PUSH, null);
    endPort = PortFactory.getInstance().createOutputPort(this, END_PORT_NAME);
    outputPort = PortFactory.getInstance().createOutputPort(this, OUTPUT_PORT_NAME);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  
  // a temporary fix for usage with early Passerelle 8.3.x ; 
  // to resolve an issue with PortMode.equals that led to ParameterPorts being accepted as msg provider for a MessageBuffer
  @Override
  public boolean acceptInputPort(Port p) {
    return !(PortMode.AGNOSTIC == p.getMode()) && super.acceptInputPort(p);
  }
  
  @Override
  protected void doInitialize() throws InitializationException {
    stepQueue.clear();
    super.doInitialize();
  }

  /**
   * Intercept each "next" message while it's being pushed via the nextPort, so we can use this as indication that a next step message can be sent out.
   */
  @Override
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    if (nextPort.getName().equals(ctxt.getPortName())) {
      sendStepMessage();
    }
    super.offer(ctxt);
  }

  /**
   * React on each received start message, by generating a complete step sequence again, and store the steps in a queue.
   */
  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage inputMsg = request.getMessage(startPort);
    if (inputMsg != null) {
      getLogger().debug("{} - received start msg {}", getFullName(), inputMsg);
      boolean wasIdle = stepQueue.isEmpty();
      generateSteps(response);
      if (wasIdle) {
        // send out first msg of the loop
        sendStepMessage();
      }
    }
  }

  /**
   * As the loop actor manages a complete loop execution "in the background" for each received trigger msg, i.e. we do not block the process method during the
   * complete loop execution, this must be marked as "asynchronous" processing.
   */
  @Override
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    if (request.getMessage(startPort) != null) {
      return ProcessingMode.ASYNCHRONOUS;
    } else {
      return super.getProcessingMode(ctxt, request);
    }
  }

  /**
   * Generate all loop steps and queue them, followed by an "end-marker".
   * <p>
   * Steps must be added via <code>addStep()</code> and the last one for a given sequence should
   * be added via <code>addEndMarkerStep(response)</code>.
   * </p>
   * 
   * @param response
   */
  protected abstract void generateSteps(ProcessResponse response);
  
  /**
   * Clear the queue. This is typically done at/around actor initialization time.
   * But some implementations may expose options to clear the queue during a model execution as well.
   */
  protected void clearStepQueue() {
    stepQueue.clear();
  }
  
  /**
   * adds the given non-null Step to the step queue
   * @param step
   * @return true if the addition was done successfully, false otherwise
   */
  protected boolean addStep(Step step) {
    if(step!=null) {
      return stepQueue.offer(step);
    } else {
      return false;
    }
  }

  /**
   * adds a specific "end-marker" Step to the step queue, for the given response 
   * @param response the ProcessResponse (and linked request) for which an end-marker step must be added
   * @return true if the addition was done successfully, false otherwise
   */
  protected boolean addEndMarkerStep(ProcessResponse response) {
    if(response!=null) {
      return stepQueue.offer(Step.buildEndMarker(response));
    } else {
      return false;
    }
  }

  /**
   * @param inputMsg
   * @throws ProcessingException
   */
  private void sendStepMessage() throws ProcessingException {
    Step step = stepQueue.poll();
    if (step != null) {
      if (step.isEndMarker()) {
        // output end loop signal
        ProcessResponse response = step.response;
        processFinished(response.getContext(), response.getRequest(), response);
        sendOutputMsg(endPort, MessageFactory.getInstance().createTriggerMessage());
        if (stepQueue.isEmpty()) {
          if (!isFinishRequested() && startPort.getActiveSources().isEmpty()) {
            requestFinish();
          }
        } else {
          // continue with next loop that seems to be on the queue
          sendStepMessage();
        }
      } else if (isFinishRequested()) {
        if (getDirectorAdapter().isActorBusy(this)) {
          while (!stepQueue.isEmpty()) {
            // clear all busy work
            ProcessResponse response = stepQueue.poll().response;
            if (response != null)
              processFinished(response.getContext(), response.getRequest(), response);
          }
        }
      } else {
        final ManagedMessage resultMsg = createMessage();
        getLogger().trace("{} - sendStepMessage() - step {}", getFullName(), step.stepCtr);
        try {
          resultMsg.setBodyContent(step.stepValue, ManagedMessage.objectContentType);
          sendOutputMsg(outputPort, resultMsg);
        } catch (final MessageException e) {
          throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Cannot send message out", this, e);
        }
      }
    }
  }

  /**
   * Simple container structure to maintain all relevant info for each loop step that must be executed.
   */
  static class Step {
    private boolean endMarker;
    long stepCtr;
    Serializable stepValue;
    ProcessResponse response;

    public Step(long stepCtr, Serializable stepValue, ProcessResponse response) {
      this.stepCtr = stepCtr;
      this.stepValue = stepValue;
      this.response = response;
    }

    private Step() {
    }

    public boolean isEndMarker() {
      return endMarker;
    }

    public static Step buildEndMarker(ProcessResponse response) {
      Step s = new Step();
      s.endMarker = true;
      s.response = response;
      return s;
    }
  }
}
