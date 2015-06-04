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
package com.isencia.passerelle.process.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.director.PasserelleDirector;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageOutputContext;
import com.isencia.passerelle.message.MessageProvider;
import com.isencia.passerelle.message.MessageQueue;
import com.isencia.passerelle.message.SimpleActorMessageQueue;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.SettableMessage;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * Continuing on the track started with the "v3" and "v5" Actor APIs, the process-context-aware API offers further
 * enhancements in Actor development features, combined with runtime enhancements.
 * <p>
 * Similar to the v5 Actor API, a process-actor hides the complexity of push/pull port management, threading etc. But
 * whereas plain actors only care about the receipt of any arbitrary msg on a given port, a process-actor knows about
 * specific request processing contexts to which incoming message may be related.
 * </p>
 * <p>
 * In "streaming"-mode model runs, where actors may be receiving many consecutive messages on their input ports, during
 * one run, such process-context-aware actors must be able to handle mixed ordering of received messages. <br/>
 * Practically, this implies that actors with multiple data-input-ports must contain a kind of internal buffering for
 * received messages. Based on a definition of mandatory and optional inputs (using the default approach of
 * blocking/non-blocking a.k.a. pull/push port modes), message may need to be kept waiting a while until all required
 * messages have been received for a same context. <br/>
 * Only then should the <code>process(...)</code> method be invoked with a <code>ProcessRequest</code> containing all
 * the related messages.
 * </p>
 * 
 * @author erwin
 */

public abstract class Actor extends com.isencia.passerelle.actor.Actor implements ProcessActor, MessageBuffer {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(Actor.class);

  // Just a counter for the fire cycles.
  // We're using this to be able to show for each input msg on which fire cycle
  // it arrived.
  private long iterationCount = 0;

  // A flag to indicate the special case of a source actor.
  // As these do not have any input ports, so the std algorithm
  // to automatically deduce that the actor can requestFinish() is not valid.
  private boolean isSource = true;

  // Collection of msg providers, i.e. typically receivers on input ports
  // that directly feed their received msgs into this MessageBuffer's queue.
  private Collection<Object> msgProviders = new HashSet<Object>();

  // Queue of messages that have been pushed to us, incl info on the input port
  // on which
  // they have been received.
  private MessageQueue pushedMessages;
  // lock to manage blocking on empty pushedMessages queue and to synchronize
  // concurrent access
  private ReentrantLock msgQLock = new ReentrantLock();
  private Condition msgQNonEmpty = msgQLock.newCondition();

  // These collections maintain current processing containers between
  // prefire/fire/postfire.
  // ========================================================================================

  // TODO evaluate if these should not be managed centrally, e.g. attached to
  // the <code>Director</code> or so, linked to the event queue i.c.o. an
  // <code>ETDirector</code> etc

  // This map contains all process requests for which some data has been
  // received,
  // but still not all required data. They are mapped to their context ID (in
  // string format, as stored in the msg header).
  private Map<String, ProcessRequest> incompleteProcessRequests = new ConcurrentHashMap<String, ProcessRequest>();
  // This queue contains prepared response containers for the process requests
  // that have received all required inputs for their context and are waiting to
  // be processed.
  // When the actor's processing resources have a free slot, they look in this
  // queue for new work.
  private Queue<ProcessResponse> pendingProcessRequests = new LinkedBlockingQueue<ProcessResponse>();
  // This queue contains finished work, ready to be inspected for messages that
  // must be sent out etc.
  private Queue<ProcessResponse> finishedProcessResponses = new LinkedBlockingQueue<ProcessResponse>();

  // Parameter to specify an optional buffer time between actor processing iterations.
  // This can be useful for streaming-mode executions, where actors may be able
  // to optimize their work when they can process many msgs/events in one shot, i.o. one-by-one.
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

    // At this level we don't use such queue capacity settings anymore.
    // If queues with limited capacity are needed, our CAP domain Director must be used.
    // So we drop these parameters here.
    receiverQueueCapacityParam.setContainer(null);
    receiverQueueWarningSizeParam.setContainer(null);
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (attribute.getName().equals("Receiver Q Capacity (-1)") || attribute.getName().equals("Receiver Q warning size (-1)")) {
      try {
        attribute.setContainer(null);
      } catch (NameDuplicationException e) {
      }
      return;
    }
    super.attributeChanged(attribute);
  }

  @Override
  protected String getExtendedInfo() {
    return "";
  }

  public MessageQueue getMessageQueue() {
    return pushedMessages;
  }

  protected MessageQueue newMessageQueue() throws InitializationException {
    MessageQueue result = null;
    Director d = getDirector();
    if (d instanceof PasserelleDirector) {
      result = ((PasserelleDirector) d).newMessageQueue(this);
    } else {
      result = new SimpleActorMessageQueue(this);
    }
    return result;
  }

  /**
   * Process-aware actors that must support out-of-order multiple inputs for their processing contexts, require
   * non-blocking input ports. This is implemented via the <code>MessageBuffer</code> & <code>MessageProvider</code>
   * system.
   * <p>
   * Via this method call, <code>Port</code>s sniff around a bit on their <code>Actor</code> to check if it wants to act
   * as a <code>MessageBuffer</code> for the <code>Port</code>. So for process-aware actors, this call will return
   * <code>true</code> for all data input ports that belong to this actor instance.
   * </p>
   */
  public boolean acceptInputPort(Port p) {
    if (p == null || p.getContainer() != this) {
      return false;
    }
    if (p instanceof ControlPort || !p.isInput()) {
      return false;
    }
    return true;
  }

  /**
   * Registers a <code>MessageProvider</code>, so the actor knows there's someone out there that may feed messages to
   * it. This is important to allow the actor to determine when it can wrap-up.
   */
  public boolean registerMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Registered msgprovider {}", getFullName(), provider);
    return msgProviders.add(provider);
  }

  /**
   * Unregisters a <code>MessageProvider</code>, so the actor knows that it should not expect anything anymore from this
   * one. This is important to allow the actor to determine when it can wrap-up.
   */
  public boolean unregisterMessageProvider(MessageProvider provider) {
    getLogger().debug("{} - Unregistered msgprovider {}", getFullName(), provider);
    return msgProviders.remove(provider);
  }

  /**
   * This method is called each time a message is received on receivers of the actor's input ports, for the ports that
   * have been accepted via <code>acceptInputPort()</code>.
   * <p>
   * In this way the ports/receivers are able to push messages directly into the common msg queue of the actor, i.o.
   * forcing the actor to try to get its input messages from a number of <code>BlockingQueue</code>s, one for each
   * blocking input port.
   * </p>
   */
  public void offer(MessageInputContext ctxt) throws PasserelleException {
    getLogger().debug("{} - offer {}", getFullName(), ctxt);
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged, so
        // refuse the task
        throw new Exception("Msg Queue lock overcharged for " + getFullName());
      }
      pushedMessages.put(ctxt);
      msgQNonEmpty.signal();
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.MSG_DELIVERY_FAILURE, "Error storing received msg", this, ctxt.getMsg(), e);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
    }
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    super.doPreInitialize();
    // need to do this here, as in thread-based models
    // the doInitialize could cause race conditions with preceeding actors
    // that already started sending msgs!
    iterationCount = 1;
    if (pushedMessages != null) {
      pushedMessages.clear();
    } else {
      pushedMessages = newMessageQueue();
    }
    incompleteProcessRequests.clear();
    pendingProcessRequests.clear();
    finishedProcessResponses.clear();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void doInitialize() throws InitializationException {
    getLogger().trace("{} - doInitialize() - entry", getFullName());
    super.doInitialize();

    List<Port> inputPortList = this.inputPortList();
    for (Port _p : inputPortList) {
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        isSource = false;
        break;
      }
    }

    try {
      triggerFirstIteration();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error triggering a fire iteration for source actor " + getFullName(), this, e);
    }
    getLogger().trace("{} - doInitialize() - exit", getFullName());
  }

  /**
   * Checks if any messages have been received since the previous iteration. If so, tries to aggregate them per
   * <code>Context</code>. If a <code>ProcessRequest</code> has a complete set of messages, it is stored in a
   * "pending-for-processing" queue.
   */
  @Override
  protected boolean doPreFire() throws ProcessingException {
    getLogger().trace("{} - doPreFire() - entry", getFullName());

    boolean readyToFire = super.doPreFire();
    if (!isSource) {
      if (hasPushedMessages() || (readyToFire && !msgProviders.isEmpty())) {
        try {
          int bufferTime = ((IntToken) bufferTimeParameter.getToken()).intValue();
          if (bufferTime > 0) {
            getLogger().debug("{} - doPreFire() - sleeping for buffer time {}", getFullName(), bufferTime);
            Thread.sleep(bufferTime);
          }
        } catch (Exception e) {
          getLogger().warn(getFullName() + " - Failed to enforce buffer time", e);
        }
        // we need to check all pushed msgs and group them according to their
        // contexts
        aggregatePushedMessages();
      }
      readyToFire = !pendingProcessRequests.isEmpty();
      // when all ports are exhausted, and no messages have arrived in the
      // meantime, we can stop this actor
      if (!readyToFire && areAllInputsFinished() && !hasPushedMessages()) {
        requestFinish();
      }
    } else {
      // For sources, we feed in an empty <code>ProcessRequest</code> anyway, with a process manager obtained from
      // somewhere...
      // It's up to the <code>process()</code> implementation to define when the source must finish and wrap up.
      ProcessRequest currentProcessRequest = new ProcessRequest(getNonMessageBoundProcessManager());
      currentProcessRequest.setIterationCount(iterationCount);
      pendingProcessRequests.add(new ProcessResponse(currentProcessRequest));
    }
    getLogger().trace("{} - doPreFire() - exit : {}", getFullName(), readyToFire);
    return readyToFire;
  }

  /**
   * This method should be overridden by source actors to obtain/construct a valid ProcessManager.
   * 
   * @return a ProcessManager for the actor's running process, but not bound to a specific received message. <br/>
   *         <b>Can be null!</b>
   * 
   * @throws ProcessingException
   *           if something goes wrong in obtaining a ProcessManager
   */
  protected ProcessManager getNonMessageBoundProcessManager() throws ProcessingException {
    NamedObj flow = toplevel();
    ProcessManager processManager = null;
    try {
      StringParameter procMgrParameter = (StringParameter) flow.getAttribute(ProcessRequest.HEADER_PROCESS_ID, StringParameter.class);
      if (procMgrParameter != null) {
        processManager = ProcessManagerServiceTracker.getService().getProcessManager(procMgrParameter.stringValue());
        if (processManager != null) {
          return processManager;
        }
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error obtaining ProcessManager", this, e);
    }
    throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error obtaining ProcessManager", this, null);
  }

  /**
   * Pop a pending process request from the queue, if any, and process it.
   */
  @Override
  protected void doFire() throws ProcessingException {
    getLogger().trace("{} - doFire() - entry", getFullName());
    if (!pendingProcessRequests.isEmpty()) {
      ProcessResponse currentProcessResponse = pendingProcessRequests.poll();
      ProcessRequest currentProcessRequest = currentProcessResponse.getRequest();
      ProcessManager processManager = currentProcessRequest.getProcessManager();

      getLogger().trace("{} - doFire() - processing request {}", getFullName(), currentProcessRequest);
      if (mustValidateIteration()) {
        try {
          getLogger().trace("{} - doFire() - validating iteration for request {}", getFullName(), currentProcessRequest);
          validateIteration(currentProcessRequest);
          getAuditLogger().debug("ITERATION VALIDATED");
          getLogger().trace("{} - doFire() - validation done for request {}", getFullName(), currentProcessRequest);
        } catch (ValidationException e) {
          try {
            getErrorControlStrategy().handleIterationValidationException(this, e);
          } catch (IllegalActionException e1) {
            // a validation error is a dramatic event, and when even its handling fails, we jump out of the normal
            // actor's processing asap
            throw new ProcessingException(ErrorCode.ERROR_PROCESSING_FAILURE, "Error reporting iteration validation error", this, e);
          }
        }
      }
      try {
        getDirectorAdapter().notifyActorStartedTask(this, currentProcessRequest);
        notifyStartingFireProcessing();
        process(processManager, currentProcessRequest, currentProcessResponse);
        if (ProcessingMode.SYNCHRONOUS.equals(getProcessingMode(currentProcessRequest))) {
          processFinished(processManager, currentProcessRequest, currentProcessResponse);
        }
      } catch (ProcessingException ex) {
        ExecutionTracerService.trace(this, ex.getMessage());
        // TODO add some way to log errors on the processManager for non-TaskBasedActors
        currentProcessResponse.setException(ex);
        processFinished(processManager, currentProcessRequest, currentProcessResponse);
      } catch (Throwable t) {
        ExecutionTracerService.trace(this, t.getMessage());
        // TODO add some way to log errors on the processManager for non-TaskBasedActors
        currentProcessResponse.setException(new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error processing task", this, t));
        processFinished(processManager, currentProcessRequest, currentProcessResponse);
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

    boolean result = super.doPostFire();
    if (!result) {
      // check if we don't have asynch work ongoing
      result = getDirectorAdapter().isActorBusy(this);
    }
    if (result) {
      iterationCount++;
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
   * Overridable method that triggers a first iteration, from inside the actor initialization. Default implementation
   * calls <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input
   * ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerFirstIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Overridable method that triggers a next iteration, after each actor's previous iteration. Default implementation
   * calls <code>Director.fireAtCurrentTime(this)</code> when the actor is a source. (i.e. has no connected data input
   * ports)
   * 
   * @throws IllegalActionException
   */
  protected void triggerNextIteration() throws IllegalActionException {
    if (isSource) {
      getDirector().fireAtCurrentTime(this);
    }
  }

  /**
   * Check out all msgs pushed to this actor, and group them according to their context. If a ProcessRequest is found
   * with all required inputs filled in, add it to the pending queue.
   * 
   * @throws ProcessingException
   */
  protected void aggregatePushedMessages() throws ProcessingException {
    getLogger().trace("{} - checkPushedMessages() - entry", getFullName());
    int msgCtr = 0;
    try {
      if (!msgQLock.tryLock(10, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged,
        // so refuse the task
        throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock overcharged...", this, null);
      }
      // Contrary to plain v5 Actors, we only want to handle one
      // MessageInputContext per iteration.
      // So no while loop here!
      if (!pushedMessages.isEmpty()) {
        MessageInputContext msgInputCtxt = pushedMessages.poll();
        Iterator<ManagedMessage> msgIterator = msgInputCtxt.getMsgIterator();
        while (msgIterator.hasNext()) {
          ManagedMessage managedMessage = (ManagedMessage) msgIterator.next();
          String[] ctxtHdrs = ((MessageContainer) managedMessage).getHeader(ProcessRequest.HEADER_PROCESS_ID);
          if (ctxtHdrs != null && ctxtHdrs.length > 0) {
            for (String ctxtHdr : ctxtHdrs) {
              ProcessRequest processRequest = incompleteProcessRequests.get(ctxtHdr);
              if (processRequest == null) {
                ProcessManager processManager = ProcessManagerServiceTracker.getService().getProcessManager(ctxtHdr);
                if (processManager != null) {
                  processRequest = new ProcessRequest(processManager);
                  processRequest.setIterationCount(iterationCount);
                  incompleteProcessRequests.put(ctxtHdr, processRequest);
                } else {
                  throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error obtaining ProcessManager for processID " + ctxtHdr, this, managedMessage, null);
                }
              }
              if (processRequest != null) {
                processRequest.addInputMessage(msgInputCtxt.getPortIndex(), msgInputCtxt.getPortName(), managedMessage);
              }
            }
          } else {
            ProcessManager processManager = getNonMessageBoundProcessManager();
            if (processManager != null) {
              ProcessRequest processRequest = new ProcessRequest(processManager);
              processRequest.setIterationCount(iterationCount);
              incompleteProcessRequests.put(processManager.getId(), processRequest);
            } else {
              throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error obtaining ProcessManager", this, managedMessage, null);
            }
          }
          msgCtr++;
        }
      }

      // now check for any completely-defined context-scoped process requests
      Collection<Entry<String, ProcessRequest>> transfers = new ArrayList<Entry<String, ProcessRequest>>();
      for (Entry<String, ProcessRequest> prEntry : incompleteProcessRequests.entrySet()) {
        if (prEntry.getValue().hasSomethingToProcess()) {
          transfers.add(prEntry);
        }
      }
      for (Entry<String, ProcessRequest> entry : transfers) {
        pendingProcessRequests.offer(new ProcessResponse(entry.getValue()));
        incompleteProcessRequests.remove(entry.getKey());
      }
    } catch (InterruptedException e) {
      throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock interrupted...", this, null);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error preparing process requests", this, e);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
      getLogger().trace("{} - checkPushedMessages() - exit - added {}", getFullName(), msgCtr);
    }
  }

  protected ProcessManager getProcessManager(Request request) {
    return ProcessManagerServiceTracker.getService().getProcessManager(request.getProcessingContext().getProcessId());
  }

  /**
   * Does a check on the size of the <code>pushedMessages</code> queue, protected with a <code>msgLock</code>
   * 
   * @return true if this actor currently has msgs in its <code>pushedMessages</code> queue.
   * @throws ProcessingException
   *           if the access to the queue fails, e.g. when the lock is not available within a reasonable time
   */
  protected boolean hasPushedMessages() throws ProcessingException {
    getLogger().trace("{} - hasPushedMessages() - entry", getFullName());
    boolean result = false;
    try {
      if (!msgQLock.tryLock(1, TimeUnit.SECONDS)) {
        // if we did not get the lock, something is getting overcharged,
        // so refuse the task
        throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock overcharged...", this, null);
      }
      result = !pushedMessages.isEmpty();
    } catch (InterruptedException e) {
      throw new ProcessingException(ErrorCode.RUNTIME_PERFORMANCE_INFO, "Msg Queue lock interrupted...", this, null);
    } finally {
      try {
        msgQLock.unlock();
      } catch (Exception e) {
      }
      getLogger().trace("{} - hasPushedMessages() - exit - {}", getFullName(), result);
    }
    return result;
  }

  /**
   * @return true when all input ports are exhausted
   */
  protected boolean areAllInputsFinished() {
    return msgProviders.isEmpty();
  }

  public ProcessingMode getProcessingMode(ProcessRequest request) {
    return ProcessingMode.SYNCHRONOUS;
  }

  public void processFinished(ProcessManager processManager, ProcessRequest request, ProcessResponse response) {
    try {
      if (response.getException() == null) {
        // Mark the contexts as processed.
        // Not sure if this is still relevant for v5 actors,
        // as even PUSHed messages are assumed to be handled once, in the
        // iteration when they are offered to process().
        Iterator<MessageInputContext> allInputContexts = request.getAllInputContexts();
        while (allInputContexts.hasNext()) {
          MessageInputContext msgInputCtxt = allInputContexts.next();
          msgInputCtxt.setProcessed(true);
        }

        // and now send out the results
        MessageOutputContext[] outputs = response.getOutputs();
        if (outputs != null) {
          for (MessageOutputContext output : outputs) {
            SettableMessage message = (SettableMessage) output.getMessage();
            message.setHeader(ProcessRequest.HEADER_PROCESS_ID, processManager.getId());
            sendOutputMsg(output.getPort(), message);
          }
        }
        outputs = response.getOutputsInSequence();
        if (outputs != null && outputs.length > 0) {
          Long seqID = MessageFactory.getInstance().createSequenceID();
          for (int i = 0; i < outputs.length; i++) {
            MessageOutputContext context = outputs[i];
            boolean isLastMsg = (i == (outputs.length - 1));
            try {
              SettableMessage msgInSeq = (SettableMessage) MessageFactory.getInstance().createMessageCopyInSequence(context.getMessage(), seqID, new Long(i), isLastMsg);
              msgInSeq.setHeader(ProcessRequest.HEADER_PROCESS_ID, processManager.getId());
              sendOutputMsg(context.getPort(), msgInSeq);
            } catch (MessageException e) {
              throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output sequence msg for msg " + context.getMessage().getID(), this,
                  context.getMessage(), e);
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
   * Method that should be overridden for actors that need to be able to validate their state before processing a next
   * fire-iteration.
   * </p>
   * <p>
   * E.g. it can typically be used to validate dynamic parameter settings, and/or messages received on their input
   * ports.
   * </p>
   * 
   * @param request
   *          contains all messages received on the actor's input ports for the current iteration.
   * @throws ValidationException
   */
  protected void validateIteration(ProcessRequest request) throws ValidationException {
  }

  /**
   * Overridable method to determine if an actor should do a validation of its state and incoming request for each
   * iteration. <br>
   * By default, checks on its Passerelle director what must be done. If no Passerelle director is used (but e.g. a
   * plain Ptolemy one), it returns false.
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
    try {
      actor.pushedMessages = actor.newMessageQueue();
    } catch (InitializationException e) {
      throw new RuntimeException("Failed to create new message queue for cloned actor " + actor.getFullName(), e);
    }
    actor.msgProviders = new HashSet<Object>();
    actor.incompleteProcessRequests = new ConcurrentHashMap<String, ProcessRequest>();
    actor.pendingProcessRequests = new LinkedBlockingQueue<ProcessResponse>();
    actor.finishedProcessResponses = new LinkedBlockingQueue<ProcessResponse>();
    actor.msgQLock = new ReentrantLock();
    actor.msgQNonEmpty = actor.msgQLock.newCondition();
    return actor;
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      Context processContext = ProcessRequest.getContextForMessage(message);
      return port.getFullName() + " - msg for request " + processContext.getRequest().getId();
    } catch (Exception e) {
      return super.getAuditTrailMessage(message, port);
    }
  }
}
