/**
 * 
 */
package com.isencia.passerelle.process.actor.trial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.actor.event.AbstractEventsGenerator;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;
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
 * Each blank line represents a break in the event stream. I.e. the actor will send the events before the break right after each other, 
 * and the next ones in a next iteration.
 * </p>
 * 
 * @author delerw
 */
public class MockEventGenerator extends AbstractEventsGenerator {
  private static final long serialVersionUID = -8625027541186615074L;

  private final static Logger LOGGER = LoggerFactory.getLogger(MockEventGenerator.class);
  private StringParameter eventsParameter;

  private BufferedReader evtStringReader;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MockEventGenerator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    eventsParameter = new StringParameter(this, "Events");
    new TextStyle(eventsParameter, "paramsTextArea");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    try {
      String eventDefs = ((StringToken) eventsParameter.getToken()).stringValue();
      evtStringReader = new BufferedReader(new StringReader(eventDefs));
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error initializing event batches", this, e);
    }
  }
  
  @Override
  protected void process(Task task, ProcessManager processManager, ProcessResponse processResponse) throws ProcessingException {
    String eventDef = null;
    boolean noEvents = true;
    try {
      while ((eventDef = evtStringReader.readLine()) != null) {
        if (eventDef.trim().length() == 0) {
          break;
        } else {
          String[] eventFields = eventDef.split("\\|");
          if (eventFields.length == 3) {
            try {
              SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
              Date d = fmt.parse(eventFields[0]);
              createEvent(d, eventFields[1], eventFields[2]);
            } catch (Exception e) {
              getLogger().error("Invalid event definition: " + eventDef, e);
              ExecutionTracerService.trace(this, "Invalid event definition: " + eventDef);
            }
          } else {
            getLogger().error("Invalid event definition: wrong nr of fields (should be 3) : " + eventDef);
            ExecutionTracerService.trace(this, "Invalid event definition: wrong nr of fields (should be 3) : " + eventDef);
          }
        }
      }
    } catch (IOException e) {
      throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error generating mock events", this, e);
    }
    if (noEvents && eventDef == null && this.areAllInputsFinished()) {
      requestFinish();
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    if (evtStringReader != null) {
      try {
        evtStringReader.close();
      } catch (IOException e) {
        // don't care; at least we tried...
      }
    }
    super.doWrapUp();
  }

}
