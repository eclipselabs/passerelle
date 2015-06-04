/* Copyright 2012 - iSencia Belgium NV

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
package com.isencia.passerelle.actor.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
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
import com.isencia.passerelle.message.internal.ErrorMessageContainer;

/**
 * An actor that extracts the ManagedMessage from ErrorMessages received
 * on its input port, and sends it out.
 * It can also send out the description of the contained PasserelleException
 * via the <code>errorDescrOutput</code> port.
 *  
 * The error can optionally be logged.
 * <p>
 * This is useful to link to an error-output of one or more actors,
 * to implement an "ignore error" behaviour, combined with continued
 * sequence operations "as-if-everything-was-OK".
 * 
 * In this respect it differs from the plain DevNullActor, which
 * discards the received msg and thus effectively interrupts the sequence.
 * </p>
 * <p>
 * If the received message is not an ErrorMessage, or it does not
 * contain a ManagedMessage as the error's context, it is just forwarded
 * to the output as is.
 * </p>
 * @author delerw
 *
 */
public class ErrorCatcher extends Actor {

	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = LoggerFactory.getLogger(ErrorCatcher.class);

	public Port input;
	public Port output;
	public Port errorDescrOutput;
	public Parameter logReceivedMessages;

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public ErrorCatcher(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		input = PortFactory.getInstance().createInputPort(this, null);
		output = PortFactory.getInstance().createOutputPort(this);
		errorDescrOutput = PortFactory.getInstance().createOutputPort(this, "errorDescrOutput");
		
		logReceivedMessages = new Parameter(this,"Log received messages", new BooleanToken(false));
		logReceivedMessages.setTypeEquals(BaseType.BOOLEAN);
		new CheckBoxStyle(logReceivedMessages, "checkbox");

		_attachText("_iconDescription", 
				"<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:white;stroke:lightgrey\"/>\n" +
                "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" style=\"stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" style=\"stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n" +
                "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" style=\"stroke-width:1.0;stroke:black\"/>\n" +
                "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n" +
                "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" style=\"stroke-width:1.0;stroke:grey\"/>\n" +

                "<rect x=\"-6\" y=\"-5\" width=\"12\" height=\"15\" style=\"fill:lightgrey;stroke:grey\"/>\n" +
                "<line x1=\"-1\" y1=\"-9\" x2=\"1\" y2=\"-9\" style=\"stroke-width:2.0\"/>\n" +
                "<line x1=\"-7\" y1=\"-7\" x2=\"7\" y2=\"-7\" style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"-2\" y1=\"-5\" x2=\"-2\" y2=\"10\" style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"2\" y1=\"-5\" x2=\"2\" y2=\"10\" style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"-5\" y1=\"12\" x2=\"5\" y2=\"12\" style=\"stroke-width:2.0\"/>\n" +
                "</svg>\n");
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
		ManagedMessage msg = request.getMessage(input);
		ManagedMessage outputMsg = msg;
		try {
			if(msg instanceof ErrorMessageContainer) {
				ErrorMessageContainer errorMsg = (ErrorMessageContainer)msg; 
				Object context = errorMsg.getContext();
				if(context!=null && context instanceof ManagedMessage) {
					outputMsg = (ManagedMessage) context;
				}
				if(errorMsg.getException()!=null) {
  				String errDescr = errorMsg.getException().getMessage();
  				if(errDescr!=null) {
  				  try {
              ManagedMessage errDescrOutputMsg = createMessage();
              errDescrOutputMsg.setBodyContentPlainText(errDescr);
              response.addOutputMessage(errorDescrOutput, errDescrOutputMsg);
            } catch (MessageException e) {
              getLogger().error("Error sending error description",e);
            }
  				}
				}
				if(((BooleanToken)logReceivedMessages.getToken()).booleanValue()) {
				  getLogger().info("Discarding error for context "+errorMsg.getContext(),errorMsg.getRootException());
				}
			}
		} catch (IllegalActionException e) {
			getLogger().error("Error reading parameter value",e);
		} finally {
			response.addOutputMessage(output, outputMsg);
		}
	}

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }
}
