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

package com.isencia.passerelle.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.domain.cap.ProcessThread;
import com.isencia.passerelle.util.LoggerManager;

/**
 * A PortHandler offers a cross-domain encapsulation of the specific mechanisms needed to obtain a message from an input port.
 * <p>
 * For Passerelle, the desired behaviour of a multi-channel input port is that the actor is able to react per received message on any channel. I.e. not stay
 * blocked until each channel has received at least one message/token.
 * </p>
 * <p>
 * In process-like domains, Ptolemy natively does not permit to do this, as the only way to effectively check if a message is available on a channel is through
 * a blocking <code>Port.get(i)</code> per channel.
 * </p>
 * <p>
 * Passerelle's PortHandler offers several ways of bypassing this limitation :
 * <ul>
 * <li>Old approach in process domains : each multi-channel port gets a PortHandler that creates a "reader" thread per input channel. If a channel has received
 * a message, it gets "popped" by the assigned thread. All such messages are collected in an intermediate queue. Through the PortHandler wrapper, actors
 * effectively read messages from this queue, i.o. per channel. For single-channel ports, the PortHandler can function without extra threads and extra queue,
 * and just reads from the single channel directly.<br/>
 * Furthermore, these older Actor base classes require at least one PULL input port to prevent uncontrolled infinite fire() loops.</li>
 * <li>Not-so-old approach in process domains : Actors based on <code>com.isencia.passerelle.actor.v5.Actor</code> have an optimized mechanism in place for
 * multi-channel PUSH input ports, without the need for extra threads per input channel. Through a "collaboration" between Passerelle's specialized Port and
 * v5.Actor, input channels directly feed their received message tokens into a single collecting queue per Port. <br/>
 * For PULL input ports nothing has changed however, except that a v5.Actor is perfectly able to function with only PUSH ports. But if there are multi-channel
 * PULL input ports, they will still lead to extra threads.</li>
 * <li>New approach in event domains : Message dispatching is done by a central dispatcher that could have its own thread-pool in place. In this domain it is
 * possible to check if a message is available on a channel without requiring a blocking <code>Port.get(i)</code> per channel. This allows to handle
 * multi-channel PUSH and PULL ports without extra threads.</li>
 * </ul>
 * The PortHandler hides the exact implementations for these options, and offers a single easy interface to get messages from input ports. In a process domain,
 * it will generate <code>ChannelHandler</code>s per input channel when needed. In an event domain, it will just iterate over the channels without needing extra
 * threads.
 * </p>
 * 
 * @author erwin
 */
public class PortHandler {
  // ~ Static variables/initializers

  protected static Logger LOGGER = LoggerFactory.getLogger(PortHandler.class);
  // for logging MDC
  protected String actorInfo = "none";

  protected BlockingQueue<Token> queue = null;
  protected Port ioPort = null;
  protected Object channelLock = new Object();
  protected PortListener listener = null;
  protected Thread[] channelHandlers = null;
  protected boolean started = false;

  // A counter for channels that are still active
  // When this counter reaches 0 again, it means the handler
  // can stop.
  protected int channelCount = 0;

  // flag if we're in a process domain, when it may be needed to launch channel handlers.
  private boolean inProcessDomain = true;

  /**
   * @param ioPort
   */
  public PortHandler(Port ioPort) {
    this.ioPort = ioPort;
    channelCount = getWidth();
    queue = new LinkedBlockingQueue<Token>();
    Nameable actor = ioPort.getContainer();
    if (actor != null) {
      actorInfo = ((NamedObj) actor).getFullName();
    }
  }

  /**
   * @param ioPort
   * @param inProcessDomain
   */
  public PortHandler(Port ioPort, boolean inProcessDomain) {
    this(ioPort);
    this.inProcessDomain = inProcessDomain;
  }

  /**
   * @param ioPort
   * @param listener
   * @param inProcessDomain
   */
  public PortHandler(Port ioPort, PortListener listener, boolean inProcessDomain) {
    this(ioPort, listener);
    this.inProcessDomain = inProcessDomain;
  }

  /**
   * Creates a new PortHandler object.
   * 
   * @param ioPort
   * @param listener
   *          an object interested in receiving messages from the handler in push mode
   */
  public PortHandler(Port ioPort, PortListener listener) {
    this(ioPort);
    this.listener = listener;
  }

  /**
   * @param listener
   *          an object interested in receiving messages from the handler in push mode
   */
  public void setListener(PortListener listener) {
    this.listener = listener;
  }

  /**
   * @return flag indicating whether the handler is already running...
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * @return the port behind this handler
   */
  public Port getPort() {
    return ioPort;
  }

  /**
   * Returns a message token received by this handler. For process domains this method blocks until either:
   * <ul>
   * <li>a message has been received
   * <li>the message channels are all exhausted. In this case a null token is returned.
   * <ul>
   * For event/other domains, this method does not block, but can return :
   * <ul>
   * <li>a token with a message
   * <li>an empty token, i.e. Token.NIL, when the port has no message but may still receive msgs in the future.
   * <li>null : the port's message channels are all exhausted.
   * <ul>
   * 
   * @return a message token received by the handler
   */
  public Token getToken() {
    LOGGER.trace("{} - getToken() - entry", getPort().getFullName());

    if (hasNoMoreTokens()) {
      LOGGER.debug("{} - getToken() - has no more tokens", getPort().getFullName());
      return null;
    }

    Token token = Token.NIL;
    if (mustUseHandlers()) {
      // messages will be in the queue
      try {
        token = (Token) queue.take();
        if (Token.NIL.equals(token) || PasserelleToken.POISON_PILL.equals(token)) {
          // indicates a terminating system
          queue.offer(token);
          LOGGER.debug("{} - getToken() - got a termination token {}", getPort().getFullName(), token);
          token = null;
        }
      } catch (InterruptedException e) {
        LOGGER.error("Token queue access was interrupted", e);
      }
    } else {
      // just read the port directly
      token = readTokenFromPort();
    }
    LOGGER.trace("{} - getToken() - exit - token : {} ", getPort().getFullName(), token);
    return token;
  }

  protected Token readTokenFromPort() {
    Token token = Token.NIL;
    for (int i = 0; i < ioPort.getWidth(); ++i) {
      boolean channelIsDead = false;
      try {
        if (ioPort.hasToken(i)) {
          token = ioPort.get(i);
          if (token != null && !PasserelleToken.POISON_PILL.equals(token)) {
            break;
          }
          if (token == null) {
            channelIsDead = true;
          }
        }
      } catch (NoTokenException e) {
        channelIsDead = true;
      } catch (Exception e) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(getPort().getFullName()+" - readTokenFromPort() exception", e);
        }
        channelIsDead = true;
      }
      if (channelIsDead) {
        synchronized (channelLock) {
          channelCount--;
        }
      }
    }
    return token;
  }

  /**
   * @return the number of message channels connected to the port that is handled by this handler
   */
  public int getWidth() {
    return ioPort.getWidth();
  }

  /**
   * @return the name of the io port handled by this handler
   */
  public String getName() {
    return (ioPort != null ? ioPort.getName() : "");
  }

  /**
   * @return flag indicating whether there are tokens in the message queue for this handler
   */
  protected boolean hasNoMoreTokens() {
    synchronized (channelLock) {
      return (channelCount == 0) && queue.isEmpty();
    }
  }

  /**
   * Get the handler running...
   */
  public void start() {
    LOGGER.trace("{} - start() - entry", getName());
    if (started) {
      LOGGER.trace("{} - start() - ALREADY STARTED - exit", getName());
      return;
    }

    // calculate it here (again), as the nr of channels might have changed between
    // construction time and start time
    channelCount = getWidth();

    if (mustUseHandlers()) {
      channelHandlers = new Thread[getWidth()];

      for (int i = 0; i < getWidth(); i++) {
        channelHandlers[i] = createChannelHandler(i);
        channelHandlers[i].start();
      }
    }

    started = true;

    LOGGER.trace("{} - start() - STARTED - exit", getName());
  }

  /**
   * If the port has a shared message buffer, or channelcount is 0 or 1, no extra handler threads are needed unless a listener has been registered.
   * 
   * @return flag indicating whether extra handler threads must be used.
   */
  protected boolean mustUseHandlers() {
    if (listener != null)
      return true;

    if (ioPort instanceof Port) {
      Port _p = (Port) ioPort;
      if (_p.getMessageBuffer() != null) {
        return false;
      }
    }

    return inProcessDomain && (getWidth() > 1);
  }

  /**
   * Override to provide alternative implementation.
   * 
   * @param index
   * @return
   */
  protected Thread createChannelHandler(final int index) {
    return new ChannelHandler(index);
  }

  public final class ChannelHandler extends Thread {
    private Token token = null;
    private boolean terminated = false;
    private int channelIndex = 0;

    public ChannelHandler(int channelIndex) {
      this.channelIndex = channelIndex;
    }

    public void run() {
      try {
        LoggerManager.pushMDC(ProcessThread.ACTOR_MDC_NAME, actorInfo);

        LOGGER.debug("{} - ChannelHandler.{} - run() - entry", PortHandler.this.ioPort.getFullName(), channelIndex);

        while (!terminated) {
          fetch();
        }
        synchronized (channelLock) {
          channelCount--;
        }

        // No more channels active
        // Force queue to return
        if (channelCount == 0) {
          queue.offer(Token.NIL);
          if (listener != null) {
            listener.noMoreTokens();
          }
        }

        LOGGER.debug("{} - ChannelHandler.{} - run() - exit", PortHandler.this.ioPort.getFullName(), channelIndex);
      } catch (Throwable t) { // NOSONAR - need to make sure any exception that breaks the run() loop is logged
        LOGGER.error(PortHandler.this.ioPort.getFullName() + " - Error in ChannelHandler", t);
        throw new RuntimeException(t);
      } finally {
        LoggerManager.popMDC(ProcessThread.ACTOR_MDC_NAME);
      }
    }

    private void fetch() {
      LOGGER.trace("{} - ChannelHandler.{} - fetch() - entry", PortHandler.this.ioPort.getFullName(), channelIndex);

      try {
        if (ioPort.hasToken(channelIndex)) {
          token = ioPort.get(channelIndex);

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(PortHandler.this.ioPort.getFullName() + " ChannelHandler." + channelIndex + " fetch() - got token : " + token);
          }

          if (token == null) {
            terminated = true;
          } else if (!token.isNil()) {
            queue.offer(token);
            if (listener != null) {
              listener.tokenReceived();
            }
          }
        } else if (ioPort.isExhausted()) {
          terminated = true;
        }

      } catch (TerminateProcessException e) {
        terminated = true;
      } catch (IllegalActionException e) {
        terminated = true;
      } catch (NoTokenException e) {
        terminated = true;
      }

      LOGGER.trace("{} - ChannelHandler.{} - fetch() - exit", PortHandler.this.ioPort.getFullName(), channelIndex);
    }
  }
}