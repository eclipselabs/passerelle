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
package com.isencia.passerelle.process.actor.flow;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * A <code>AggregationStrategy</code> that looks for processing <code>Context</code>s in the sequenced messages, and
 * aggregates their tasks and results.
 * 
 * @author erwin
 * 
 */
public class ContextAggregationStrategy implements AggregationStrategy {

  public ManagedMessage aggregateMessages(ProcessManager processManager, ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException {
    MessageContainer scopeMsg = (MessageContainer) initialMsg;
    ManagedMessage msg = scopeMsg.copy();
    Context mergedCtxt = getBranchedContextFor(processManager, initialMsg);
    Context[] branches = new Context[otherMessages.length];
    for (int i = 0; i < otherMessages.length; i++) {
      MessageContainer otherMsg = (MessageContainer) otherMessages[i];
      msg.addCauseID(otherMsg.getID());
      Object bodyContent = otherMsg.getBodyContent();
      if (bodyContent instanceof Context) {
        // this is for legacy flows/actors that still send the Context in the msg body
        Context branchedCtx = (Context) bodyContent;
        branches[i] = branchedCtx;
      } else {
        // this is for the new approach since 8.8, where Context identifiers are sent as msg headers
        String[] scopeGrp = otherMsg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE_GRP);
        String[] scope = otherMsg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE);
        Context branchedCtx = null;
        if(scopeGrp.length==1 && scope.length==1) {
          branchedCtx = processManager.removeScopedProcessContext(scopeGrp[0], scope[0]);
        }
        branches[i] = (branchedCtx!=null) ? branchedCtx : mergedCtxt;
      }
    }
    mergedCtxt.join(branches);
    return msg;
  }
  
  protected Context getBranchedContextFor(ProcessManager processManager, ManagedMessage msg) {
    String[] scopeGrp = msg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE_GRP);
    String[] scope = msg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE);
    Context branchedCtx = null;
    if(scopeGrp!=null && scope!=null && scopeGrp.length==1 && scope.length==1) {
      branchedCtx = processManager.getScopedProcessContext(scopeGrp[0], scope[0]);
    }
    return (branchedCtx!=null) ? branchedCtx : processManager.getRequest().getProcessingContext();
  }
}
