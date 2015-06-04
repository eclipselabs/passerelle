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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * A <code>Join</code> should be used in a combination with a preceding {@link MessageSequenceGenerator}. Where the
 * {@link MessageSequenceGenerator} is used to generate multiple derived messages in a sequence, the <code>Join</code>
 * is used to assemble and merge them again.
 * <p>
 * Two typical use cases can be distinguished :
 * <ul>
 * <li>The <code>MessageSequenceGenerator</code> is a <code>Fork</code> actor. Then the message sequence will be
 * processed in actors in parallel branches. The branches should all end up in a <code>Join</code> actor to merge the
 * processing results into one output message.</li>
 * <li>The <code>MessageSequenceGenerator</code> is a <code>Splitter</code> actor. Then the message sequence is sent
 * through a single branch that is applying the same processing steps on each message sequentially. A <code>Join</code>
 * actor can be used also in this case, to aggregate all results into one message again.</li>
 * </ul>
 * </p>
 * <p>
 * When the results of all processing branches have been received, the aggregated message, typically containing the
 * union of all executed tasks and obtained results, is sent out via the output port.
 * </p>
 * 
 * @author erwin
 */
public class Join extends Actor {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(Join.class);

  public Port mergeInput; // NOSONAR
  public Port output; // NOSONAR

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Join(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    mergeInput = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void process(ProcessManager processManager, com.isencia.passerelle.process.actor.ProcessRequest request, com.isencia.passerelle.process.actor.ProcessResponse response)
      throws ProcessingException {
    Iterator<MessageInputContext> msgInputCtxtItr = request.getAllInputContexts();
    while (msgInputCtxtItr.hasNext()) {
      MessageInputContext inputContext = (MessageInputContext) msgInputCtxtItr.next();
      if (!inputContext.isProcessed()) {
        if (mergeInput.getName().equals(inputContext.getPortName())) {
          Iterator<ManagedMessage> msgIterator = inputContext.getMsgIterator();
          while (msgIterator.hasNext()) {
            ManagedMessage branchedMsg = msgIterator.next();
            ManagedMessage mergedMessage = mergeMessage(processManager,branchedMsg);
            if (mergedMessage != null) {
              response.addOutputMessage(output, mergedMessage);
            }
          }
        }
      }
    }
  }

  private ManagedMessage mergeMessage(ProcessManager processManager,ManagedMessage branchedMsg) throws ProcessingException {
    String seqGeneratorName = branchedMsg.getSingleHeader(MessageSequenceGenerator.HEADER_SEQ_SRC);
    if (seqGeneratorName!=null) {
      Entity seqGenerator = ((CompositeEntity) getContainer()).getEntity(seqGeneratorName);
      if (seqGenerator != null) {
        return ((MessageSequenceGenerator) seqGenerator).aggregateProcessedMessage(processManager,branchedMsg);
      }
    }
    return null;
  }

  @Override
  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      if (message.getBodyContent() instanceof Context) {
        Context diagnosisContext = (Context) message.getBodyContent();
        return port.getFullName() + " - msg for request " + diagnosisContext.getRequest().getId();
      } else {
        return super.getAuditTrailMessage(message, port);
      }
    } catch (MessageException e) {
      getLogger().error("Error getting msg content", e);
      return super.getAuditTrailMessage(message, port);
    }
  }
}
