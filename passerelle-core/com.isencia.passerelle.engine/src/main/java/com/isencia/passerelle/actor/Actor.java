/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.passerelle.actor;

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
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringEvent.FiringEventType;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.actor.gui.EditorIcon;
import com.isencia.passerelle.actor.gui.IOptionsFactory;
import com.isencia.passerelle.actor.gui.OptionsFactory;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.ErrorPort;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListener;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.director.DirectorUtils;
import com.isencia.passerelle.domain.cap.BlockingQueueReceiver;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.interceptor.IMessageCreator;
import com.isencia.passerelle.statistics.ActorStatistics;
import com.isencia.passerelle.statistics.StatisticsServiceFactory;

/**
 * Base class for all Passerelle Actors. Uses Passerelle's custom parameter panes. Defines a getInfo() method, combining
 * the actor's name with extended info that can be defined in actor subclasses.
 * <p>
 * An actor's life cycle is determined/executed through the following methods, that are being invoked by the Passerelle
 * engine. <br>
 * 1. Constructor :
 * <ul>
 * <li>invoked once
 * <li>this is where IO ports and parameters must be constructed
 * <li>Actor subclasses must call the super(...) constructor
 * </ul>
 * 2. preInitialize()
 * <ul>
 * <li>invoked once for every model execution
 * <li>this is where model-dependent resource activation/configuration must be done
 * <li>Actor subclasses must call super.preInitialize()
 * </ul>
 * <p>
 * In the standard Passerelle execution mode, the above methods are invoked for all actors in a model in one common
 * thread, e.g. the preInitialize() of all actors is called sequentially. As a consequence, none of these methods should
 * block!
 * </p>
 * <p>
 * In the standard Passerelle execution mode, all methods below are invoked concurrently on all actors in a model, i.e.
 * each actor's fireing cycle has its own thread.
 * </p>
 * 3. initialize()
 * <ul>
 * <li>invoked once for every model execution AND for every dynamic model adjustment (not supported by engine in
 * Passerelle v1.x)
 * <li>this is where all run initialization must be done
 * <li>Actor subclasses must call super.initialize()
 * </ul>
 * 4. preFire()
 * <ul>
 * <li>invoked once before every fire()
 * <li>used to test cycle preconditions and (re)set cycle parameters
 * <li>return true to allow call to fire()
 * <li>return false to indicate preconditions are not OK and firing cycle should be retried later
 * <li>Actor subclasses must call super.preFire() and include the returned boolean in their logical result expression
 * </ul>
 * 5. fire()
 * <ul>
 * <li>invoked once between every preFire() and postFire()
 * <li>this is where the real actor behaviour must be implemented: read inputs, do something and possibly send results
 * <li>fire() is implemented by Actor base classes as a template method, and some specific methods (doFire(),...) must
 * be implemented to fill in the custom behaviour.
 * </ul>
 * 6. postFire()
 * <ul>
 * <li>invoked once after every fire()
 * <li>used to test cycle postconditions
 * <li>return true if actor's processing cycle should continue
 * <li>return false if actor's processing cycle should stop, after which wrapUp() will be invoked by the Passerelle
 * Engine
 * <li>Actor subclasses must call super.postFire() and include the returned boolean in their logical result expression
 * </ul>
 * 7a. wrapUp()
 * <ul>
 * <li>invoked once at the end of an actor's processing cycle
 * <li>this is where all resources should be released, and all extra threads that the actor has launched should be
 * properly terminated
 * <li>after this method invocation, the actor will leave from the running model
 * <li>Actor subclasses must call super.wrapUp()
 * </ul>
 * 7b. terminate()
 * <ul>
 * <li>invoked when normal termination via wrapUp() does not work
 * <li>should also release all resources and ensure that all threads for the actor are terminated.
 * <li>should not block, a terminate() procedure should be swift and reliable.
 * <li>Actor subclasses must call super.terminate()
 * </ul>
 * </p>
 * <p>
 * An actor normally determines by itself at what moment it can leave from a running model, i.e. when it is of no more
 * use. This is typically related to the status of the actor's input feeds: input ports (for Transformers and Sinks) or
 * external data feeds (for Sources).
 * </p>
 * A Passerelle input may signal that it has reached the end of its feed by returning a "null" message. For actors with
 * only 1 input, a "null" message indicates that the actor can safely decide that it can leave the running model. The
 * actor implementation code can signal that to the Passerelle infrastructure by calling Actor.requestFinish(). Then the
 * Actor.postFire() will return false, wrapUp() will be called etc. Actors with multiple inputs must determine which
 * inputs are critical for the actors' processing and which are optional. If a situation arises where the critical
 * inputs are no longer alive, again requestFinish() can be invoked after which the actor will gracefully leave the
 * running model. </p>
 * <p>
 * Some actors also have a "trigger" port. These have the following behaviour:
 * <ul>
 * <li>the trigger port is not connected: the trigger port is completely ignored, i.e. the Actor behaves as a
 * non-triggered one
 * <li>the trigger port is connected:
 * <ul>
 * <li>1. the actor may only start generating output messages after receiving a trigger message
 * <li>2. the actor may only invoke requestFinish() when its (critical) input(s) have run dry AND the trigger port as
 * well.
 * <li>3. the actor must be able to restart getting input data and generating results after its inputs have run dry,
 * when a next trigger message is received. <br>
 * E.g. a FileReader actor will re-read the same file and send the contents as output messages every time a new trigger
 * is received.
 * </ul>
 * </ul>
 * </p>
 * 
 * @author erwin
 */
public abstract class Actor extends TypedAtomicActor implements IMessageCreator {

  private static final long serialVersionUID = 1L;

  private final static Logger LOGGER = LoggerFactory.getLogger(Actor.class);

  private final static Logger AUDITLOGGER = LoggerFactory.getLogger("audit");

  public static final String MANDATORY_ATTR_NAME = "_mandatory";

  // A simple cache for all event types, so we don't need to construct new
  // ones for every iteration. Will only be set when the actor is being debugged.
  private Map<FiringEventType, FiringEvent> firingEventCache;

  private ActorStatistics statistics;

  private ErrorControlStrategy errorControlStrategy;

  /**
   * Flag indicating that a polite request has arrived to finish this actors processing cycle. The actor will react on
   * this by returning false from its next invocation of postFire().
   */
  private boolean finishRequested = false;

  /**
   * Flag indicating that the actor is in a paused state, as a consequence of a pause() call. In this state, its
   * iteration cycle has been halted, and will only continue after a resume() has been done.
   */
  private boolean paused;

  /**
   * Flag indicating that the actor should be treated as a "daemon" actor, similar to the concept of daemon threads.
   * I.e. the actor serves as "support"for the actor/model execution, but should not block the model termination. <br/>
   * This can be applied e.g. to ErrorHandler actors without connected input ports etc.
   */
  private boolean daemon;

  /**
   * CONTROL input port, used to request an actor to finish its processing.
   */
  public ControlPort requestFinishPort = null;
  private PortHandler requestFinishHandler;

  /**
   * CONTROL output port, used by an actor to indicate that a TECHNICAL error occurred during its processing. FUNCTIONAL
   * errors should be handled by extra output ports, specific to the functional domain of each actor. The basic
   * implementation of Actor.fire() uses a Template Method pattern that catches all checked and unchecked exceptions
   * from the abstract doFire() method. In the catch-block, an error message is generated on the error port, containing
   * some error information, if the error is {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}
   * .
   */
  public ErrorPort errorPort = null;

  /**
   * CONTROL output port, used by an actor to indicate that it has finished its processing, and is starting its wrapup()
   * handling.
   */
  public ControlPort hasFinishedPort = null;

  /**
   * CONTROL output port, used by an actor to indicate that it has finished one fire() execution
   */
  public ControlPort hasFiredPort = null;

  protected boolean isFiring = false;

  /**
   * The options factory can be used to extend/modify options for actor parameters. It is typically set in the
   * configuration files, so we don't need to modify actor source code for options extensions.
   */
  public final static String OPTIONS_FACTORY_CFG_NAME = "_optionsFactory";
  private IOptionsFactory optionsFactory;

  /**
   * The collection of parameters that are meant to be available to a model configurer tool. The actor's parameters that
   * are not in this collection are not meant to be configurable, but are only meant to be used during model assembly
   * (in addition to the public ones).
   */
  private Collection<Parameter> configurableParameters = new ArrayList<Parameter>();

  /**
   * The collection of parameters that are meant to be available to an expert only, inside the modeling tool. All
   * parameters that are not in this collection will always be visible in the modeling tool...
   */
  private Collection<Parameter> expertParameters = new ArrayList<Parameter>();

  /**
   * The collection of standard headers for each message generated by this actor
   */
  protected Map<String, String> actorMsgHeaders = new HashMap<String, String>();

  /**
   * Parameter to set a size for input port queues, starting at which a warning message will be logged. This can be
   * useful to determine processing hot-spots in Passerelle sequences, where actors may become flooded by input messages
   * that they are unable to process in time.
   * <p>
   * Default value = -1 indicates that no such warning logs are generated.
   * </p>
   */
  public Parameter receiverQueueWarningSizeParam;
  /**
   * Parameter to set a max capacity for input port queues. When a queue reaches its max capacity, any new tokens trying
   * to reach the input port will be refused, and a NoRoomException will be thrown.
   * <p>
   * Should only be used in very specific cases, as it does not correspond to the theoretical semantics of Kahn process
   * networks, the basis for Passerelle's execution model (cfr Ptolemy project docs).
   * </p>
   * <p>
   * Default value = -1 indicates that received queues have unlimited capacity.
   * </p>
   */
  public Parameter receiverQueueCapacityParam;

  /**
   * Constructor for Actor.
   * 
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Actor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    requestFinishPort = PortFactory.getInstance().createInputControlPort(this, "requestFinish");

    errorPort = PortFactory.getInstance().createOutputErrorPort(this);

    hasFiredPort = PortFactory.getInstance().createOutputControlPort(this, "hasFired");

    hasFinishedPort = PortFactory.getInstance().createOutputControlPort(this, "hasFinished");

    receiverQueueCapacityParam = new Parameter(this, "Receiver Q Capacity (-1)", new IntToken(-1));
    receiverQueueWarningSizeParam = new Parameter(this, "Receiver Q warning size (-1)", new IntToken(-1));
    registerExpertParameter(receiverQueueCapacityParam);
    registerExpertParameter(receiverQueueWarningSizeParam);

    try {
      new EditorIcon(this, "_icon");
    } catch (Throwable t) { // NOSONAR
      // ignore, probably means that we're on a server with no display
    }

    actorMsgHeaders.put(ManagedMessage.SystemHeader.HEADER_SOURCE_REF, getFullName());
  }

  public DirectorAdapter getDirectorAdapter() {
    return DirectorUtils.getAdapter(getDirector(), null);
  }

  /**
   * Allow public access to flag indicating whether this actor is currently a "debugging target". I.e. whether
   * DebugListeners are registered, as is typically the case when a breakpoint has been set for this actor.
   * 
   * @return true if this actor is part of a debugging configuration, e.g. a breakpoint has been set for it.
   */
  public boolean isDebugged() {
    return _debugging;
  }

  /**
   * Override this method as "interceptor" to ensure FiringEvents are also sent to DebugListeners, so Passerelle can
   * implement its breakpoint mechanism on top of Ptolemy's related features. <br/>
   * A second goal is to optimize event instance creation by using a cache of pre-constructed events per type, i.o.
   * ptolemy's default approach of creating new events per recording request.
   */
  @Override
  public void recordFiring(FiringEventType type) {
    if (isDebugged()) {
      initializeEventCacheIfStillNeeded();
      FiringEvent firingEvent = firingEventCache.get(type);
      if (firingEvent != null) {
        event(firingEvent);
        getDirectorAdapter().notifyFiringEventListeners(firingEvent);
      }
    }
    // TODO check with ptolemy if we can not move the event cache down to them
    // for the moment this call always creates a new event instance before checking
    // if there are listeners that are interested at all...
    super.recordFiring(type);
  }

  protected void initializeEventCacheIfStillNeeded() {
    if (firingEventCache == null) {
      Director director = getDirector();
      firingEventCache = new HashMap<FiringEventType, FiringEvent>();
      firingEventCache.put(FiringEvent.BEFORE_ITERATE, new FiringEvent(director, this, FiringEvent.BEFORE_ITERATE));
      firingEventCache.put(FiringEvent.BEFORE_PREFIRE, new FiringEvent(director, this, FiringEvent.BEFORE_PREFIRE));
      firingEventCache.put(FiringEvent.AFTER_PREFIRE, new FiringEvent(director, this, FiringEvent.AFTER_PREFIRE));
      firingEventCache.put(FiringEvent.BEFORE_FIRE, new FiringEvent(director, this, FiringEvent.BEFORE_FIRE));
      firingEventCache.put(FiringEvent.AFTER_FIRE, new FiringEvent(director, this, FiringEvent.AFTER_FIRE));
      firingEventCache.put(FiringEvent.BEFORE_POSTFIRE, new FiringEvent(director, this, FiringEvent.BEFORE_POSTFIRE));
      firingEventCache.put(FiringEvent.AFTER_POSTFIRE, new FiringEvent(director, this, FiringEvent.AFTER_POSTFIRE));
      firingEventCache.put(FiringEvent.AFTER_ITERATE, new FiringEvent(director, this, FiringEvent.AFTER_ITERATE));
    }
  }

  /**
   * @return the execution statistics of this actor
   */
  public ActorStatistics getStatistics() {
    return statistics;
  }

  /**
   * Utility method to log informational messages. <br/>
   * The idea is to harmonize the log message, if in 'info' level add the actor name and if in 'trace' or 'debug' level
   * add the full name.
   * 
   * @param logMessage
   */
  public void logInfo(String logMessage) {
    if (LOGGER.isTraceEnabled() || LOGGER.isDebugEnabled())
      LOGGER.debug(this.getFullName() + " - " + logMessage);
    else if (LOGGER.isInfoEnabled())
      LOGGER.info(this.getName() + " - " + logMessage);
  }

  /**
   * Returns a unique and informative description of this actor instance.
   * 
   * @return A unique description of this actor instance
   * @deprecated just use getName() or getFullName()
   */
  @Deprecated
  final public String getInfo() {
    return getName() + " - " + getExtendedInfo();
  }

  /**
   * Returns a part of the unique description, often combining a number of parameter settings. This part is appended to
   * the actor name in the getInfo() method.
   * 
   * @return A part of the unique description, often combining a number of parameter settings.
   * @deprecated
   */
  @Deprecated
  protected String getExtendedInfo() {
    return "";
  }

  public IOptionsFactory getOptionsFactory() {
    try {
      Attribute attribute = getAttribute(OPTIONS_FACTORY_CFG_NAME, OptionsFactory.class);
      if (attribute != optionsFactory) {
        optionsFactory = (OptionsFactory) attribute;
      }
    } catch (IllegalActionException e) {
      getLogger().error("Error during getting of OptionsFactory attribute", e);
    }

    return optionsFactory;
  }

  protected void setOptionsFactory(IOptionsFactory optionsFactory) {
    this.optionsFactory = optionsFactory;
  }

  /**
   * <p>
   * IMPORTANT REMARK : Since Passerelle v8.0, this logic has moved from being invoked during
   * <code>Actor.initialize()</code> to <code>Actor.preinitialize()</code>! This to allow a completely reliable stop of
   * a model execution, i.c.o. a <code>ValidationException</code>, without running the risk that some other actor
   * already did some work. In process-like domains, <code>Actor.initialize()</code> is invoked concurrently on all
   * actors, i.a. when the actor threads have already started. <code>Actor.preinitialize()</code> is done sequentially
   * for all actors, before their threads are started.
   * </p>
   **/
  @Override
  @SuppressWarnings("unchecked")
  final public void preinitialize() throws IllegalActionException {
    getLogger().trace("{} - preinitialize() - entry", getFullName());

    super.preinitialize();

    statistics = new ActorStatistics(this);

    try {
      doPreInitialize();

      // let the Passerelle ports do their initialization,
      // which prepares for the Passerelle-method for model termination
      List<ptolemy.kernel.Port> ports = portList();
      for (Iterator<ptolemy.kernel.Port> portsItr = ports.iterator(); portsItr.hasNext();) {
        ptolemy.kernel.Port port = portsItr.next();
        if (port instanceof Port) {
          ((Port) port).initialize();
        }
      }

      if (mustValidateInitialization()) {
        try {
          validateInitialization();
          if (getAuditLogger().isDebugEnabled())
            getAuditLogger().debug(getFullName() + " - (PRE)INITIALIZATION VALIDATED");
        } catch (ValidationException e) {
          getErrorControlStrategy().handleInitializationValidationException(this, e);
        }
      }
    } catch (InitializationException e) {
      getErrorControlStrategy().handleInitializationException(this, e);
    }
    getDirectorAdapter().notifyActorActive(this);

    getLogger().trace("{} - preinitialize() - exit ", getFullName());
  }

  @Override
  public Receiver newReceiver() throws IllegalActionException {
    Receiver rcver = super.newReceiver();
    if (rcver instanceof BlockingQueueReceiver) {
      BlockingQueueReceiver qRcvr = (BlockingQueueReceiver) rcver;
      int qCapacity = receiverQueueCapacityParam != null ? ((IntToken) receiverQueueCapacityParam.getToken()).intValue() : -1;
      qRcvr.setCapacity(qCapacity);

      int qWarningSize = receiverQueueWarningSizeParam != null ? ((IntToken) receiverQueueWarningSizeParam.getToken()).intValue() : -1;
      qRcvr.setSizeWarningThreshold(qWarningSize);
    }
    return rcver;
  }

  /**
   * Template method implementation for preinitialize().
   * 
   * @throws InitializationException
   * @see ptolemy.actor.AtomicActor#preinitialize()
   */
  protected void doPreInitialize() throws InitializationException {
  }

  @Override
  final public void initialize() throws IllegalActionException {
    getLogger().trace("{} - initialize() - entry", getFullName());

    super.initialize();
    paused = false;
    finishRequested = false;

    // TODO see how we can avoid starting a PortHandler when outside of Process domains
    if (requestFinishPort.getWidth() > 0) {
      // If at least 1 channel is connected to the port
      // Install handler on input port
      requestFinishHandler = createPortHandler(requestFinishPort, new PortListenerAdapter() {
        @Override
        public void tokenReceived() {
          Token token = requestFinishHandler.getToken();
          if (token != null && !token.isNil()) {
            getLogger().trace("{} - requestFinishHandler.tokenReceived()", getFullName());
            requestFinish();
            getLogger().trace("{} - requestFinishHandler.tokenReceived()", getFullName());
          }
        }
      });
      // Start handling the port
      requestFinishHandler.start();
    }

    try {
      getLogger().trace("{} doInitialize() - entry", getFullName());
      doInitialize();
      getLogger().trace("{} doInitialize() - exit", getFullName());
    } catch (InitializationException e) {
      getErrorControlStrategy().handleInitializationException(this, e);
    }

    statistics.reset();
    StatisticsServiceFactory.getService().registerStatistics(statistics);

    // audit logging for state per actor is on debug
    // NDC is not yet active during initialize, so we
    // show complete getFullName().
    if (getAuditLogger().isDebugEnabled())
      getAuditLogger().debug(getFullName() + " - INITIALIZED");

    getLogger().trace("{} - initialize() - exit ", getFullName());
  }

  /**
   * Overridable method to determine if an actor should do a validation of its initialization results. <br>
   * By default, checks on its Passerelle director what must be done. If no Passerelle director is used (but e.g. a
   * plain Ptolemy one), it returns true.
   * 
   * @see validateInitialization()
   * @see initialize()
   * @return
   * @throws IllegalActionException
   */
  protected boolean mustValidateInitialization() throws IllegalActionException {
    return getDirectorAdapter().mustValidateInitialization();
  }

  /**
   * Template method implementation for initialize().
   * 
   * @throws InitializationException
   * @see ptolemy.actor.AtomicActor#initialize()
   */
  protected void doInitialize() throws InitializationException {
  }

  /**
   * <p>
   * Method that should be overridden for actors that need to be able to validate their initial conditions, after the
   * actor's doPreInitialize() is done and before their first iteration is executed when a model is launched.
   * </p>
   * <p>
   * IMPORTANT REMARK : Since Passerelle v8.0, this logic has moved from being invoked during
   * <code>Actor.initialize()</code> to <code>Actor.preinitialize()</code>! This to allow a completely reliable stop of
   * a model execution, i.c.o. a <code>ValidationException</code>, without running the risk that some other actor
   * already did some work. In process-like domains, <code>Actor.initialize()</code> is invoked concurrently on all
   * actors, i.a. when the actor threads have already started. <code>Actor.preinitialize()</code> is done sequentially
   * for all actors, before their threads are started.
   * </p>
   * 
   * @throws ValidationException
   */
  protected void validateInitialization() throws ValidationException {
  }

  /**
   * @return a flag indicating that the actor should be treated as a "daemon" actor, similar to the concept of daemon
   *         threads. I.e. the actor serves as "support"for the actor/model execution, but should not block the model
   *         termination.
   */
  public boolean isDaemon() {
    return daemon;
  }

  /**
   * Actor implementations should invoke this method with argument "true" when they want to be treated as daemon actors.
   * 
   * @param daemon
   *          set true when the actor must be treated as a daemon actor.
   */
  protected void setDaemon(boolean daemon) {
    this.daemon = daemon;
  }

  /**
   * Non-threadsafe method that can be used as an indication whether this actor is in its fire() processing. Can be used
   * for example in a monitoring UI to activate some kind of actor decoration. TODO: better alternative is to implement
   * an Observer for this feature.
   * 
   * @return a flag indicating whether this actor is in its fire() processing
   */
  final public boolean isFiring() {
    return (isFiring);
  }

  /**
   * Within Passerelle, actors typically change state info during prefire/fire...
   */
  @Override
  public boolean isFireFunctional() {
    return false;
  }

  /**
   * A slight variation on the stopFire() semantics, provided in Ptolemy. A stopFire() is used to interrupt asap an
   * ongoing fire() - that may be blocked, waiting for some event or so - but does not assume that a resume of the
   * fire() will be explicitly invoked.
   * <p>
   * Even though the doc of the stopFire() mentions that it assumes that an actor should be able to continue where it
   * left, when a next fire() is called after a stopFire(), it is not clear how this can be represented/implemented.
   * E.g. a problem is that in principle, each fire() iteration risks needing new input messages on the input port(s)...
   * </p>
   * <p>
   * A Passerelle actor thus supports a more "traditional" pair of pauseFire/resumeFire methods, accompanied by 2
   * overridable doPauseFire()/doResumeFire() methods.
   * </p>
   * <p>
   * When a running model is paused/resumed, the actors will first receive a "notification" invocation of the
   * pauseFire/resumeFire methods, before the actual "pause" or "resume" of the model iterations is done. In this way,
   * an actor can react in a consistent way, independently of the "normal" fire() iteration semantics.
   * </p>
   * <p>
   * E.g. a long-running external operation may be interrupted/paused in doPauseFire() and the actor may store status
   * info if needed. During the doResumeFire(), the original operation may be continued till completion, before a next
   * actor fire() iteration is done.
   * </p>
   * 
   * @return true if the actor was not yet paused, and is paused now.
   */
  final public synchronized boolean pauseFire() {
    getLogger().trace("{} - pauseFire() - entry", getFullName());
    boolean wasNotPaused = !paused;
    try {
      paused = true;
      if (wasNotPaused)
        doPauseFire();
      return wasNotPaused;
    } catch (ClassCastException e) {
      return wasNotPaused;
    } finally {
      getLogger().trace("{} - pauseFire() - exit", getFullName());
    }
  }

  /**
   * Overridable template method, for when an actor implementation needs some special action to be done when an actor is
   * being paused from an active state.
   * <p>
   * By default it calls doStopFire().
   * </p>
   */
  protected void doPauseFire() {
    doStopFire();
  }

  /**
   * A "notification" method, allowing an actor to first resume any pending operation that was interrupted/paused with
   * pauseFire(), before the "normal" actor fire() iterations are resumed.
   * <p>
   * Remark that all paused actors will receive a resumeFire() notification call sequentially, in 1 thread, and that the
   * "normal" model iterations will only start after all actors have finished their resumeFire() actions. The order of
   * the invocations on the different actors is not determined.
   * </p>
   * 
   * @return true if the actor was paused before, and is no longer paused after this method has finished.
   */
  final public synchronized boolean resumeFire() {
    getLogger().trace("{} - resumeFire() - entry", getFullName());
    boolean wasPaused = paused;
    try {
      if (wasPaused)
        doResumeFire();
      paused = false;
      return wasPaused;
    } catch (ClassCastException e) {
      return wasPaused;
    } finally {
      getLogger().trace("{} - resumeFire() - exit - {}", getFullName(), wasPaused);
    }
  }

  /**
   * Overridable template method, for when an actor implementation needs some special action to be done when an actor is
   * resuming from a paused state.
   * <p>
   * By default it does nothing.
   * </p>
   */
  protected void doResumeFire() {
  }

  /**
   * @return whether this actor's fire cycle is paused or not.
   */
  final public synchronized boolean isPaused() {
    return paused;
  }

  @Override
  final public boolean prefire() throws IllegalActionException {
    getLogger().trace("{} - prefire() - entry", getFullName());
    boolean res = true;
    try {
      if (!isFinishRequested()) {
        try {
          getLogger().trace("{} doPreFire() - entry", getFullName());
          res = doPreFire();
          getLogger().trace("{} doPreFire() - exit", getFullName());
        } catch (ProcessingException e) {
          res = false;
          getErrorControlStrategy().handlePreFireException(this, e);
        } catch (RuntimeException e) {
          res = false;
          getErrorControlStrategy().handlePreFireRuntimeException(this, e);
        }
      }
      return res;
    } catch (IllegalActionException e) {
      getDirectorAdapter().notifyActorInactive(this);
      throw e;
    } finally {
      getLogger().trace("{} - prefire() - exit - {}", getFullName(), res);
    }
  }

  /**
   * Template method implementation for prefire(). Method that can be overriden to implement precondition checking for
   * the fire() loop. By default, returns true. If the method returns true, the actor's fire() method will be called. If
   * the method returns false, preFire() will be called again repetitively till it returns true. So it's important that
   * for "false" results there is some blocking/waiting mechanism implemented to avoid wild looping!
   * 
   * @return flag indicating whether the actor is ready for fire()
   * @see ptolemy.actor.AtomicActor#prefire()
   */
  protected boolean doPreFire() throws ProcessingException {
    return true;
  }

  /**
   * The basic implementation of Actor.fire() uses a Template Method pattern that catches all checked and unchecked
   * exceptions from the abstract doFire() method. In the catch-block, an error message is generated on the error port,
   * containing some error information, if the error is
   * {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}. For
   * {@link com.isencia.passerelle.core.PasserelleException#FATAL FATAL} exceptions, an IllegalException is generated.
   * If the error port is not connected, {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}
   * errors are notified to the Passerelle Director. The fire() method also generates notification messages on the
   * {@link #hasFiredPort} for each successfull fire loop.
   * 
   * @throws IllegalActionException
   */
  @Override
  final public void fire() throws IllegalActionException {
    isFiring = true;
    try {
      getLogger().trace("{} - fire() - entry", getFullName());
      if (!isFinishRequested()) {
        try {
          if (!isMockMode()) {
            getLogger().trace("{} doFire() - entry", getFullName());
            doFire();
            getLogger().trace("{} doFire() - exit", getFullName());
          } else {
            doMockFire();
          }
        } catch (ProcessingException e) {
          getErrorControlStrategy().handleFireException(this, e);
        } catch (TerminateProcessException e) {
          requestFinish();
          // delegate handling to domain execution process
          throw e;
        } catch (RuntimeException e) {
          getErrorControlStrategy().handleFireRuntimeException(this, e);
        }
      }
    } catch (IllegalActionException e) {
      getDirectorAdapter().notifyActorInactive(this);
      throw e;
    } finally {
      getLogger().trace("{} - fire() - exit", getFullName());
      isFiring = false;
      if (hasFiredPort.getWidth() > 0) {
        try {
          hasFiredPort.broadcast(new PasserelleToken(MessageFactory.getInstance().createTriggerMessage()));
        } catch (Exception e) {
          getLogger().error(getFullName() + " - Error sending hasFired msg", e);
        }
      }
    }
  }

  /**
   * Template method implementation for fire(). The actual processing behaviour of the actor must be implemented by this
   * method.
   * 
   * @throws ProcessingException
   * @see ptolemy.actor.AtomicActor#fire()
   */
  protected abstract void doFire() throws ProcessingException;

  /**
   * Utility method to support developing actors that can run in mock mode. In that mode, they could e.g. simulate/mock
   * some sample behaviour without needing to access external resources (databases, message buses etc). By default, this
   * method just calls doFire(). Complex actors with dependencies on external resources, may override this method to
   * allow easy local testing in the IDE. The mock mode is defined on the Passerelle director.
   * 
   * @throws ProcessingException
   */
  protected void doMockFire() throws ProcessingException {
    doFire();
  }

  @Override
  final public boolean postfire() throws IllegalActionException {
    getLogger().trace("{} - postfire() - entry", getFullName());
    boolean res = true;
    try {
      try {
        getLogger().trace("{} doPostFire() - entry", getFullName());
        res = doPostFire();
        getLogger().trace("{} doPostFire() - exit", getFullName());
      } catch (ProcessingException e) {
        res = false;
        getErrorControlStrategy().handlePostFireException(this, e);
      } catch (RuntimeException e) {
        res = false;
        getErrorControlStrategy().handlePostFireRuntimeException(this, e);
      }
      return res;
    } finally {
      getLogger().trace("{} - postfire() - exit - {}", getFullName(), res);
    }
  }

  /**
   * Template method implementation for postfire(). Method that can be overriden to implement postcondition checking for
   * the fire() loop. By default, returns true unless a finish has been requested, i.e. it delegates to
   * isFinishRequested(). If the method returns true, the actor's preFire/fire/postFire loop will be called again. If
   * the method returns false, the fire loop will stop and the actor's wrapup() method will be called by the
   * Passerelle/Ptolemy framework.
   * 
   * @return flag indicating whether the actor wants to continue with its fire loop
   * @see ptolemy.actor.AtomicActor#postfire()
   */
  protected boolean doPostFire() throws ProcessingException {
    return !isFinishRequested();
  }

  @Override
  final public void wrapup() throws IllegalActionException {
    getLogger().trace("{} - wrapup() - entry", getFullName());

    StatisticsServiceFactory.getService().unregisterStatistics(statistics);

    try {
      getLogger().trace("{} doWrapUp() - entry", getFullName());
      doWrapUp();
      getLogger().trace("{} doWrapUp() - exit", getFullName());
    } catch (TerminationException e) {
      getErrorControlStrategy().handleTerminationException(this, e);
    }

    // This may seem duplicate work, but it's not.
    // In process-like domains, models shut down nicely in an automatic way
    // when the input ports notice that there all their "src" ports are exhausted.
    // I.e. for those domains. ports typically are already "finished" before the actor starts wrapping up.
    // So there, this next piece of code is not needed.
    // For event-like domains the wrap-up flow is different however.
    // There, the Director decides when to shut down the model,
    // which is more similar to a forced stop by a user.
    Iterator inputPorts = inputPortList().iterator();
    while (inputPorts.hasNext()) {
      Port inputPort = (Port) inputPorts.next();
      inputPort.requestFinish();
    }

    try {
      hasFinishedPort.broadcast(new PasserelleToken(MessageFactory.getInstance().createTriggerMessage()));
    } catch (Exception e) {
      getLogger().error(getFullName() + " - Error sending hasFinished msg", e);
    }

    // Inform connected receivers that this actor has stopped
    Iterator outputPorts = outputPortList().iterator();

    while (outputPorts.hasNext()) {
      Port port = (Port) outputPorts.next();
      // port.broadcast(PasserelleToken.POISON_PILL);
      Receiver[][] farReceivers = port.getRemoteReceivers();

      for (int i = 0; i < farReceivers.length; i++) {
        if (farReceivers[i] != null) {
          for (int j = 0; j < farReceivers[i].length; j++) {
            if (farReceivers[i][j].getContainer() instanceof Port) {
              ((Port) farReceivers[i][j].getContainer()).notifySourcePortFinished(port);
            }
            // the below does not work well when "diamond" relations are used
            // as these can lead to different counts of source ports connecting
            // to the diamond, versus channels arriving at the destination port(s)
            // and then a source port could still try to send a msg to a
            // receiver that already received a requestFinish(), leading to TerminationExceptions...
            // if (farReceivers[i][j] instanceof ProcessReceiver) {
            // // Ensure that the model termination
            // // ripples through the complete model.
            // ((ProcessReceiver) farReceivers[i][j]).requestFinish();
            // }
            // else {
            // When using a Passerelle actor in a
            // non-process-oriented model, we trust
            // the domain processing for determination
            // of model termination, so we don't do
            // anything special in this case...
            // }
          }
        } else {
          getLogger().warn("{} - wrapup() - port {} has a remote receiver null on channel {}", new Object[] { getFullName(), port.getName(), i });
        }
      }
    }

    super.wrapup();

    getAuditLogger().debug("{} - WRAPPED UP", getFullName());

    getDirectorAdapter().notifyActorInactive(this);

    getLogger().trace("{} - wrapup() - exit", getFullName());
  }

  /**
   * Template method implementation for wrapup().
   * 
   * @throws TerminationException
   * @see ptolemy.actor.AtomicActor#wrapup()
   */
  protected void doWrapUp() throws TerminationException {
  }

  @Override
  final public void terminate() {
    getLogger().trace("{} - terminate() - entry", getFullName());
    super.terminate();
    getLogger().trace("{} - terminate() - exit", getFullName());
  }

  @Override
  final public void stopFire() {
    getLogger().trace("{} - stopfire() - entry()", getFullName());
    pauseFire();
    getLogger().trace("{} - stopfire() - exit", getFullName());
  }

  @Override
  final public void stop() {
    getLogger().trace("{} - stop() - entry()", getFullName());
    super.stop();
    if (!isFinishRequested()) {
      requestFinish();
    }
    doStop();
    getLogger().trace("{} - stop() - exit", getFullName());
  }

  /**
   * Template method implementation for stopFire().
   * 
   * @see ptolemy.actor.AtomicActor#stopFire()
   */
  protected void doStopFire() {
  }

  /**
   * Template method implementation for stop().
   * 
   * @see ptolemy.actor.AtomicActor#stop()
   */
  protected void doStop() {
  }

  /**
   * Method to request this actor to finish its processing.
   */
  final public void requestFinish() {
    finishRequested = true;
    getLogger().debug("{} FINISH REQUESTED !!", getFullName());
  }

  /**
   * @return a flag indicating whether a finish has already been requested for this actor
   */
  final public boolean isFinishRequested() {
    return finishRequested;
  }

  /**
   * @return a flag indicating whether this actor is executing in a model that is launched in mock/test-mode
   */
  final public boolean isMockMode() {
    return getDirectorAdapter().isMockMode();
  }

  /**
   * @return all configurable parameters
   */
  final public Parameter[] getConfigurableParameters() {
    return configurableParameters.toArray(new Parameter[0]);
  }

  /**
   * Method attempts to find a Configurable parameter by class type. This encapsulates finding a parameter by name
   * rather than pushing that find (current just a loop) to outside classes to implement.
   * 
   * @param name
   * @return
   */
  final public Collection<Parameter> getConfigurableParameter(final Class<? extends Parameter> type) {
    if (configurableParameters == null)
      return null;
    if (configurableParameters.isEmpty())
      return null;

    final Parameter[] params = getConfigurableParameters();
    final Collection<Parameter> ret = new HashSet<Parameter>(params.length);
    for (int i = 0; i < params.length; i++) {
      if (type.isAssignableFrom(params[i].getClass())) {
        ret.add(params[i]);
      }
    }
    return ret;
  }

  /**
   * Method attempts to find a Configurable parameter by name. This encapsulates finding a parameter by name rather than
   * pushing that find (current just a loop) to outside classes to implement.
   * 
   * @param name
   * @return
   */
  final public Parameter getConfigurableParameter(final String name) {
    if (name == null)
      return null;
    if (configurableParameters == null)
      return null;
    if (configurableParameters.isEmpty())
      return null;
    final Parameter[] params = getConfigurableParameters();
    for (int i = 0; i < params.length; i++) {
      if (name.equals(params[i].getName())) {
        return params[i];
      }
    }
    return null;
  }

  /**
   * Register an actor parameter as configurable. Such parameters will be available in the Passerelle model
   * configuration tools. All other actor parameters are only available in model assembly tools.
   * 
   * @param newParameter
   */
  final public void registerConfigurableParameter(Parameter newParameter) {
    if (newParameter != null && !configurableParameters.contains(newParameter) && newParameter.getContainer().equals(this)) {
      configurableParameters.add(newParameter);
    }
    // should already be FULL, but let's be a bit profilactic
    newParameter.setVisibility(Settable.FULL);
  }

  /**
   * Marks an actor parameter as mandatory, i.e. it must get a non-blank value.
   * 
   * TODO : validate that all such parameters have non-blank values, during the initialization validation.
   * 
   * @param parameter
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  final public void registerMandatoryParameter(Parameter parameter) throws IllegalActionException, NameDuplicationException {
    new Attribute(parameter, MANDATORY_ATTR_NAME);
  }

  /**
   * Register an actor parameter as visible for experts only in the modeling tool. This also sets the parameter's
   * visibility for Ptolemy to Settable.EXPERT
   * 
   * @param newParameter
   */
  final public void registerExpertParameter(Parameter newParameter) {
    if (newParameter != null && newParameter.getContainer().equals(this)) {
      if (!expertParameters.contains(newParameter))
        expertParameters.add(newParameter);

      newParameter.setVisibility(Settable.EXPERT);
    }
  }

  /**
   * @return
   */
  final public static Logger getAuditLogger() {
    return AUDITLOGGER;
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * Utility method to read the current value from a parameter if it is present and can be read.
   * 
   * @param parameter
   * @return the evaluated string value of the parameter, or null if parameter is null
   * @throws InitializationException
   *           if the parameter is not-null but its value can not be evaluated or read.
   */
  protected String readParameter(StringParameter parameter) throws InitializationException {
    String result = null;
    if (parameter != null) {
      try {
        result = parameter.stringValue();
      } catch (IllegalActionException e) {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading " + parameter.getName(), this, e);
      }
    }
    return result;
  }

  final protected ErrorControlStrategy getErrorControlStrategy() throws IllegalActionException {
    if (errorControlStrategy != null) {
      return errorControlStrategy;
    } else {
      return getDirectorAdapter().getErrorControlStrategy();
    }
  }

  final protected void setErrorControlStrategy(ErrorControlStrategy errorControlStrategy) {
    this.errorControlStrategy = errorControlStrategy;
  }

  /**
   * Default implementation just creates a standard message using the MessageFactory. This method may be overridden by
   * actor sub-classes to handle message creation differently
   * 
   * @return
   */
  @Override
  public ManagedMessage createMessage() {
    return MessageFactory.getInstance().createMessage(getStandardMessageHeaders());
  }

  public ManagedMessage createMessageFromCauses(ManagedMessage... causes) {
    ManagedMessage result = MessageFactory.getInstance().createMessage(getStandardMessageHeaders());
    for (ManagedMessage causeMsg : causes) {
      result.addCauseID(causeMsg.getID());
    }
    return result;
  }

  /**
   * Default implementation just creates a standard message using the MessageFactory. This method may be overridden by
   * actor sub-classes to handle message creation differently
   * 
   * @return
   * @throws MessageException
   */
  public ManagedMessage createMessage(Object content, String contentType) throws MessageException {
    ManagedMessage message = MessageFactory.getInstance().createMessage(getStandardMessageHeaders());
    message.setBodyContent(content, contentType);
    return message;
  }

  /**
   * Default implementation just creates a standard message using the MessageFactory. This method may be overridden by
   * actor sub-classes to handle message creation differently
   * 
   * @return
   */
  @Override
  public ManagedMessage createTriggerMessage() {
    return MessageFactory.getInstance().createTriggerMessage(getStandardMessageHeaders());
  }

  /**
   * Default implementation for creating an error message, based on some exception. This method may be overridden by
   * actor sub-classes to handle message creation differently.
   * 
   * @param exception
   * @return
   */
  @Override
  public ManagedMessage createErrorMessage(PasserelleException exception) {
    return MessageFactory.getInstance().createErrorMessage(exception, getStandardMessageHeaders());
  }

  /**
   * Utility method, to be used by actor implementations that need to override createMessage(), or create
   * ManagedMessages in another way... They should always pass this Map in the MessageFactory.createSomeMessage()
   * methods... TODO find some better way to enforce this...
   * 
   * @return
   */
  final protected Map<String, String> getStandardMessageHeaders() {
    return actorMsgHeaders;
  }

  /**
   * TODO investigate if we can have an alternative to the 'public', which still allows an error strategy to somehow get
   * an actor to send an error message...
   * 
   * @param exception
   * @throws IllegalActionException
   */
  final public void sendErrorMessage(PasserelleException exception) throws IllegalActionException {
    getLogger().debug("{} sendErrorMessage() - generating error msg for exception {}", getFullName(), exception);
    if (errorPort.getWidth() > 0) {
      ManagedMessage errorMessage = createErrorMessage(exception);
      Token errorToken = new PasserelleToken(errorMessage);
      errorPort.broadcast(errorToken);
    } else {
      // notify our director about the problem
      getDirectorAdapter().reportError(this, exception);
    }
  }

  /**
   * Send a message on an output port. Logs msg sending on debug level and in the audit trail. The log msg detail for
   * the audit trail can be defined in actor sub-classes by overriding the method getAuditTrailMessage().
   * 
   * @param port
   * @param message
   * @throws ProcessingException
   * @throws IllegalArgumentException
   *           if the port is not a valid output port of this actor
   * @throws NullPointerException
   *           when port or message are null
   */
  protected void sendOutputMsg(Port port, ManagedMessage message) throws ProcessingException, IllegalArgumentException {
    if (port.getContainer() != this)
      throw new IllegalArgumentException("port " + port.getFullName() + " not defined in actor " + this.getFullName());

    try {
      Token token = new PasserelleToken(message);
      port.broadcast(token);
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("{} - sendOutputMsg() - message {} sent on port {}", new Object[] { getFullName(), message.getID(), port.getName() });
      }

      if (getAuditLogger().isDebugEnabled()) {
        String auditDetail = null;
        try {
          auditDetail = getAuditTrailMessage(message, port);
        } catch (Exception e) {
          // simple hack to log a default msg anyway
          auditDetail = "message " + message.getID() + " on port " + this.getDisplayName() + "." + port.getDisplayName();
        }
        if (auditDetail != null) {
          getAuditLogger().debug(auditDetail);
        }
      }

    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_DELIVERY_FAILURE, "Error sending message on output " + port, this, message, e);
    }
  }

  /**
   * Method to be overridden to specify custom audit logging messages. When it returns null, no audit trail is logged
   * for an outgoing message.
   * 
   * @param message
   * @param port
   * @return
   */
  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    return "message " + message.getID() + (port != null ? " on port " + this.getDisplayName() + "." + port.getDisplayName() : "");
  }

  /**
   * To be invoked by actors when the actual fire() processing is starting. The actor developer must ensure that this
   * method is called before the actual processing logic is being executed, after having received the relevant input
   * messages (or leaving the blocked state for any other reason).
   */
  protected void notifyStartingFireProcessing() {
    getLogger().trace("{} - notifyStartingFireProcessing() - entry", getFullName());
    statistics.beginCycle();
    isFiring = true;
    getLogger().trace("{} - notifyStartingFireProcessing() - exit", getFullName());
  }

  /**
   * To be invoked by actors when the actual fire() processing is finished. The actor developer must ensure that this
   * method is called before the actor gets blocked, waiting for new input messages.
   */
  protected void notifyFinishedFireProcessing() {
    getLogger().trace("{} - notifyFinishedFireProcessing() - entry", getFullName());
    statistics.endCycle();
    isFiring = false;
    getLogger().trace("{} - notifyFinishedFireProcessing() - exit", getFullName());
  }

  @Override
  public Object clone(Workspace workspace) throws CloneNotSupportedException {
    final Actor actor = (Actor) super.clone(workspace);
    actor.expertParameters = new ArrayList<Parameter>();
    for (Parameter p : this.expertParameters) {
      try {
        Parameter clonedP = (Parameter) actor.getAttribute(p.getName(), Parameter.class);
        if (clonedP != null) {
          actor.expertParameters.add(clonedP);
        }
      } catch (IllegalActionException e) {
        getLogger().error("Error cloning expertParameters", e);
      }
    }
    actor.configurableParameters = new ArrayList<Parameter>();
    for (Parameter p : this.configurableParameters) {
      try {
        Parameter clonedP = (Parameter) actor.getAttribute(p.getName(), Parameter.class);
        if (clonedP != null) {
          actor.configurableParameters.add(clonedP);
        }
      } catch (IllegalActionException e) {
        getLogger().error("Error cloning configurableParameters", e);
      }
    }

    actor.actorMsgHeaders = new HashMap<String, String>();
    actor.actorMsgHeaders.put(ManagedMessage.SystemHeader.HEADER_SOURCE_REF, actor.getFullName());

    actor.statistics = new ActorStatistics(actor);
    return actor;
  }

  /**
   * Overridable method to construct customizable port handlers, that will be registered on each push-input-port.
   * 
   * @param p
   * @return new PortHandler
   */
  protected PortHandler createPortHandler(Port p) {
    // A dirty way to determine that we're in a PN-like domain,
    // where we need to add "active" PortHandlers with threads per input channel.
    // In event-driven domains, we don't need multithreaded PortHandlers...
    boolean needActiveHandlers = isInProcessDomain();
    return new PortHandler(p, needActiveHandlers);
  }

  final protected boolean isInProcessDomain() {
    return getDirector() instanceof ProcessDirector;
  }

  final protected PortHandler createPortHandler(Port p, PortListener portListener) {
    PortHandler pH = createPortHandler(p);
    pH.setListener(portListener);
    return pH;
  }
}