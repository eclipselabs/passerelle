/**
 * 
 */
package com.isencia.passerelle.process.actor.flow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.model.util.ModelUtils;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * <p>
 * An actor with configurable named output ports, that sends out copies of the incoming Context on each outgoing port.
 * The original Context is sent on the default output port.
 * </p>
 * <p>
 * This is useful to ensure that parallel branches in a sequence don't see each others intermediate results.
 * </p>
 * <p>
 * The <code>Fork</code> is typically used with the <code>Join</code>, to merge the results of the parallel branches
 * into one Context again.
 * </p>
 * 
 * @author erwin
 * 
 */
public class Fork extends AbstractMessageSequenceGenerator {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(Fork.class);

  public Port input;
  public OutputPortConfigurationExtender outputPortCfgExt;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public Fork(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name, true);
    input = PortFactory.getInstance().createInputPort(this, null);
    outputPortCfgExt = new OutputPortConfigurationExtender(this, "output port configurer");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }


  @Override
  protected String getAuditTrailMessage(ManagedMessage message, Port port) {
    try {
      if (message.getBodyContent() instanceof Context) {
        Context processContext = (Context) message.getBodyContent();
        return port.getFullName() + " - msg for request " + processContext.getRequest().getId();
      } else {
        return super.getAuditTrailMessage(message, port);
      }
    } catch (MessageException e) {
      getLogger().error("Error getting msg content", e);
      return super.getAuditTrailMessage(message, port);
    }
  }

  @Override
  public void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response)
      throws ProcessingException {
    MessageContainer message = (MessageContainer) request.getMessage(input);
    if (message != null) {
      try {
        Context processContext = ProcessRequest.getContextForMessage(processManager, message);
        Long scopeId = processManager.getRequest().getId();
        registerSequenceScopeMessage(scopeId, message);

        try {
          getAuditLogger().info("Forking msg with scope " + scopeId + " : " + getAuditTrailMessage(message, input));
        } catch (Exception e) {
          getLogger().error("Error logging audit trail", e);
        }

        String myName = ModelUtils.getFullNameButWithoutModelName((CompositeActor) toplevel(), this);
        List<Port> outputPorts = outputPortCfgExt.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); ++i) {
          Port p = outputPorts.get(i);
          String scopeName = p.getName();
          Context newOne = processContext.fork();
          processManager.registerScopedProcessContext(myName, scopeName, newOne);
          MessageContainer outputMsg = (MessageContainer) MessageFactory.getInstance().createMessageCloneInSequence(message, 
              processContext.getRequest().getId(), // sequence; should we be more specific than on request ID level?
              new Long(i), // sequence position
              (i == (outputPorts.size() - 1))); // end of sequence?
          // the SEQ SRC name can be the simple actor name, as the Join is always looking for it in the same (sub)flow-level
          outputMsg.setHeader(HEADER_SEQ_SRC, getName());
          // the CTXT SCOPE GRP must be unique across a complete flow to ensure that concurrent fork/join-s on different flow levels can not clash
          outputMsg.setHeader(ProcessRequest.HEADER_CTXT_SCOPE_GRP, myName);
          outputMsg.setHeader(ProcessRequest.HEADER_CTXT_SCOPE, scopeName);
          response.addOutputMessage(p, outputMsg);
        }
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating forked messages", this, message, e);
      }
    }

  }

}
