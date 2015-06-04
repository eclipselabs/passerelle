/* Copyright 2014 - iSencia Belgium NV

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
package com.isencia.passerelle.actor.v5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Director;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageOutputContext;
import com.isencia.passerelle.message.MessageProvider;
import com.isencia.passerelle.message.MessageQueue;
import com.isencia.passerelle.message.SimpleActorMessageQueue;

/**
 * <p>
 * Continuing on the track started with the "v3" Actor API, the v5 API offers further enhancements in Actor development features, combined with runtime
 * enhancements.
 * </p>
 * <p>
 * The v5 Actor solves the general issue of multi-input-port-actors in the underlying Ptolemy PN domain, where it is not possible to obtain flexible behaviour
 * for an actor with multiple "blocking" input ports.
 * <p>
 * With the "v3 actor api" of Passerelle, it is still the case that multiple "pull"/"blocking" input ports imply that each port must have received a message
 * before the actor will fire. <br/>
 * And when only "push"/"non-blocking" ports are used, the actor iteration loops continuously in an uncontrollable way.
 * </p>
 * <p>
 * Often it is required to have an actor with multiple inputs, where the actor should fire when a message was received on at least one of the inputs. <br/>
 * This is implemented here by adding an internal buffer/queue which is fed by the messages arriving at the input ports. A non-empty buffer will trigger an
 * actor iteration. The actor implementation must typically be ready to accept multiple input msgs in one shot. Such behaviour can be useful when
 * high-throughput msg streams must be processed in combination with non-negligible processing times, and i.c.o. possible optimizations when processing msg
 * batches in one shot.
 * </p>
 * <p>
 * Optionally one can set a configurable buffer time in ms. If this is set, the actor will wait at least this time before processing any received (pushed)
 * messages. When no messages have been received when the configured buffered time has passed, the actor keeps on waiting till the first received message.
 * Remark that even then, due to the asynchronous and concurrent behaviour, the processing could already see more than one received message when it is finally
 * triggered.
 * </p>
 * <p>
 * With this v5 Actor, it is in principle no longer relevant to have PULL input ports, although they are still supported. Remark that for PULL ports, each Actor
 * fire iteration can only handle 1 received message at a time, as the PN semantics do not allow to find out if more than 1 msg is available on a PULL port.
 * Consequently, also the concepts of buffer and of buffer time are not applicable for PULL ports. I.e. the buffer time only comes into play, in the presence of
 * active PUSH ports.
 * </p>
 * 
 * @author erwin
 */
@SuppressWarnings("serial")
public abstract class Actor extends com.isencia.passerelle.actor.Actor implements MessageBuffer {
  private final static Logger LOGGER = LoggerFactory.getLogger(Actor.class);

  /**
   * Identifies whether the actor will process a given ProcessRequest synchronously (all work done when the <code>process()</code> method returns) or
   * asynchronously (work done in background and will probably take longer than the return of the <code>process()</code> method invocation).
   */
  public enum ProcessingMode {
    SYNCHRONOUS, ASYNCHRONOUS;
  }

  // Just a counter for the fire cycles.
  // We're using this to be able to show for each input msg on which fire cycle it arrived.
  private long iterationCount = 0;

  // A flag to indicate the special case of a source actor.
  // As these do not have any input ports, so the std algorithm
  // to automatically deduce that the actor can requestFinish() is not valid.
  private boolean isSource = true;

  // Collection of msg providers, i.e. typically receivers on input ports
  // that directly feed their received msgs into this MessageBuffer's queue.
  private Collection<Object> msgProviders = new HashSet<Object>();

  // List of (blocking) handlers we need to poll each cycle
  // to check for new input messages.
  protected List<PortHandler> blockingInputHandlers = new ArrayList<PortHandler>();
  protected Map<Port, Boolean> blockingInputFinishRequests = new HashMap<Port, Boolean>();

  // Queue of messages that have been pushed to us, 
  // incl info on the input port on which
  // they have been received.
  private MessageQueue pushedMessages;

  // These track current processing containers between prefire/fire/postfire.
  // Storing processing state in this way can only work for actors that are iterating
  // in a single thread!!!!
  private ProcessRequest currentProcessRequest;
  private ProcessResponse currentProcessResponse;

  // parameter to specify an optionl buffer time between actor processing iterations.
  public Parameter bufferTimeParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Actor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    bufferTimeParameter = new Parameter(this, "Buffer time (ms)", new IntToken(0));
    registerExpertParameter(bufferTimeParameter);
  }

  @Override
  protected String getExtendedInfo() {
    return "";
  }

  public MessageQueue getMessageQueue() {
    return pushedMessages;
  }

  public boolean acceptInputPort(Port p) {
    if (p == null || p.getContainer() != this) {
      return false;
    }
    if (p instanceof ControlPort || !p.isInput()) {
      return false;
    }
    return PortMode.PUSH.equals(p.getMode());
  }

  public boolean registerMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Registered msgprovider {}", getFullName(), provider);
    return msgProviders.add(provider);
  }

  public boolean unregisterMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Unregistered msgprovider {}", getFullName(), provider);
    return msgProviders.remove(provider);
  }

  /**
   * This method is called from the actor's PUSH ports, each time they receive a message.
   */
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    getLogger().debug("{} - offer {}", getFullName(), ctxt);
    try {
      pushedMessages.put(ctxt);
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.MSG_DELIVERY_FAILURE, "Error storing received msg", this, ctxt.getMsg(), e);
    }
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    super.doPreInitialize();
    pushedMessages = newMessageQueue();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  protected void doInitialize() throws InitializationException {
    getLogger().trace("{} - doInitialize() - entry", getFullName());
    super.doInitialize();

    blockingInputHandlers.clear();
    blockingInputFinishRequests.clear();

    iterationCount = 0;
    currentProcessRequest = new ProcessRequest();
    currentProcessRequest.setIterationCount(++iterationCount);
    currentProcessResponse = null;

    List<Port> inputPortList = this.inputPortList();
    for (Port _p : inputPortList) {
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        if (_p.isBlocking()) {
          blockingInputHandlers.add(createPortHandler(_p));
          blockingInputFinishRequests.put(_p, Boolean.FALSE);
        }
        isSource = false;
      }
    }
    for (int i = 0; i < blockingInputHandlers.size(); ++i) {
      PortHandler h = blockingInputHandlers.get(i);
      if (h.getWidth() > 0) {
        blockingInputFinishRequests.put(h.getPort(), Boolean.FALSE);
        h.start();
      } else {
        blockingInputFinishRequests.put(h.getPort(), Boolean.TRUE);
      }
    }

    try {
      triggerFirstIteration();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error triggering a fire iteration for source actor " + getFullName(), this, e);
    }

    getLogger().trace("{} - doInitialize() - exit", getFullName());
  }

  protected MessageQueue newMessageQueue() throws InitializationException {
    MessageQueue result = null;
    Director d = getDirector();
    if(d instanceof PasserelleDirector) {
      result = ((PasserelleDirector)d).newMessageQueue(this);
    } else {
      result = new SimpleActorMessageQueue(this);
    }
    return result;
  }

  @Override
  protected boolean doPreFire() throws ProcessingException {
    getLogger().trace("{} - doPreFire() - entry  ", getFullName());
    boolean readyToFire = super.doPreFire();
		if (readyToFire && !isSource) {
      // first read from all blocking inputs
      for (int i = 0; i < blockingInputHandlers.size(); i++) {
        PortHandler handler = blockingInputHandlers.get(i);
        Port _p = (Port) handler.getPort();
        ManagedMessage msg = null;
        // If a port is exhausted, we just pass a null msg to the request.
        // If not, we try to read another msg from it.
        // A null msg indicates that the port is exhausted.
        if (!blockingInputFinishRequests.get(_p).booleanValue()) {
          // For the moment, we only read at most one msg per PULL input port.
          // Remark that for an event-driven domain, it is possible that preFire()
          // is invoked repetitively before a fire() is possible.
          // Msg streams should be handled via PUSH ports.
          if (currentProcessRequest.getMessage(_p) == null) {
            boolean portHasMsg = false;
            boolean portExhausted = false;
            try {
              Token token = handler.getToken();
              portHasMsg = (token != null) && (token != Token.NIL);
              portExhausted = (token == null);
              if (portHasMsg) {
                msg = MessageHelper.getMessageFromToken(token);
                currentProcessRequest.addInputMessage(0, _p.getName(), msg);
              } else {
                // all blocking/PULL ports must have received a message before we can fire
                readyToFire = false;
              }
            } catch (ProcessingException e) {
              throw e;
            } catch (PasserelleException e) {
              throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Error getting message from input", _p, e);
            }
            if (portExhausted) {
              blockingInputFinishRequests.put(handler.getPort(), Boolean.TRUE);
              getLogger().debug("{} - doPreFire() - found exhausted port {} ", getFullName(), handler.getName());
            } else if (msg != null) {
              if (getLogger().isDebugEnabled())
                getLogger().debug("{} - doPreFire() - message {} received on port {}", new Object[] { getFullName(), msg.getID(), handler.getName() });
            }
          }
        }
      }
      if (readyToFire && (!pushedMessages.isEmpty() || !msgProviders.isEmpty())) {
        try {
          // TODO check if it's not nicer to maintain buffer time from 1st preFire() call
          // to when readyToFire, i.o. adding it after the time we've already been waiting
          // for all PULL ports having a message.
          int bufferTime = ((IntToken) bufferTimeParameter.getToken()).intValue();
          if (bufferTime > 0) {
            getLogger().debug("{} - doPreFire() - sleeping for buffer time {}", getFullName(), bufferTime);
            Thread.sleep(bufferTime);
          }
        } catch (Exception e) {
          getLogger().warn(getFullName() + " - Failed to enforce buffer time", e);
        }
        // we've got at least one PUSH port that registered a msg provider
        // so we need to include all pushed msgs in the request as well
        addPushedMessages(currentProcessRequest);
      }
      readyToFire = readyToFire && currentProcessRequest.hasSomethingToProcess();
      // when all ports are exhausted, we can stop this actor
      if (!readyToFire && !getDirectorAdapter().isActorBusy(this) && areAllInputsFinished() && pushedMessages.isEmpty()) {
        requestFinish();
        readyToFire = true;
      }
    }
    getLogger().trace("{} - doPreFire() - exit : {}", getFullName(), readyToFire);
    return readyToFire;
  }

  @Override
  protected void doFire() throws ProcessingException {
    getLogger().trace("{} - doFire() - entry", getFullName());
    if (isSource || currentProcessRequest.hasSomethingToProcess()) {
      getLogger().trace("{} - doFire() - processing request {}", getFullName(), currentProcessRequest);
      ActorContext ctxt = new ActorContext();
      if (mustValidateIteration()) {
        try {
          getLogger().trace("{} - doFire() - validating iteration for request {}", getFullName(), currentProcessRequest);
          validateIteration(ctxt, currentProcessRequest);
          getAuditLogger().debug("ITERATION VALIDATED");
          getLogger().trace("{} - doFire() - validation done for request {}", getFullName(), currentProcessRequest);
        } catch (ValidationException e) {
          try {
            getErrorControlStrategy().handleIterationValidationException(this, e);
          } catch (IllegalActionException e1) {
            // interpret this is a FATAL error
            throw new ProcessingException(ErrorCode.ERROR_PROCESSING_FAILURE, "Error reporting iteration validation error", this, e);
          }
        }
      }
      currentProcessResponse = new ProcessResponse(ctxt, currentProcessRequest);
      try {
        getDirectorAdapter().notifyActorStartedTask(this, currentProcessRequest);
        notifyStartingFireProcessing();
        process(ctxt, currentProcessRequest, currentProcessResponse);
      } finally {
        notifyFinishedFireProcessing();
      }

      getLogger().trace("{} - doFire() - obtained response {}", getFullName(), currentProcessResponse);
    }
    getLogger().trace("{} - doFire() - exit", getFullName());
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    getLogger().trace("{} - doPostFire() - entry", getFullName());

    if (currentProcessResponse != null
        && ProcessingMode.SYNCHRONOUS.equals(getProcessingMode(currentProcessResponse.getContext(), currentProcessResponse.getRequest()))) {
      processFinished(currentProcessResponse.getContext(), currentProcessResponse.getRequest(), currentProcessResponse);
    }

    currentProcessResponse = null;

    boolean result = super.doPostFire();
    if (!result) {
      // check if we don't have asynch work ongoing
      result = getDirectorAdapter().isActorBusy(this);
    }
    if (result) {
      // create new proc req for next iteration
      iterationCount++;
      currentProcessRequest = new ProcessRequest();
      currentProcessRequest.setIterationCount(iterationCount);

      try {
        triggerNextIteration();
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Error triggering a fire iteration for source actor " + getFullName(), this, e);
      }
    }

    getLogger().trace("{} - doPostFire() - exit : {}", getFullName(), result);
    return result;
  }

  /**
   * Overridable method that triggers a first iteration, from inside the actor initialization. Default implementation calls
   * <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerFirstIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Overridable method that triggers a next iteration, after each actor's previous iteration. Default implementation calls
   * <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerNextIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Overridable method to allow custom collecting and addition of received pushed input messages. By default, this just reads whatever's received (possibly
   * nothing). But alternatively, this could e.g. block till at least one pushed msg was received etc. (check e.g. BufferedInputsActor).
   * 
   * @param req
   * @throws ProcessingException
   */
  protected void addPushedMessages(ProcessRequest req) throws ProcessingException {
    getLogger().trace("{} - addPushedMessages() - entry", getFullName());
    int msgCtr = 0;
    try {
      while (!pushedMessages.isEmpty()) {
        req.addInputContext(pushedMessages.poll());
        msgCtr++;
      }
    } catch (InterruptedException e) {
      throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock interrupted...", this, null);
    } finally {
      getLogger().trace("{} - addPushedMessages() - exit - added {}", getFullName(), msgCtr);
    }
  }

  /**
   * @return true when all input ports are exhausted
   */
  protected boolean areAllInputsFinished() {
    boolean result = true;
    Collection<Boolean> portFinishIndicators = blockingInputFinishRequests.values();
    for (Boolean portFinishedIndicator : portFinishIndicators) {
      result = result && portFinishedIndicator;
    }
    return result && msgProviders.isEmpty();
  }

  /**
   * Overridable method to indicate whether the given request will be processed synchronously or asynchronously. Default implementation indicates synchronous
   * processing for all requests.
   * <p>
   * Actors that have asynchronous processing, should combine returning <code>ProcessingMode.ASYNCHRONOUS</code> here, with invoking
   * <code>processFinished(ActorContext ctxt, ProcessRequest request, ProcessResponse response)</code> when the work is done for a given request.
   * </p>
   * 
   * @param ctxt
   * @param request
   * @return whether the given request will be processed synchronously or asynchronously.
   */
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.SYNCHRONOUS;
  }

  /**
   * @param ctxt the context in which the request must be processed
   * @param request the request that must be processed
   * @param response after processing this should contain the output messages that the actor should send, or a ProcessingException if some error was encountered
   *          during processing. (However, normally, for synchronous processing, exceptions will just be thrown.)
   * @throws ProcessingException
   */
  protected abstract void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException;

  /**
   * @param ctxt the context in which the request was processed
   * @param request the request that was processed
   * @param response contains the output messages that the actor should send, or a ProcessingException if some error was encountered during processing.
   */
  protected void processFinished(ActorContext ctxt, ProcessRequest request, ProcessResponse response) {
    try {
      if (response.getException() == null) {
        // Mark the contexts as processed.
        // Not sure if this is still relevant for v5 actors,
        // as even PUSHed messages are assumed to be handled once, in the iteration when they are offered to process().
        Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        while (allInputContexts.hasNext()) {
          MessageInputContext msgInputCtxt = allInputContexts.next();
          msgInputCtxt.setProcessed(true);
        }

        // and now send out the results
        MessageOutputContext[] outputs = response.getOutputs();
        if (outputs != null) {
          for (MessageOutputContext output : outputs) {
            sendOutputMsg(output.getPort(), output.getMessage());
          }
        }
        outputs = response.getOutputsInSequence();
        if (outputs != null && outputs.length > 0) {
          Long seqID = MessageFactory.getInstance().createSequenceID();
          for (int i = 0; i < outputs.length; i++) {
            MessageOutputContext context = outputs[i];
            boolean isLastMsg = (i == (outputs.length - 1));
            try {
              ManagedMessage msgInSeq = MessageFactory.getInstance().createMessageCopyInSequence(context.getMessage(), seqID, new Long(i), isLastMsg);
              sendOutputMsg(context.getPort(), msgInSeq);
            } catch (MessageException e) {
              throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output sequence msg for msg " + context.getMessage().getID(), this, context.getMessage(), e);
            }
          }
        }
      } else {
        throw response.getException();
      }
    } catch (ProcessingException e) {
      try {
        getErrorControlStrategy().handleFireException(this, e);
      } catch (IllegalActionException e1) {
        getLogger().error("Error handling exception ", e);
      }
    } finally {
      getDirectorAdapter().notifyActorFinishedTask(this, response.getRequest());
    }
  }

  /**
   * <p>
   * Method that should be overridden for actors that need to be able to validate their state before processing a next fire-iteration.
   * </p>
   * <p>
   * E.g. it can typically be used to validate dynamic parameter settings, and/or messages received on their input ports.
   * </p>
   * 
   * @param ctxt
   * @param request contains all messages received on the actor's input ports for the current iteration.
   * @throws ValidationException
   */
  protected void validateIteration(ActorContext ctxt, ProcessRequest request) throws ValidationException {
  }

  /**
   * Overridable method to determine if an actor should do a validation of its state and incoming request for each iteration. <br>
   * By default, checks on its Passerelle director what must be done. If no Passerelle director is used (but e.g. a plain Ptolemy one), it returns false.
   * 
   * @see validateIteration()
   * @see doFire()
   * @return
   */
  protected boolean mustValidateIteration() {
    try {
      return getDirectorAdapter().mustValidateIteration();
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    final Actor actor = (Actor) super.clone(workspace);
    actor.blockingInputHandlers = new ArrayList<PortHandler>();
    actor.blockingInputFinishRequests = new HashMap<Port, Boolean>();
    try {
      actor.pushedMessages = actor.newMessageQueue();
    } catch (InitializationException e) {
      throw new RuntimeException("Failed to create new message queue for cloned actor "+actor.getFullName(), e);
    }
    actor.msgProviders = new HashSet<Object>();
    return actor;
  }

  protected Logger getLogger() {
    return LOGGER;
  }
}
