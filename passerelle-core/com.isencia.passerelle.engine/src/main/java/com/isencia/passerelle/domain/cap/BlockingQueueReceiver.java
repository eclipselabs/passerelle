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

package com.isencia.passerelle.domain.cap;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageProvider;

/**
 * DOCUMENT ME!
 * 
 * @version $Id: BlockingQueueReceiver.java,v 1.6 2005/10/28 14:06:18 erwin Exp $
 * @author Dirk Jacobs
 */
public class BlockingQueueReceiver extends QueueReceiver implements ProcessReceiver, MessageProvider {
  // ~ Instance variables _____________________________________________________________________________________________________________________________________

  private final static Logger logger = LoggerFactory.getLogger(BlockingQueueReceiver.class);

  private BoundaryDetector _boundaryDetector;
  private boolean _terminate = false;

  private int sizeWarningThreshold;

  private MessageBuffer buffer;

  // ~ Constructors ___________________________________________________________________________________________________________________________________________

  /**
   * Construct an empty receiver with no container
   */
  public BlockingQueueReceiver() {
    super();
    _boundaryDetector = new BoundaryDetector(this);
  }

  /**
   * Construct an empty receiver with the specified container.
   * 
   * @param container The container of this receiver.
   * @exception IllegalActionException If the container does not accept this receiver.
   */
  public BlockingQueueReceiver(IOPort container) throws IllegalActionException {
    super(container);
    _boundaryDetector = new BoundaryDetector(this);
  }

  /**
   * @param queue
   */
  public void setMessageBuffer(MessageBuffer buffer) {
    this.buffer = buffer;
    if (buffer != null) buffer.registerMessageProvider(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isConnectedToBoundary() {
    return _boundaryDetector.isConnectedToBoundary();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isConnectedToBoundaryInside() {
    return _boundaryDetector.isConnectedToBoundaryInside();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isConnectedToBoundaryOutside() {
    return _boundaryDetector.isConnectedToBoundaryOutside();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isConsumerReceiver() {
    if (isConnectedToBoundary()) {
      return true;
    }

    return false;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isInsideBoundary() {
    return _boundaryDetector.isInsideBoundary();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isOutsideBoundary() {
    return _boundaryDetector.isOutsideBoundary();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isProducerReceiver() {
    if (isOutsideBoundary() || isInsideBoundary()) {
      return true;
    }

    return false;
  }

  /**
   * @return false
   */
  public boolean isReadBlocked() {
    return false;
  }

  /**
   * @return false
   */
  public boolean isWriteBlocked() {
    return false;
  }

  /**
   * Get the next token from the shared queue, or from the private queue when no shared queue was set. If this receiver has been terminated (e.g. via
   * requestFinish()), and no tokens are queued anymore, null is returned. If the receiver is not terminated, this method blocks till a token has been received
   * in the queue.
   * 
   * @return the next token received, or null when the receiver has finished and all tokens have been retrieved already.
   */
  public Token get() {
    Workspace workspace = getContainer().workspace();
    Token result = null;
    if (buffer != null) {
      throw new UnsupportedOperationException("get() not supported for shared buffer");
    } else {
      synchronized (this) {
        while (isPaused() || (!_terminate && !super.hasToken())) {
          try {
            workspace.wait(this, 1000);
          } catch (InterruptedException e) {
          }
        }

        if (super.hasToken()) {
          result = super.get();
        }
      }
    }
    return result;
  }

  private boolean isPaused() {
    try {
      Manager manager = ((CompositeActor) getContainer().toplevel()).getManager();
      return Manager.PAUSED.equals(manager.getState());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Passerelle BlockingQueueReceiver always has place for more. No Max capacity, just a warning level notification.
   * 
   * @return true
   */
  @Override
  public boolean hasRoom() {
    return true;
  }

  /**
   * Passerelle BlockingQueueReceiver always has place for more. No Max capacity, just a warning level notification.
   * 
   * @return true
   */
  @Override
  public boolean hasRoom(int tokens) {
    return true;
  }

  /**
   * Following Ptolemy PN rules, hasToken() must always return true.
   * 
   * @return true
   */
  @Override
  public boolean hasToken() {
    return true;
  }

  /**
   * Following Ptolemy PN rules, hasToken() must always return true.
   * 
   * @return true
   */
  @Override
  public boolean hasToken(int tokens) {
    return true;
  }

  @Override
  public List<Token> elementList() {
    if (buffer != null) {
      throw new UnsupportedOperationException("elementList() not supported for shared buffer");
    } else {
      return super.elementList();
    }
  }

  @Override
  public int getCapacity() {
    if (buffer != null) {
      return 0;
    } else {
      return super.getCapacity();
    }
  }

  @Override
  public void setCapacity(int capacity) throws IllegalActionException {
    if (buffer != null) {
      throw new UnsupportedOperationException("setCapacity() not supported for shared buffer");
    } else {
      super.setCapacity(capacity);
    }
  }

  @Override
  public int size() {
    if (buffer != null) {
      return buffer.getMessageQueue().size();
    } else {
      return super.size();
    }
  }

  /**
   * Store the token in the queue. If the size warning threshold has been reached, a warning msg is logged.
   * 
   * @param token
   */
  @Override
  public void put(Token token) {
    synchronized (this) {
      if (_terminate) {
        return;
      } else {
        if (buffer != null) {
          try {
            if (getContainer() instanceof Port) {
              Port _p = (Port) getContainer();
              try {
                token = _p.convertTokenForMe(token);
                _p.getStatistics().acceptReceivedMessage(null);
              } catch (Exception e) {
                throw new RuntimeException("Failed to convert token " + token, e);
              }
            }
            ManagedMessage msg = MessageHelper.getMessageFromToken(token);
            MessageInputContext ctxt = new MessageInputContext(0, getContainer().getName(), msg);
            buffer.offer(ctxt);
          } catch (PasserelleException e) {
            throw new RuntimeException("Failed to interpret token " + token, e);
          }
        } else {
          // token can be put in the queue;
          super.put(token);
        }
      }
      // Wake up all threads waiting on a write to this receiver;
      notifyAll();
      if (getSizeWarningThreshold() != QueueReceiver.INFINITE_CAPACITY && size() >= getSizeWarningThreshold()) {
        logger.warn(getContainer().getFullName() + " - reached/passed warning threshold size " + getSizeWarningThreshold());
      }
    }
  }

  /**
   * Sets the terminate flag. From then, no new tokens are stored and after all tokens have been retrieved, a null token is returned as an indicator that this
   * receiver is finished.
   */
  public synchronized void requestFinish() {
    if (logger.isTraceEnabled()) {
      logger.trace("requestFinish() - entry - for " + toString() + " in " + this.getContainer().getFullName());
    }
    _terminate = true;
    notifyAll();
    if (buffer != null) buffer.unregisterMessageProvider(this);
    if (logger.isTraceEnabled()) {
      logger.trace("requestFinish() - exit");
    }
  }

  @Override
  public void reset() {
    _terminate = false;
    _boundaryDetector.reset();
  }

  public void setSizeWarningThreshold(int qWarningSize) {
    this.sizeWarningThreshold = qWarningSize;
  }

  public int getSizeWarningThreshold() {
    return sizeWarningThreshold;
  }
}