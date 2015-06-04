/**
 * 
 */
package com.isencia.passerelle.process.actor.trial;

import java.io.BufferedReader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.actor.TaskBasedActor;
import com.isencia.passerelle.process.model.AttributeNames;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * @author delerw
 * 
 */
public class DummyResultActor extends TaskBasedActor {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DummyResultActor.class);

	public StringParameter resultTypeParam; // NOSONAR
	public StringParameter resultItemsParameter; // NOSONAR

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public DummyResultActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		resultTypeParam = new StringParameter(this, AttributeNames.RESULT_TYPE);
		resultTypeParam.setExpression(name);

		resultItemsParameter = new StringParameter(this, "Result items");
		new TextStyle(resultItemsParameter, "paramsTextArea");
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected void process(Task task, ProcessManager processManager, ProcessResponse processResponse) throws ProcessingException {
		try {
			ResultBlock rb = processManager.getFactory().createResultBlock(task, resultTypeParam.getExpression());

			String paramDefs = ((StringToken) resultItemsParameter.getToken()).stringValue();
			BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
			String paramDef = null;
			while ((paramDef = reader.readLine()) != null) {
				String[] paramKeyValue = paramDef.split("=");
				if (paramKeyValue.length == 2) {
					processManager.getFactory().createResultItem(rb, paramKeyValue[0], paramKeyValue[1], null);
				} else {
					ExecutionTracerService.trace(this, "Invalid mapping definition: " + paramDef);
				}
			}
		} catch (Exception e) {
			throw new ProcessingException("Error generating dummy results for " + resultTypeParam.getExpression(), task.getProcessingContext(), e);
		}

		processManager.notifyFinished(task);
	}
}
