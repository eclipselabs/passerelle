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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.data.Token;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageProvider;

public class CapReceiver extends PNQueueReceiver implements MessageProvider {

  private MessageBuffer buffer;
  private volatile boolean _terminate;

  /**
   * Construct an empty receiver with no container
   */
  public CapReceiver() {
    super();
  }

  /**
   * Construct an empty receiver with the specified container.
   * 
   * @param container
   *          The container of this receiver.
   * @exception IllegalActionException
   *              If the container does not accept this receiver.
   */
  public CapReceiver(IOPort container) throws IllegalActionException {
    super(container);
  }

  /**
   * @param queue
   */
  public void setMessageBuffer(MessageBuffer buffer) {
    this.buffer = buffer;
    if (buffer != null)
      buffer.registerMessageProvider(this);
  }

  @Override
  public Token get() {
    Workspace workspace = getContainer().workspace();
    Token result = null;
    if (buffer != null) {
      throw new UnsupportedOperationException("get() not supported for shared buffer");
    } else {
      synchronized (this) {
        while (isPaused()) {
          try {
            workspace.wait(this, 1000);
          } catch (InterruptedException e) {
          }
        }
      }
      result = super.get();
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
    if (buffer == null) {
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
    if (buffer != null && !_terminate) {
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

  @Override
  public void requestFinish() {
    _terminate = true;
    if (buffer != null) {
      buffer.unregisterMessageProvider(this);
    }
    super.requestFinish();
  }

  @Override
  public void reset() {
    _terminate = false;
    super.reset();
  }
}