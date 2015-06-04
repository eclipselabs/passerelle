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

package com.isencia.passerelle.domain.et.impl;

import java.util.LinkedList;
import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.domain.et.EventRefusedException;
import com.isencia.passerelle.domain.et.SendEvent;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageBuffer;
import com.isencia.passerelle.message.MessageHelper;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.message.MessageProvider;

/**
 * @author delerw
 */
public class ETReceiver extends AbstractReceiver implements MessageProvider {

  private LinkedList<Token> tokens = new LinkedList<Token>();
  
  private ETDirector director;

  // When this receiver is linked to a collecting MessageBuffer, 
  // all tokens that are put() here are directly forwarded to the buffer.
  private MessageBuffer buffer;

  public ETReceiver(ETDirector director) {
    this.director = director;
  }

  /**
   * @param container
   * @throws IllegalActionException
   */
  public ETReceiver(IOPort container, ETDirector director) throws IllegalActionException {
    super(container);
    this.director = director;
  }

  /** Clear this receiver of any contained tokens.
   */
  public void clear() {
      tokens.clear();
  }

  /**
   * @return the first/oldest token in the receiver.
   * @exception NoTokenException If there are no more tokens. This is a runtime
   *              exception, so it need not to be declared explicitly.
   */
  @Override
  public Token get() throws NoTokenException {
    if (buffer != null) {
      throw new UnsupportedOperationException("get() not supported for shared buffer");
    } else if (tokens.isEmpty()) {
      throw new NoTokenException(getContainer(), "No more tokens in the ET receiver.");
    }
    return (Token) tokens.removeFirst();
  }

  @Override
  public boolean hasRoom() {
    return true;
  }

  @Override
  public boolean hasRoom(int numberOfTokens) {
    return true;
  }

  @Override
  public boolean hasToken() {
    return (!tokens.isEmpty());
  }

  @Override
  public boolean hasToken(int numberOfTokens) {
    return (tokens.size() >= numberOfTokens);
  }

  @Override
  public void put(Token token) throws IllegalActionException{
    // Is it possible to determine the sending port here?
    // I don't think so...
    // Alternatively we could generate this event in the sending Port??
    SendEvent evt = new SendEvent(token, null, (Port) getContainer());
    if(token==null) {
      return;
    } else if (buffer != null) {
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
        MessageInputContext ctxt = new ETMessageInputContext(evt, 0, getContainer().getName(), msg);
        buffer.offer(ctxt);
      } catch (PasserelleException e) {
        throw new RuntimeException("Failed to interpret token " + token, e);
      }
    } else {
      tokens.add(token);
    }
    try {
      director.enqueueEvent(evt);
    } catch (EventRefusedException e) {
      throw new IllegalActionException(null, e, null);
    }
  }

  public synchronized void requestFinish() {
    if (buffer != null) buffer.unregisterMessageProvider(this);
  }

  /**
   * @param queue
   */
  public void setMessageBuffer(MessageBuffer buffer) {
    this.buffer = buffer;
    if (buffer != null) buffer.registerMessageProvider(this);
  }
}
