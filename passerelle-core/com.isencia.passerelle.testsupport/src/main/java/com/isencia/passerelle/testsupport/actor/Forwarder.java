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
package com.isencia.passerelle.testsupport.actor;

import java.util.Iterator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

@SuppressWarnings("serial")
public class Forwarder extends Actor {

  public Port input;
  public Port output;

  public Forwarder(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    doProcess(ctxt, request, response);
  }

  protected void doProcess(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    Iterator<ManagedMessage> allMessages = request.getAllMessages(input);
    while (allMessages.hasNext()) {
      ManagedMessage receivedMsg = (ManagedMessage) allMessages.next();
      // Create a new outgoing msg, "caused by" the received input msg
      // and for the rest a complete copy of the received msg
      try {
        ManagedMessage outputMsg = MessageFactory.getInstance().createCausedCopyMessage(receivedMsg);
        response.addOutputMessage(output, outputMsg);
      } catch (MessageException e) {
        throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error constructing copy from received message from value parameter", this,
            receivedMsg, e);
      }
    }
  }
}
