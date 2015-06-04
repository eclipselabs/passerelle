/**
 * 
 */
package com.isencia.passerelle.process.model.impl.factory;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
import com.isencia.passerelle.process.model.AttributeNames;
import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.RawResultBlock;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.process.model.impl.CaseImpl;
import com.isencia.passerelle.process.model.impl.ContextEventImpl;
import com.isencia.passerelle.process.model.impl.ErrorItemImpl;
import com.isencia.passerelle.process.model.impl.MainRequestImpl;
import com.isencia.passerelle.process.model.impl.RawResultBlockImpl;
import com.isencia.passerelle.process.model.impl.RequestAttributeImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockAttributeImpl;
import com.isencia.passerelle.process.model.impl.ResultBlockImpl;
import com.isencia.passerelle.process.model.impl.ResultItemAttributeImpl;
import com.isencia.passerelle.process.model.impl.StringResultItemImpl;
import com.isencia.passerelle.process.model.impl.TaskImpl;

/**
 * @author "puidir"
 * 
 */
public class ProcessFactoryImpl implements ProcessFactory {
  @Override
  public Attribute createAttribute(AttributeHolder holder, String name, String value) {
    if (holder == null) {
      throw new IllegalArgumentException("AttributeHolder can not be null");
    }
    if (holder instanceof Request) {
      return new RequestAttributeImpl((Request) holder, name, value);
    } else if (holder instanceof ResultBlock) {
      return new ResultBlockAttributeImpl((ResultBlock) holder, name, value);
    } else if (holder instanceof ResultItem) {
      return new ResultItemAttributeImpl((ResultItem<?>) holder, name, value);
    } else {
      throw new IllegalArgumentException("Unknown AttributeHolder type " + holder.getClass());
    }
  }

  @Override
  public Case createCase(String externalReference) {
    return new CaseImpl();
  }

  @Override
  public ContextErrorEvent createContextErrorEvent(Context context, ErrorItem errorItem) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, String description,
      Set<String> relatedDataTypes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause,
      Set<String> relatedDataTypes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContextEvent createContextEvent(Context context, String topic, String message) {
    return new ContextEventImpl(context, topic, message);
  }

  @Override
  public ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, String description, List<String> detailedDescriptions,
      Set<String> relatedDataTypes) {
    return new ErrorItemImpl(severity, category, code, shortDescription, description, detailedDescriptions, relatedDataTypes);
  }

  @Override
  public ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes) {
    return new ErrorItemImpl(severity, category, code, shortDescription, cause, relatedDataTypes);
  }

  @Override
  public RawResultBlock createRawResultBlock(Task task, String type) {
    return new RawResultBlockImpl(task, type);
  }

  @Override
  public Request createRequest(Case requestCase, String initiator, String category, String type, String correlationId) {
    return new MainRequestImpl(requestCase, initiator, type, correlationId, category);
  }

  @Override
  public Request createRequest(Case requestCase, String initiator, String executor, String category, String type, String correlationId) {
    return new MainRequestImpl(requestCase, initiator, executor, type, correlationId, category);
  }

  @Override
  public ResultBlock createResultBlock(Task task, String type) {
    return createResultBlock(task, type, new Date());
  }

  @Override
  public ResultBlock createResultBlock(Task task, String type, Date date) {
    ResultBlock rb = new ResultBlockImpl(task, type, date);
    try {
      String resultTag = (String) task.getProcessingContext().getEntryValue(AttributeNames.RESULT_TAG);
      if (resultTag != null) {
        createAttribute(rb, "tag", resultTag);
      }
    } catch (NullPointerException e) {
      // ignore, in unit tests there is no task context nor tags and then we end up with NPE here
    }
    return rb;
  }

  @Override
  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit) {
    return createResultItem(resultBlock, name, value, unit, null, null);
  }

  @Override
  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date) {
    return createResultItem(resultBlock, name, value, unit, null, date);
  }

  @Override
  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Integer level) {
    return createResultItem(resultBlock, name, value, unit, level, null);
  }

  public ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Integer level, Date date) {
    if (name == null) {
      return null;
    }
    return new StringResultItemImpl(resultBlock, name, value, unit, date, level);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Task> T createTask(Class<T> taskClass, Request request, String initiator, String type) throws Exception {
    if (taskClass == null) {
      return (T) (createTask(request, initiator, type));
    }
    return taskClass.getConstructor(Context.class, String.class, String.class).newInstance(request.getProcessingContext(), initiator, type);
  }

  @Override
  public Task createTask(Request request, String initiator, String type) {
    return new TaskImpl(request.getProcessingContext(), initiator, type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Task> T createTask(Class<T> taskClass, Context processContext, String initiator, String type) throws Exception {
    if (taskClass == null) {
      return (T) (createTask(processContext, initiator, type));
    }
    return taskClass.getConstructor(Context.class, String.class, String.class).newInstance(processContext, initiator, type);
  }

  @Override
  public Task createTask(Context processContext, String initiator, String type) {
    return new TaskImpl(processContext, initiator, type);
  }

  public void destroy() {
    ProcessFactoryTracker.setService(null);
  }

  public void init() {
    ProcessFactoryTracker.setService(this);
  }
}
