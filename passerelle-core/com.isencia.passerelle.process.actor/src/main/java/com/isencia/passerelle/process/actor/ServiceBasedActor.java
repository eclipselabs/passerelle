package com.isencia.passerelle.process.actor;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.AttributeNames;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ServiceTask;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.util.ProcessModelUtils;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.RequestProcessingBroker;
import com.isencia.passerelle.process.service.RequestProcessingBrokerTracker;

/**
 * This is a generic base class for service based actors.
 * <p>
 * The goal for such actors is to delegate the actual task processing logic to a separate services layer.
 * </p>
 * <p>
 * ServiceBasedActors and the underlying services should both implement a timeout handling logic. Services are typically
 * configured via preferences or another configuration source, including their default timeout period. Via the
 * {@link TaskExecutionBroker} and {@link TaskExecutionService} interfaces, actors can enforce their preferred timeout
 * period on the service.
 * </p>
 * <p>
 * Basic service implementations (e.g. blocking ones) may not have own timeout handling. But the TaskExecutionBroker
 * MUST have it, using the timeout settings given by the actor.
 * </p>
 * <p>
 * So a ServiceBasedActor should put its timeout definition for two reasons :
 * <ul>
 * <li>to guarantee timeout handling (in the broker) independent of the actual service implementation</li>
 * <li>to define specific timeouts on the "control layer", i.e. per actor instance, as needed for the specific process
 * flow</li>
 * </ul>
 * The second reason is relevant e.g. for different actors using a same underlying service but with different
 * requirements for end-2-end response times in their respective process flows.
 * </p>
 * <p>
 * This actor can be used as is, by configuring it with the required task type and attribute mapping. Alternatively,
 * specialised subclasses can be created to prepare tasks in specific ways.
 * </p>
 * 
 * @author erwin
 * 
 */
public class ServiceBasedActor extends TaskBasedActor {
  private static final long serialVersionUID = -189559117719512370L;
  public final static Long DEFAULT_TIMEOUT = 5L;
  public final static TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  /**
   * Used to specify the timeout value for the execution of a new {@link Task} by the underlying
   * {@link TaskExecutionService}. Typically, values <= 0 are interpreted as : no timeout defined, although it is not
   * considered good practice to skip timeout handling!
   * <p>
   * Default value is set
   * </p>
   */
  public Parameter timeOutParameter;
  /**
   * Used to specify the {@link TimeUnit} for the timeout handling.
   */
  public StringParameter timeUnitParameter;

  /**
   * Used to specify whether the request info as sent to the backing service, and the response received from the
   * service, should be stored in their raw format as tracing info.
   */
  public Parameter traceRequestResponseParameter;

  /**
   * Used to configure the attributes that must be added to each new Task.
   * <p>
   * Each new line in the String value of this parameter represents one attribute. Following syntaxes are possible :
   * <ul>
   * <li>attrName : the attrName is used to lookup an item in the parent process context and, iff found, a task
   * attribute is created with the value found and the given attrName</li>
   * <li>attrName=lookupItemName : the lookupItemName is used to lookup an item in the parent process context and, iff
   * found, a task attribute is created with the value found and the given attrName</li>
   * </ul>
   * </p>
   */
  public StringParameter attributeMappingParameter;

  public ServiceBasedActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    attributeMappingParameter = new StringParameter(this, AttributeNames.ATTR_MAPPING);
    new TextStyle(attributeMappingParameter, "textarea");
    registerExpertParameter(attributeMappingParameter);

    registerExpertParameter(traceRequestResponseParameter = new Parameter(this, "Trace requests and responses", BooleanToken.FALSE));
    new CheckBoxStyle(traceRequestResponseParameter, "chkbx");

    timeOutParameter = new Parameter(this, AttributeNames.TIMEOUT_TIME, new LongToken(getDefaultTimeOutValue()));
    timeOutParameter.setTypeEquals(BaseType.LONG);

    timeUnitParameter = new StringParameter(this, AttributeNames.TIME_UNIT);
    TimeUnit[] timeUnits = TimeUnit.values();
    timeUnitParameter.setExpression(getDefaultTimeOutUnit().name());
    for (TimeUnit timeUnit : timeUnits) {
      timeUnitParameter.addChoice(timeUnit.name());
    }
  }

  @Override
  protected ServiceTask createTask(ProcessManager processManager, Context processContext, Map<String, String> taskAttributes, Map<String, Serializable> taskContextEntries)
      throws Exception {
    return (ServiceTask) super.createTask(processManager, processContext, taskAttributes, taskContextEntries);
  }

  /**
   * Tasks for ServiceBasedActors should be ServiceTask implementations!
   */
  @Override
  protected Class<? extends ServiceTask> getTaskClass(Request parentRequest) {
    return null;
  }

  /**
   * 
   * @return the {@link RequestProcessingBroker} to which this actor will delegate its {@link Task} processing.
   */
  protected RequestProcessingBroker getProcessingBroker() {
    return RequestProcessingBrokerTracker.getService();
  }

  @Override
  protected void addActorSpecificTaskAttributes(Context processContext, Map<String, String> taskAttributes) throws ProcessingException {
    super.addActorSpecificTaskAttributes(processContext, taskAttributes);
    try {
      Map<String, String> attrMappings = getAttributeMappings();
      for (Entry<String, String> attrEntry : attrMappings.entrySet()) {
        ProcessModelUtils.storeContextItemValueInMap(taskAttributes, processContext, attrEntry.getKey(), attrEntry.getValue(), (String) null);
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Unable to obtain task attributes", this, e);
    }
  }

  @Override
  protected void process(Task task, ProcessManager processManager, ProcessResponse processResponse) throws ProcessingException {
    if (!(task instanceof ServiceTask)) {
      throw new ProcessingException(ErrorCode.TASK_INIT_ERROR, "Invalid task type." + task + " Should be a ServiceTask implementation!", this, null);
    }
    try {
      Long timeOutValue = getTimeOutValue();
      TimeUnit timeUnit = getTimeOutUnit();
      ((ServiceTask) task).setTraceRequestResponse(isTraceRequestResponse());
      getProcessingBroker().process(task, timeOutValue, timeUnit);
    } catch (ProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Error processing task " + task.getId(), this, null, e);
    }
  }

  protected TimeUnit getDefaultTimeOutUnit() {
    return DEFAULT_TIMEOUT_UNIT;
  }

  private boolean isTraceRequestResponse() throws IllegalActionException {
    boolean trace = false;
    if (traceRequestResponseParameter.getToken() != null) {
      trace = ((BooleanToken) traceRequestResponseParameter.getToken()).booleanValue();
    }
    return trace;
  }

  /**
   * 
   * @return the configured {@link TimeUnit}
   * @throws IllegalActionException
   *           when the timeUnitParameter can not be read
   * @throws IllegalArgumentException
   *           when the timeUnitParameter contains an illegal value
   */
  protected TimeUnit getTimeOutUnit() throws IllegalActionException, IllegalArgumentException {
    return TimeUnit.valueOf(timeUnitParameter.stringValue());
  }

  protected Long getDefaultTimeOutValue() {
    return DEFAULT_TIMEOUT;
  }

  protected Long getTimeOutValue() throws IllegalActionException {
    Long timeoutValue = null;
    if (timeOutParameter.getToken() != null) {
      timeoutValue = ((LongToken) timeOutParameter.getToken()).longValue();
    }
    return timeoutValue;
  }

  /**
   * 
   * @return a map with entries (attrName, lookupItemName), i.e. defining the task attributes with their attrName as
   *         key, and as entry value : the name of the item in the parent process context, where to look for the value
   *         that must be assigned to the task attribute.
   * @throws Exception
   *           i.c.o. a failure reading the definition of the attribute mapping
   */
  private Map<String, String> getAttributeMappings() throws Exception {
    String mappingDefs = ((StringToken) attributeMappingParameter.getToken()).stringValue();
    Map<String, String> attrMapping = new HashMap<String, String>();
    BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
    String mappingDef = null;
    while ((mappingDef = reader.readLine()) != null) {
      String[] mappingParts = mappingDef.split("=");
      if (mappingParts.length == 2) {
        String attrName = mappingParts[0];
        String lookupItemName = mappingParts[1];
        attrMapping.put(attrName, lookupItemName);
      } else if (mappingParts.length == 1) {
        String attrName = mappingParts[0];
        attrMapping.put(attrName, attrName);
      }
    }
    return attrMapping;
  }
}
