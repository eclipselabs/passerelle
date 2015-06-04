/**
 * 
 */
package com.isencia.passerelle.process.model.factory;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.AttributeHolder;
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

/**
 * @author "puidir"
 * 
 */
public interface ProcessFactory {

  Attribute createAttribute(AttributeHolder holder, String name, String value);

  /**
   * Create a new Case.
   * 
   * @param externalReference
   *          Can be used to link the Case to external system, e.g. order identifiers, client numbers, etc
   */
  Case createCase(String externalReference);

  ContextErrorEvent createContextErrorEvent(Context context, ErrorItem errorItem);

  ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, String description,
      Set<String> relatedDataTypes);

  ContextErrorEvent createContextErrorEvent(Context context, Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause,
      Set<String> relatedDataTypes);

  ContextEvent createContextEvent(Context context, String topic, String message);

  ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, String description, List<String> detailedDescriptions,
      Set<String> relatedDataTypes);

  ErrorItem createErrorItem(Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes);

  RawResultBlock createRawResultBlock(Task task, String type);

  /**
   * Create a Request.
   * 
   * @param requestCase
   *          Requests are always linked to a Case
   * @param type
   * @param correlationId
   */
  Request createRequest(Case requestCase, String initiator, String category, String type, String correlationId);

  Request createRequest(Case requestCase, String initiator, String executor, String category, String type, String correlationId);

  ResultBlock createResultBlock(Task task, String type);

  ResultBlock createResultBlock(Task task, String type, Date date);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Date date);

  ResultItem<String> createResultItem(ResultBlock resultBlock, String name, String value, String unit, Integer level);

  <T extends Task> T createTask(Class<T> taskClass, Request request, String initiator, String type) throws Exception;

  Task createTask(Request request, String initiator, String type);

  // these are temporarily back to support frok/join scoped context handling
  <T extends Task> T createTask(Class<T> taskClass, Context context, String initiator, String type) throws Exception;

  Task createTask(Context context, String initiator, String type);
}
