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
import java.net.URI;
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

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageInputContext;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeNames;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.event.AbstractResultItemEventImpl;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
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
  public StringParameter resultTypeParam; // NOSONAR

  public EventsToTaskCollector(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
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
  public void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response) throws ProcessingException {
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

    ManagedMessage message = request.getMessage(input);
    if (message != null) {
      Task task = null;
      try {
        // time to create a task etc with the events as results
        String requestId = Long.toString(processManager.getRequest().getId());
        String referenceId = Long.toString(processManager.getRequest().getCase().getId());

        Map<String, String> taskAttributes = new HashMap<String, String>();
        taskAttributes.put(AttributeNames.CREATOR_ATTRIBUTE, getFullName());
        taskAttributes.put(AttributeNames.REF_ID, referenceId);
        taskAttributes.put(AttributeNames.REQUEST_ID, requestId);

        String scopeGroup = message.getSingleHeader(ProcessRequest.HEADER_CTXT_SCOPE_GRP);
        String scope = message.getSingleHeader(ProcessRequest.HEADER_CTXT_SCOPE);
        Context processContext = processManager.getScopedProcessContext(scopeGroup, scope);
        task = createTask(processManager, processContext, taskAttributes, new HashMap<String, Serializable>());
        if (!events.isEmpty()) {
          ProcessFactory entityFactory = processManager.getFactory();
          ResultBlock rb = entityFactory.createResultBlock(task, resultTypeParam.stringValue());
          for (Event event : events) {
            if (event instanceof AbstractResultItemEventImpl<?>) {
              AbstractResultItemEventImpl<?> resultItemEvent = (AbstractResultItemEventImpl<?>) event;
              String value = resultItemEvent.getValueAsString();
              entityFactory.createResultItem(rb, event.getTopic(), value, null, event.getCreationTS());
              
              Set<Attribute> attributes = resultItemEvent.getAttributes();
              for (Attribute attribute : attributes) {
                entityFactory.createResultItem(rb, attribute.getName(), attribute.getValueAsString(), null, event.getCreationTS());
              }
            } else {
              entityFactory.createResultItem(rb, event.getTopic(), null, null, event.getCreationTS());
            }
          }
          
          ProcessPersister persister = processManager.getPersister();
          boolean shouldClose = persister.open(true);
          persister.persistResultBlocks(rb);
          if (shouldClose) {
            persister.close();
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
    } else {
      // should not happen, but one never knows, e.g. when a requestFinish msg arrived or so...
      getLogger().warn("Actor " + this.getFullName() + " received empty message in process()");
      processFinished(processManager, request, response);
    }
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
  protected Task createTask(ProcessManager processManager, Context processContext, Map<String, String> taskAttributes,
      Map<String, Serializable> taskContextEntries) throws Exception {
    String initiator = new URI("actor", null, "/" + FlowUtils.getOriginalFullName(this).substring(1), null, null).toString();
    String taskType = taskTypeParam.stringValue();
    Task task = processManager.getFactory().createTask(null, processContext, initiator, taskType);
    for (Entry<String, String> attr : taskAttributes.entrySet()) {
      processManager.getFactory().createAttribute(task, attr.getKey(), attr.getValue());
    }
    for (String key : taskContextEntries.keySet()) {
      Serializable value = taskContextEntries.get(key);
      task.getProcessingContext().putEntry(key, value);
    }
    ProcessPersister persister = processManager.getPersister();
    boolean shouldClose = persister.open(true);
    persister.persistTask(task);
    if (shouldClose) {
      persister.close();
    }
    return task;
  }
}
