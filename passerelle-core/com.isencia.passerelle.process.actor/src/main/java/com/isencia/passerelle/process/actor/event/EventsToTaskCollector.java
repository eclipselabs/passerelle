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
package com.isencia.passerelle.process.actor.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.AttributeNames;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.event.AbstractResultItemEventImpl;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * This actor provides a bridge from the event-based monitoring "world" towards the classic Context/Task-based
 * processing.
 * <p>
 * It is not desirable to store detailed traces of each activity during the monitoring of potentially huge and
 * long-running event streams. Only when a potential issue is identified, it makes sense to store this fact as an alarm.
 * The treatment of the alarms is a good match to classic task-based processing and tracing. <br/>
 * This actor can be used to collect alarm (or other) events and to add them as Task results to a processing Context.
 * </p>
 * <p>
 * Currently this is implemented in a basic way, adding events as items on the default Context of the monitoring process
 * execution. In the future more options must be added, e.g. to start specific diagnosis Contexts per alarm etc.
 * </p>
 * <p>
 * REMARK : some basic elements are duplicated from TaskBasedActor, but this actor is not a TaskBasedActor in the
 * traditional sense!
 * </p>
 * 
 * @author erwin
 */
public class EventsToTaskCollector extends Actor {
  private static final long serialVersionUID = 2978872425811556859L;
  private final static Logger LOGGER = LoggerFactory.getLogger(EventsToTaskCollector.class);

  public Port input; // NOSONAR
  public Port output; // NOSONAR

  // by default the actor name is set as task/result type
  public StringParameter taskTypeParam; // NOSONAR
  public StringParameter resultTypeParam;

  private ProcessManager processManager = null;

  public EventsToTaskCollector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, PortMode.PUSH, null);
    output = PortFactory.getInstance().createOutputPort(this);
    taskTypeParam = new StringParameter(this, AttributeNames.TASK_TYPE);
    taskTypeParam.setExpression(name);
    resultTypeParam = new StringParameter(this, AttributeNames.RESULT_TYPE);
    resultTypeParam.setExpression(name);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    NamedObj flow = toplevel();
    try {
      processManager = null;
      StringParameter procMgrParameter = (StringParameter) flow.getAttribute(com.isencia.passerelle.process.actor.ProcessRequest.HEADER_PROCESS_ID, StringParameter.class);
      if (procMgrParameter != null) {
        processManager = ProcessManagerServiceTracker.getService().getProcessManager(procMgrParameter.stringValue());
      }
    } catch (Exception e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error obtaining ProcessManager", this, e);
    }
    if (processManager == null) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error obtaining ProcessManager", this, null);
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    Iterator<MessageInputContext> inputContexts = request.getAllInputContexts();
    Set<Event> events = new HashSet<Event>();
    while (inputContexts.hasNext()) {
      MessageInputContext inputContext = (MessageInputContext) inputContexts.next();
      Iterator<ManagedMessage> msgIterator = inputContext.getMsgIterator();
      while (msgIterator.hasNext()) {
        ManagedMessage msg = msgIterator.next();
        if (msg != null) {
          try {
            Object o = msg.getBodyContent();
            if (o instanceof Event) {
              events.add((Event) o);
            }
          } catch (Exception e) {
            getLogger().error("Error reading msg contents", e);
            // and then don't stop but try to work with the other received events after all
          }
        }
      }
    }

    Task task = null;
    try {
      // time to create a task etc with the events as results
      String requestId = Long.toString(processManager.getRequest().getId());
      String referenceId = Long.toString(processManager.getRequest().getCase().getId());

      Map<String, String> taskAttributes = new HashMap<String, String>();
      taskAttributes.put(AttributeNames.CREATOR_ATTRIBUTE, getFullName());
      taskAttributes.put(AttributeNames.REF_ID, referenceId);
      taskAttributes.put(AttributeNames.REQUEST_ID, requestId);
      task = createTask(processManager, processManager.getRequest(), taskAttributes, new HashMap<String, Serializable>());
      if (!events.isEmpty()) {
        ProcessFactory entityFactory = processManager.getFactory();
        ResultBlock rb = entityFactory.createResultBlock(task, resultTypeParam.stringValue());
        for (Event event : events) {
          String value = null;
          if (event instanceof AbstractResultItemEventImpl<?>) {
            value = ((AbstractResultItemEventImpl<?>) event).getValueAsString();
          }
          entityFactory.createResultItem(rb, event.getTopic(), value, null, event.getCreationTS());
        }
      }
      processManager.notifyFinished(task);
    } catch (Throwable t) {
      ExecutionTracerService.trace(this, t.getMessage());
      response.setException(new ProcessingException(ErrorCode.TASK_ERROR, "Error processing task", this, t));
      if (task != null) {
        processManager.notifyError(task, t);
      }
    }

    try {
      // send the process context with the additional task
      ManagedMessage msg = createMessage(processManager, ManagedMessage.objectContentType);
      response.addOutputMessage(output, msg);
    } catch (MessageException e) {
      response.setException(new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error sending output msg", this, e));
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    processManager = null;
    super.doWrapUp();
  }

  /**
   * Returns a new Task with the configured taskType.
   * 
   * @param processManager
   * @param parentRequest
   * @param taskAttributes
   * @param taskContextEntries
   * @return the new task
   * @throws Exception
   */
  protected Task createTask(ProcessManager processManager, Request parentRequest, Map<String, String> taskAttributes,
      Map<String, Serializable> taskContextEntries) throws Exception {
    String taskType = taskTypeParam.stringValue();
    Task task = processManager.getFactory().createTask(null, parentRequest, FlowUtils.getFullNameWithoutFlow(this), taskType);
    for (Entry<String, String> attr : taskAttributes.entrySet()) {
      processManager.getFactory().createAttribute(task, attr.getKey(), attr.getValue());
    }
    for (String key : taskContextEntries.keySet()) {
      Serializable value = taskContextEntries.get(key);
      task.getProcessingContext().putEntry(key, value);
    }
    processManager.getPersister().persistTask(task);
    return task;
  }
}
