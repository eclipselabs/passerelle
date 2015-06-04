package com.isencia.passerelle.testsupport.actor;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;

public class MultiBlockingInputActor extends Actor {
  private static final long serialVersionUID = 2209749466396103463L;
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiBlockingInputActor.class);

  public Port input1;
  public Port input2;

  public Port output;

  public MultiBlockingInputActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input1 = PortFactory.getInstance().createInputPort(this, "input1", null);
    input2 = PortFactory.getInstance().createInputPort(this, "input2", null);

    output = PortFactory.getInstance().createOutputPort(this);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    StringBuilder contentBldr = new StringBuilder("Iteration " + request.getIterationCount() + ":");
    Iterator<MessageInputContext> contexts = request.getAllInputContexts();
    while (contexts.hasNext()) {
      MessageInputContext context = (MessageInputContext) contexts.next();
      Iterator<ManagedMessage> msgIterator = context.getMsgIterator();
      while (msgIterator.hasNext()) {
        ManagedMessage msg = (ManagedMessage) msgIterator.next();
        contentBldr.append(context.getPortName() + ":" + msg.getID());
      }
    }
    String msg = contentBldr.toString();
    System.out.println(msg);
    try {
      ManagedMessage resultMsg = createMessage(msg, "text/plain");
      response.addOutputMessage(output, resultMsg);
    } catch (MessageException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
