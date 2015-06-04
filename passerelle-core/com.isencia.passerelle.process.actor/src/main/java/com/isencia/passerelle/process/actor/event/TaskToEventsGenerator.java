package com.isencia.passerelle.process.actor.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * This actor provides a bridge from a classic EDM Context/Task-based processing towards the event-based processing "world".
 * <p>
 * It can be used to map a Task in the ongoing processing context to a stream of Events :
 * <ul>
 * <li>The Task's ContextEvents are sent out as is</li>
 * <li>The Task's results (ResultItems) are transformed into value-holding Events and sent out as well</li>
 * </ul>
 * </p>
 * <p>
 * All sent Events have the creation timestamp of their "sources", and will be sent out in the right order. I.e. oldest Events first. But there are no
 * intermediate delays introduced between the sent events, they are all sent out in one rapid sequence.
 * </p>
 * <p>
 * If the received processing Context contains multiple Tasks with the given type, the most recently created one is used.
 * </p>
 * 
 * @author erwin
 */
public class TaskToEventsGenerator extends AbstractEventsGenerator {
  private static final long serialVersionUID = -2237583566697927936L;
  private final static Logger LOGGER = LoggerFactory.getLogger(TaskToEventsGenerator.class);

  public StringParameter selectedTaskTypeParameter;
  public Parameter sendContextEventsParameter;
  public Parameter sendResultItemEventsParameter;

  public TaskToEventsGenerator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    selectedTaskTypeParameter = new StringParameter(this, "Selected Task type");
    sendContextEventsParameter = new Parameter(this, "Send Context Events", BooleanToken.TRUE);
    new CheckBoxStyle(sendContextEventsParameter, "checkbox");
    sendResultItemEventsParameter = new Parameter(this, "Send ResultItem Events", BooleanToken.TRUE);
    new CheckBoxStyle(sendResultItemEventsParameter, "checkbox");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(Task task, ProcessManager processManager, ProcessResponse processResponse) throws ProcessingException {
    try {
      boolean sendContextEvents = ((BooleanToken) sendContextEventsParameter.getToken()).booleanValue();
      boolean sendResultItemEvents = ((BooleanToken) sendResultItemEventsParameter.getToken()).booleanValue();
      String selectedTaskType = selectedTaskTypeParameter.stringValue();
      if (selectedTaskType != null && selectedTaskType.trim().length() == 0) {
        selectedTaskType = null;
      }
      List<Task> tasks = task.getParentContext().getTasks();
      Task selectedTask = null;
      for (Task _task : tasks) {
        if ((_task != task) && (selectedTaskType == null || selectedTaskType.equalsIgnoreCase(_task.getType()))) {
          if ((selectedTask == null) || (_task.getProcessingContext().getCreationTS().after(selectedTask.getProcessingContext().getCreationTS()))) {
            selectedTask = _task;
          }
        }
      }
      if (selectedTask != null) {
        if (sendContextEvents) {
          addEvents(task.getProcessingContext().getEvents());
        }
        if (sendResultItemEvents) {
          for (ResultBlock block : selectedTask.getResultBlocks()) {
            for (ResultItem<?> item : block.getAllItems()) {
              String itemName = item.getName();
              int arrayIndexPos = itemName.lastIndexOf('[');
              if(arrayIndexPos>0) {
                itemName = itemName.substring(0, arrayIndexPos);
              }
              createEvent(item.getCreationTS(), itemName, item.getValueAsString());
            }
          }
        }
      }
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Error getting actor parameter value", this, e);
    }
  }

}
