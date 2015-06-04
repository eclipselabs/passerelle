/* Copyright 2013 - iSencia Belgium NV

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
import java.util.List;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageOutputContext;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * In the new Passerelle Actor API, the ProcessResponse is a generic container for all response messages, and the ports on which they should be sent. <br>
 * The actor implementation can choose/combine 2 modes for sending results:
 * <ul>
 * <li>as individual messages via the addOutputMessage()/addOutputContext()
 * <li>as entries in a sequence via addOutputMessageInSequence()/addOutputContextInSequence(). All messages registered via these methods are sent in 1 sequence,
 * in the order that they have been added.
 * </ul>
 * 
 * @author erwin
 */
public class ProcessResponse {

  private List<MessageOutputContext> outputs = new ArrayList<MessageOutputContext>();
  private List<MessageOutputContext> outputsInSequence = new ArrayList<MessageOutputContext>();

  private ProcessRequest request;
  private ProcessingException exception;

  public ProcessResponse(ProcessRequest request) {
    this.request = request;
  }

  public boolean addOutputMessage(Port outputPort, ManagedMessage outputMsg) {
    if(outputPort==null || outputMsg==null) {
      return false;
    } 
    if (request.isMessageInContext(outputMsg)) {
      outputs.add(new MessageOutputContext(outputPort, outputMsg));
      return true;
    } else {
      return false;
    }
  }

  public boolean addOutputMessageInSequence(Port outputPort, ManagedMessage outputMsg) {
    if(outputPort==null || outputMsg==null) {
      return false;
    } 
    if (request.isMessageInContext(outputMsg)) {
      outputsInSequence.add(new MessageOutputContext(outputPort, outputMsg));
      return true;
    } else {
      return false;
    }
  }

  public MessageOutputContext[] getOutputs() {
    return outputs.toArray(new MessageOutputContext[outputs.size()]);
  }

  public MessageOutputContext[] getOutputsInSequence() {
    return outputsInSequence.toArray(new MessageOutputContext[outputsInSequence.size()]);
  }

  /**
   * @return null or the exception that has occurred during the processing of the request.
   */
  public ProcessingException getException() {
    return exception;
  }

  public void setException(ProcessingException exception) {
    this.exception = exception;
  }

  /**
   * @return the request for which this is the response
   */
  public ProcessRequest getRequest() {
    return request;
  }
  
  /**
   * 
   * @return the manager for handling the request processing lifecycle
   */
  public ProcessManager getProcessManager() {
    return getRequest().getProcessManager();
  }

  public String toString() {
    StringBuffer bfr = new StringBuffer();
    MessageOutputContext[] outputs = getOutputs();
    bfr.append("\n\tIndependent msgs:");
    for (int i = 0; i < outputs.length; i++) {
      MessageOutputContext context = outputs[i];
      bfr.append("\n\t\t" + context.getPort().getName() + ": msgID=" + context.getMessage().getID());
    }
    outputs = getOutputsInSequence();
    bfr.append("\n\tSequenced msgs:");
    for (int i = 0; i < outputs.length; i++) {
      MessageOutputContext context = outputs[i];
      bfr.append("\n\t\t" + context.getPort().getName() + ": msgID=" + context.getMessage().getID());
    }
    return bfr.toString();
  }
}
