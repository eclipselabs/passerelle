package com.isencia.passerelle.process.actor.flow;

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
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.util.ExecutionTracerService;

public class BufferedTrigger extends Actor {

	private static final long serialVersionUID = 1L;
	
	public Port input;		// NOSONAR
	public Port output;		// NOSONAR

	public BufferedTrigger(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		input = PortFactory.getInstance().createInputPort(this, PortMode.PUSH,
				null);
		output = PortFactory.getInstance().createOutputPort(this);
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request,
			ProcessResponse response) throws ProcessingException {
		ExecutionTracerService.trace(this, "sending trigger");
		response.addOutputMessage(output, createTriggerMessage());

	}

}
