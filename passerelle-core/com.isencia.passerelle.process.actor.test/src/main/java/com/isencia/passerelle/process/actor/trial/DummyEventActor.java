/**
 * 
 */
package com.isencia.passerelle.process.actor.trial;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * Format in events text area is like : <br/>
 * timestamp1|name1|value1 <br/>
 * timestamp2|name2|value2 <br/>
 * <br/>
 * timestamp3|name3|value3 <br/>
 * timestamp4|name4|value4 <br/>
 * ... <br/>
 * with timestamps in the format 'yyyy/MM/dd HH:mm:ss'
 * <p>
 * Each blank line represents a break in the event stream. I.e. the actor will send the events before the break right after each other, and then wait a bit (configurable interval)
 * before sending the next ones.
 * 
 * </p>
 * 
 * @author delerw
 * 
 */
public class DummyEventActor extends Actor {

	private static final long serialVersionUID = 1L;

	public Port trigger; // NOSONAR
	public Port output; // NOSONAR

	private static final Logger LOGGER = LoggerFactory.getLogger(DummyEventActor.class);

	private StringParameter eventsParameter;
	private Parameter batchIntervalParameter;

	private List<List<ResultItem>> eventBatches = new ArrayList<List<ResultItem>>();

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public DummyEventActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		batchIntervalParameter = new Parameter(this, "Batch interval (s)", new IntToken(5));
		eventsParameter = new StringParameter(this, "Events");
		new TextStyle(eventsParameter, "paramsTextArea");

		trigger = PortFactory.getInstance().createInputControlPort(this, "trigger");
		output = PortFactory.getInstance().createOutputPort(this);

	}
	

	@Override
	protected void doInitialize() throws InitializationException {
		super.doInitialize();
		
		try {
			eventBatches.clear();

			String eventDefs = ((StringToken) eventsParameter.getToken()).stringValue();
			BufferedReader reader = new BufferedReader(new StringReader(eventDefs));
			String eventDef = null;
			List<ResultItem> currentBatch = new ArrayList<ResultItem>();
			eventBatches.add(currentBatch);
		
			ProcessFactory factory = ProcessFactoryTracker.getService();
			while ((eventDef = reader.readLine()) != null) {
				if (eventDef.trim().length() == 0) {
					if (!currentBatch.isEmpty()) {
						currentBatch = new ArrayList<ResultItem>();
						eventBatches.add(currentBatch);
					}
				} else {
					String[] eventFields = eventDef.split("\\|");
					if (eventFields.length == 3) {
						try {
							ResultItem e = createEvent(factory,eventFields[0], eventFields[1], eventFields[2]);
							currentBatch.add(e);
						} catch (Exception e) {
							LOGGER.error("Invalid event definition: " + eventDef, e);
							ExecutionTracerService.trace(this, "Invalid event definition: " + eventDef);
						}
					} else {
						LOGGER.error("Invalid event definition: wrong nr of fields (should be 3) : " + eventDef);
						ExecutionTracerService.trace(this, "Invalid event definition: wrong nr of fields (should be 3) : " + eventDef);
					}
				}
			}
		} catch (Exception e) {
			throw new InitializationException("Error initializing event batches", this, e);
		}
	}

	private ResultItem createEvent(ProcessFactory factory, String date, String name, String value) throws Exception {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date d = fmt.parse(date);
		return factory.createResultItem(null, name, value, (String) null, d);
	}

	@Override
	protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
		try {
			if (!eventBatches.isEmpty()) {
				boolean isFirstBatch = (request.getIterationCount() == 0);
				List<ResultItem> batch = eventBatches.remove(0);
				if (!isFirstBatch) {
					long interval = ((IntToken) batchIntervalParameter.getToken()).intValue() * 1000L; // NOSONAR
					Thread.sleep(interval);
				}
				sendEventBatch(batch, response);
			} else if (this.areAllInputsFinished()) {
				requestFinish();
			}
		} catch (Exception e) {
			throw new ProcessingException("Error generating dummy events from " + getFullName(), null, e);
		}
	}

	private void sendEventBatch(List<ResultItem> batch, ProcessResponse response) throws MessageException {
		for (ResultItem event : batch) {
			ManagedMessage message = createMessage(event, ManagedMessage.objectContentType);
			response.addOutputMessage(output, message);
		}
	}

}
