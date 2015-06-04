/**
 * 
 */
package com.isencia.passerelle.actor.advanced;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.MessageInputContext;
import com.isencia.passerelle.actor.v3.MessageOutputContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.MessageHelper;

/**
 * @author erwin
 */
public abstract class AdvancedActor extends Actor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(AdvancedActor.class);

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
    private AdvancedActor actor;

    private PortHandler handler;

    MsgListener(AdvancedActor actor, PortHandler handler) {
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
        int i = getMyIndex();
        MessageInputContext ctxt = new MessageInputContext(i, handler.getName(), msg);
        actor.pushedMessages.put(handler.getName(), ctxt);
      } catch (Exception e) {
        LOGGER.error("", e);
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

  // little hack to solve actor construction sequencing issues
  private boolean handlerToolsConstructed;

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
  public AdvancedActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void _addPort(ptolemy.kernel.Port port) throws IllegalActionException, NameDuplicationException {
    if (!(this.portList().contains(port))) {
      super._addPort(port);
    }
    checkAndSetPortManagementTools();
    if (port instanceof Port) {
      Port _p = (Port) port;
      if (_p.isInput() && !(_p instanceof ControlPort)) {
        if (PortMode.PUSH.equals(_p.getMode())) {
          PortHandler pH = createPortHandler(_p);
          MsgListener msgListener = new MsgListener(this, pH);
          pH.setListener(msgListener);
          pushingInputHandlers.add(pH);
          pushingInputFinishRequests.add(Boolean.FALSE);
        } else {
          blockingInputHandlers.add(createPortHandler(_p));
          blockingInputFinishRequests.add(Boolean.FALSE);
        }
        isSource = false;
      }
    }
  }

  private void checkAndSetPortManagementTools() {
    if (!handlerToolsConstructed) {
      blockingInputHandlers = new ArrayList<PortHandler>();
      blockingInputFinishRequests = new ArrayList<Boolean>();
      pushingInputHandlers = new ArrayList<PortHandler>();
      pushingInputFinishRequests = new ArrayList<Boolean>();
      handlerToolsConstructed = true;
    }
  }

  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    iterationCount = 0;

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
  }

  public long getIterationCount() {
    return iterationCount;
  }

  protected void doFire() throws ProcessingException {
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
          } catch (PasserelleException e) {
            throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "", this, e);
          }
          if (msg == null) {
            blockingInputFinishRequests.set(i, Boolean.TRUE);
            getLogger().debug("{} doFire() - found exhausted port {}", handler.getPort());
          } else {
            if (getLogger().isDebugEnabled()) {
              getLogger().debug("{} doFire() - received {}", getFullName(), getAuditTrailMessage(msg, handler.getPort()));
            }
          }
        }
        req.addInputMessage(i, handler.getName(), msg);
      }
      // then merge all pushed msgs
      // these may still contain 'old' data,
      // i.e. msgs received a while ago
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
    if (!isFinishRequested()) {
      doFire_HandleRequest(req);
    }
  }

  /**
   * Overridable method that executes the 3 steps for processing a request:
   * <ul>
   * <li>validate before continuing the fire() behaviour (if needed)
   * <li>process the request
   * <li>send outputs
   * </ul>
   * 
   * @param req
   * @throws ProcessingException
   */
  protected void doFire_HandleRequest(ProcessRequest req) throws ProcessingException {
    ActorContext ctxt = new ActorContext();
    doFire_ValidationPart(req, ctxt);
    ProcessResponse response = doFire_ProcessingPart(req, ctxt);
    doFire_SendOutputPart(response);
  }

  /**
   * @param req
   * @param ctxt
   * @throws ProcessingException
   */
  protected void doFire_ValidationPart(ProcessRequest req, ActorContext ctxt) throws ProcessingException {
    if (mustValidateIteration()) {
      try {
        getLogger().trace("{} doFire() - validating iteration for request {}", getFullName(), req);
        validateIteration(ctxt, req);
        getAuditLogger().debug("ITERATION VALIDATED");
        getLogger().trace("{} doFire() - validation done", getFullName());
      } catch (ValidationException e) {
        try {
          getErrorControlStrategy().handleIterationValidationException(this, e);
        } catch (IllegalActionException e1) {
          // interpret this is a FATAL error
          throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_FATAL, "", this, e);
        }
      }
    }
  }

  /**
   * Further part of the doFire logic : let the actor do it's actual processing based on the inputs received, and stored in the request object.
   * 
   * @param req
   * @param ctxt
   * @return
   * @throws ProcessingException
   */
  protected ProcessResponse doFire_ProcessingPart(ProcessRequest req, ActorContext ctxt) throws ProcessingException {
    // now let the actor do it's real work
    ProcessResponse response = new ProcessResponse(req);
    getLogger().trace("{} doFire() - processing request {}", getFullName(), req);
    process(ctxt, req, response);
    getLogger().trace("{} doFire() - obtained response {}", getFullName(), response);
    return response;
  }

  /**
   * Final part of the doFire logic : send output messages that are contained in the response object.
   * 
   * @param response
   * @throws ProcessingException
   */
  protected void doFire_SendOutputPart(ProcessResponse response) throws ProcessingException {
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
          throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error creating output sequence msg", this, context.getMessage(), e);
        }
      }
    }
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
   * @param request
   *          contains all messages received on the actor's input ports for the current iteration.
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
    return getDirectorAdapter().mustValidateIteration();
  }
}
