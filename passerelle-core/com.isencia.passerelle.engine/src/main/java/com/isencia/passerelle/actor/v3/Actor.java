/*
 * (c) Copyright 2001-2007, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.actor.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleException.Severity;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;

/**
 * <p>
 * A new uniform Actor API that provides a better encapsulation of the specific treatment needed for the different types of port interactions (input/output,
 * pull/push, ...).
 * </p>
 * <p>
 * Actor implementations should now register any input ports with the desired mode:
 * <ul>
 * <li>PortMode.PULL : for blocking ports, where the actor will try to get a message, and remain blocked, waiting for a message to arrive.
 * <li>PortMode.PUSH : for non-blocking ports, where the actor is notified when a message arrives
 * </ul>
 * </p>
 * <p>
 * Secondly, an actor must no longer implement any (derivatives of) preFire/fire/postFire. Instead a single new method process(...) is provided. <br>
 * From the ProcessRequest parameter, the actor implementation can ask for the last message received on a given input port.
 * <ul>
 * <li>For PULL (blocking) ports, each process() invocation will have a new message available (as the actor blocks until a new message has arrived).
 * <li>For PUSH (non-blocking) ports, it's possible that no message is available for the port. In that case, the last previously received message is given, or
 * null if no message has ever been received on it.
 * </ul>
 * The actor can also obtain the iteration counter, for which the ProcessRequest has been constructed. <br>
 * If the actor wants to send outgoing messages, it should pass them with the desired output port into the ProcessResponse. <br>
 * The actor has the option to send several outgoing messages grouped in a sequence, or as individual, "unrelated" messages.
 * </p>
 * 
 * @author erwin.de.ley@isencia.be
 * 
 * @deprecated use com.isencia.passerelle.actor.v5.Actor as base-class instead.
 */
public abstract class Actor extends com.isencia.passerelle.actor.Actor {
  private final static Logger logger = LoggerFactory.getLogger(Actor.class);

  // a flag to indicate the special case of a source actor
  // as these do not have any input ports, so the std algorithm
  // to automatically deduce that the actor can requestFinish()
  // is not valid
  private boolean isSource = true;

  /**
   * The bridge between this actor and its ports that are configured in PUSH mode.
   * 
   * @author erwin
   */
  private static class MsgListener extends PortListenerAdapter {
    private Actor actor;

    private PortHandler handler;

    MsgListener(Actor actor, PortHandler handler) {
      this.actor = actor;
      this.handler = handler;
    }

    public void noMoreTokens() {
      int i = getMyIndex();
      actor.pushingInputFinishRequests.set(i, Boolean.TRUE);
    }

    public void tokenReceived() {
      Token selectToken = handler.getToken();
      try {
        ManagedMessage msg = MessageHelper.getMessageFromToken(selectToken);
        if (msg != null) {
          int i = getMyIndex();
          MessageInputContext ctxt = new MessageInputContext(i, handler.getName(), msg);
          actor.pushedMessages.put(handler.getName(), ctxt);
        }
      } catch (Exception e) {
        logger.error("", e);
      }
    }

    /**
     * @return the index of this listener (or its port) in the list of known pushing ports.
     */
    private int getMyIndex() {
      int i = actor.pushingInputHandlers.indexOf(this.handler);
      return i;
    }
  }

  // List of (blocking) handlers we need to poll each cycle
  // to check for new input messages.
  protected List<PortHandler> blockingInputHandlers;

  protected List<Boolean> blockingInputFinishRequests;

  // List of handlers that push msgs to registered listeners
  protected List<PortHandler> pushingInputHandlers;

  protected List<Boolean> pushingInputFinishRequests;

  // Map of messages that have been pushed to us.
  // Key is the name of the input port on which they arrived.
  protected Map<String, MessageInputContext> pushedMessages = new HashMap<String, MessageInputContext>();

  // Just a counter for the fire cycles.
  // We're using this to be able to show
  // for each input msg on which fire cycle
  // it arrived.
  private long iterationCount = 0;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Actor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    blockingInputHandlers = new ArrayList<PortHandler>();
    blockingInputFinishRequests = new ArrayList<Boolean>();
    pushingInputHandlers = new ArrayList<PortHandler>();
    pushingInputFinishRequests = new ArrayList<Boolean>();
  }

  @Override
  protected String getExtendedInfo() {
    return "";
  }

  /*
   * (non-Javadoc)
   * @see com.isencia.passerelle.actor.Actor#doInitialize()
   */
  @SuppressWarnings("unchecked")
  protected void doInitialize() throws InitializationException {
    if (logger.isTraceEnabled())
      logger.trace(getInfo() + " doInitialize() - entry");

    blockingInputHandlers.clear();
    blockingInputFinishRequests.clear();
    pushingInputHandlers.clear();
    pushingInputFinishRequests.clear();

    super.doInitialize();
    iterationCount = 0;
    isSource = true;

    List<Port> inputPortList = this.inputPortList();
    for (Port _p : inputPortList) {
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        if (_p.getMode().isBlocking()) {
          blockingInputHandlers.add(createPortHandler(_p));
          blockingInputFinishRequests.add(Boolean.FALSE);
        } else {
          PortHandler pH = createPortHandler(_p);
          MsgListener msgListener = new MsgListener(this, pH);
          pH.setListener(msgListener);
          pushingInputHandlers.add(pH);
          pushingInputFinishRequests.add(Boolean.FALSE);
        }
        isSource = false;
      }
    }

    for (int i = 0; i < blockingInputHandlers.size(); ++i) {
      PortHandler h = blockingInputHandlers.get(i);
      if (h.getWidth() > 0) {
        blockingInputFinishRequests.set(i, Boolean.FALSE);
        h.start();
      } else {
        blockingInputFinishRequests.set(i, Boolean.TRUE);
      }
    }
    for (int i = 0; i < pushingInputHandlers.size(); ++i) {
      PortHandler h = pushingInputHandlers.get(i);
      if (h.getWidth() > 0) {
        pushingInputFinishRequests.set(i, Boolean.FALSE);
        h.start();
      } else {
        pushingInputFinishRequests.set(i, Boolean.TRUE);
      }
    }

    if (logger.isTraceEnabled())
      logger.trace(getInfo() + " doInitialize() - exit ");
  }

  public long getIterationCount() {
    return iterationCount;
  }

  protected void doFire() throws ProcessingException {
    if (logger.isTraceEnabled())
      logger.trace(getInfo() + " doFire() - entry");

    ProcessRequest req = new ProcessRequest();
    req.setIterationCount(iterationCount++);

    if (!isSource) {
      // first read from all blocking inputs
      for (int i = 0; i < blockingInputHandlers.size(); i++) {
        PortHandler handler = blockingInputHandlers.get(i);
        ManagedMessage msg = null;
        // if a port is exhausted, we just pass a null msg to the
        // request
        // if not, we try to read another msg from it
        // a null msg indicates that the port is exhausted
        if (!blockingInputFinishRequests.get(i).booleanValue()) {
          try {
            msg = MessageHelper.getMessage(handler);
          } catch (ProcessingException e) {
            throw e;
          } catch (PasserelleException e) {
            throw new ProcessingException("", handler, e);
          }
          if (msg == null) {
            blockingInputFinishRequests.set(i, Boolean.TRUE);
            if (logger.isDebugEnabled())
              logger.debug(getInfo() + " doFire() - found exhausted port " + handler.getName());
          } else {
            if (logger.isDebugEnabled())
              logger.debug(getInfo() + " doFire() - msg " + msg.getID() + " received on port " + handler.getName());
          }
        }
        req.addInputMessage(i, handler.getName(), msg);
      }
      // then merge all pushed msgs
      // these may still contain 'old' data, i.e. msgs received a while
      // ago
      // and that are now repeated in another process request
      Collection<MessageInputContext> pushedMsgs = pushedMessages.values();
      for (MessageInputContext msgCtxt : pushedMsgs) {
        req.addInputContext(msgCtxt);
      }

      // when all ports are exhausted, we can stop this actor
      if (areAllInputsFinished()) {
        requestFinish();
      }
    }

    if (isSource || req.hasSomethingToProcess()) {
      ActorContext ctxt = new ActorContext();
      if (mustValidateIteration()) {
        try {
          if (logger.isTraceEnabled())
            logger.trace("doFire() - validating iteration for request " + req);
          validateIteration(ctxt, req);
          if (getAuditLogger().isDebugEnabled())
            getAuditLogger().debug("ITERATION VALIDATED");
          if (logger.isTraceEnabled())
            logger.trace("doFire() - validation done");
        } catch (ValidationException e) {
          try {
            getErrorControlStrategy().handleIterationValidationException(this, e);
          } catch (IllegalActionException e1) {
            // interpret this is a FATAL error
            throw new ProcessingException(Severity.FATAL, "", this, e);
          }
        }
      }

      // now let the actor do it's real work
      ProcessResponse response = new ProcessResponse(req);
      if (logger.isTraceEnabled())
        logger.trace("doFire() - processing request " + req);
      try {
        notifyStartingFireProcessing();
        process(ctxt, req, response);
      } finally {
        notifyFinishedFireProcessing();
      }
      if (logger.isTraceEnabled())
        logger.trace("doFire() - obtained response " + response);

      // Mark the contexts as processed.
      // This is especially important for the pushed inputs,
      // that are often offered multiple times for processing.
      // But some actors need to be able to distinguish whether
      // a pushed input is offered for the 1st time, or repeatedly.
      Iterator<MessageInputContext> allInputContexts = req.getAllInputContexts();
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
            throw new ProcessingException("Error creating output sequence msg for msg " + context.getMessage().getID(), context.getMessage(), e);
          }
        }
      }
    }

    if (logger.isTraceEnabled())
      logger.trace(getInfo() + " doFire() - exit ");
  }

  /**
   * @return
   */
  protected final boolean areAllInputsFinished() {
    boolean result = true;
    for (int i = 0; i < blockingInputFinishRequests.size(); ++i) {
      result = result && blockingInputFinishRequests.get(i).booleanValue();
    }
    for (int i = 0; i < pushingInputFinishRequests.size(); ++i) {
      result = result && pushingInputFinishRequests.get(i).booleanValue();
    }
    return result;
  }

  /**
   * @param ctxt
   * @param request
   * @param response
   * @throws ProcessingException
   */
  protected abstract void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException;

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
    actor.blockingInputFinishRequests = new ArrayList<Boolean>();
    actor.pushingInputHandlers = new ArrayList<PortHandler>();
    actor.pushingInputFinishRequests = new ArrayList<Boolean>();
    actor.pushedMessages = new HashMap<String, MessageInputContext>();
    return actor;
  }
}
