package com.isencia.passerelle.process.common.exception;

import com.isencia.passerelle.core.ErrorCategory;


/**
 * @author puidir
 */
@SuppressWarnings("serial")
public class ErrorCode extends com.isencia.passerelle.core.ErrorCode {

  public static final ErrorCode SERVICE_REQUEST_PROCEED_ERROR = new ErrorCode("SERVICE_REQUEST_PROCEED_ERROR", "1008", ErrorCategory.FUNCTIONAL, Severity.ERROR,
      "Error proceeding service request.");

  // 3000 RANGE : errors related to request handling and engine internals
  public static final ErrorCode REQUEST_CONTENTS_ERROR = new ErrorCode("REQUEST_CONTENTS_ERROR", "3000", ErrorCategory.FUNCTIONAL, Severity.WARNING,"Request contents error");
  public static final ErrorCode REQUEST_INIT_ERROR = new ErrorCode("REQUEST_INIT_ERROR", "3001", ErrorCategory.FUNCTIONAL, Severity.WARNING,"Request initialisation error");

  public static final ErrorCode REQUEST_ERROR = new ErrorCode("REQUEST_ERROR", "3100", "request/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR,"Request processing error");
  public static final ErrorCode REQUEST_SLOW = new ErrorCode("REQUEST_SLOW", "3101", "request/SLOW", ErrorCategory.FUNCTIONAL, Severity.WARNING,"Request processing slow");
  public static final ErrorCode REQUEST_TIMEOUT = new ErrorCode("REQUEST_TIMEOUT", "3102", "request/TIME_OUT", ErrorCategory.FUNCTIONAL, Severity.WARNING,"Request processing timed out");

  // 3500... Request scheduling and life-cycle management
  public static final ErrorCode REQUEST_LIFECYCLE_ACCEPT_ERROR = new ErrorCode("REQUEST_LIFECYCLE_ACCEPT_ERROR", "3500", "request/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR, "Request lifecycle error : request acceptance error");
  public static final ErrorCode REQUEST_LIFECYCLE_REQUEST_REFUSED = new ErrorCode("REQUEST_LIFECYCLE_REQUEST_REFUSED", "3501", "request/ERROR", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Request lifecycle error : request refused");
  public static final ErrorCode REQUEST_LIFECYCLE_EVENT_LOG_ERROR = new ErrorCode("REQUEST_LIFECYCLE_EVENT_LOG_ERROR", "3502", "request/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR, "Request lifecycle error : event log error");
  public static final ErrorCode REQUEST_LIFECYCLE_FINISHED_NOTIF_ERROR = new ErrorCode("REQUEST_LIFECYCLE_FINISHED_NOTIF_ERROR", "3550", "request/ERROR", ErrorCategory.TECHNICAL, Severity.WARNING, "Request lifecycle error : request finished notification error");
  public static final ErrorCode REQUEST_LIFECYCLE_TIMEOUT_NOTIF_ERROR = new ErrorCode("REQUEST_LIFECYCLE_TIMEOUT_NOTIF_ERROR", "3551", "request/ERROR", ErrorCategory.TECHNICAL, Severity.WARNING, "Request lifecycle error : request timeout notification error");

  // 3700... Task scheduling/buffering etc
  // when the task involves a backend communication, these errors may also be reported
  // as specific errors for backend error/slow/timeout (5000 range)
  public static final ErrorCode SCHEDULER_ERROR = new ErrorCode("SCHEDULER_ERROR", "3700", "taskscheduler/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR, "Task Scheduler error");
  public static final ErrorCode SCHEDULER_BUFFER_LOAD = new ErrorCode("SCHEDULER_BUFFER_LOAD", "3710", "taskscheduler/buffer/LOAD", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task Scheduler buffer load");
  public static final ErrorCode SCHEDULER_POOL_LOAD = new ErrorCode("SCHEDULER_POOL_LOAD", "3720", "taskscheduler/pool/LOAD", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task Scheduler pool load");
  public static final ErrorCode SCHEDULER_WARNING = new ErrorCode("SCHEDULER_WARNING", "3730", "scheduler/WARNING", ErrorCategory.TECHNICAL, Severity.WARNING, "Task scheduler error");

  public static final ErrorCode ANALYSIS_TASK_ERROR = new ErrorCode("ANALYSIS_TASK_ERROR", "3800", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Task processing error");
  public static final ErrorCode ANALYSIS_TASK_SLOW = new ErrorCode("ANALYSIS_TASK_SLOW", "3801", "task/SLOW", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task processing slow");
  public static final ErrorCode ANALYSIS_TASK_TIMEOUT = new ErrorCode("ANALYSIS_TASK_TIMEOUT", "3802", "task/TIMEOUT", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task processing timed out");

  // 5000 RANGE : errors related to asynchronous service communications
  public static final ErrorCode TASK_INIT_ERROR = new ErrorCode("TASK_INIT_ERROR", "5000", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Task initialisation error");
  public static final ErrorCode TASK_ERROR = new ErrorCode("TASK_ERROR", "5000", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Task processing error");
  public static final ErrorCode TASK_SLOW = new ErrorCode("TASK_SLOW", "5001", "task/SLOW", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task processing slow");
  public static final ErrorCode TASK_TIMEOUT = new ErrorCode("TASK_TIMEOUT", "5002", "task/TIME_OUT", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Task processing timed out");
  public static final ErrorCode TASK_UNHANDLED = new ErrorCode("TASK_UNHANDLED", "5020", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "No service found to handle the task");

  public static final ErrorCode ASYNC_SERVICE_ERROR = new ErrorCode("ASYNC_SERVICE_ERROR", "5000", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Asynchronous service request processing error");
  public static final ErrorCode INVALID_PARAMETERS = new ErrorCode("INVALID_PARAMETERS", "5003", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.INFO, "Request contains invalid parameters");
  public static final ErrorCode INVALID_CONFIGURATION = new ErrorCode("INVALID_CONFIGURATION", "5005", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Invalid configuration");
  
  public static final ErrorCode BACKEND_ERROR = new ErrorCode("BACKEND_ERROR", "5000", "task/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Backend request processing error");
  public static final ErrorCode BACKEND_WARNING = new ErrorCode("BACKEND_WARNING", "5010", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Backend request returned a warning");

  // 5100 RANGE : errors related to task attributes and backend request parameters
  public static final ErrorCode INCOMPLETE_INPUT_ARGUMENTS = new ErrorCode("INCOMPLETE_INPUT_ARGUMENTS", "5107", ErrorCategory.FUNCTIONAL, Severity.WARNING, "Missing input parameters");

  // 8000 RANGE : errors related to OSGi
  public static final ErrorCode BUNDLE_START_FAILED = new ErrorCode("BUNDLE_START_FAILED", "8000", "system/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR, "Bundle start failed");
  public static final ErrorCode BUNDLE_STOP_FAILED = new ErrorCode("BUNDLE_STOP_FAILED", "8001", "system/WARNING", ErrorCategory.TECHNICAL, Severity.WARNING, "Bundle stop failed");

  // 9000 RANGE : dramatic technical errors
  public static final ErrorCode SYSTEM_ERROR = new ErrorCode("SYSTEM_ERROR", "9999", "system/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR, "Internal error");
  public static final ErrorCode CONFIG_ERROR = new ErrorCode("CONFIG_ERROR", "9000", "system/ERROR", ErrorCategory.FUNCTIONAL, Severity.ERROR, "Configuration error");
  
  public static final ErrorCode PERSISTENCE_ERROR = new ErrorCode("PERSISTENCE_ERROR", "9100", "persistence/ERROR", ErrorCategory.TECHNICAL, Severity.ERROR,"Persistence error");
  public static final ErrorCode PERSISTENCE_WARNING = new ErrorCode("PERSISTENCE_WARNING", "9101", "persistence/WARNING", ErrorCategory.TECHNICAL, Severity.WARNING,"Persistence warning");
  public static final ErrorCode PERSISTENCE_FATAL = new ErrorCode("PERSISTENCE_FATAL", "9102", "persistence/FATAL", ErrorCategory.TECHNICAL, Severity.FATAL,"Persistence fatal error");


  public ErrorCode(String name, String code, ErrorCategory category, Severity severity, String description) {
    super(name, code, category, severity, description);
  }

  public ErrorCode(String name, String code, String topic, ErrorCategory category, Severity severity, String description) {
    super(name, code, topic, category, severity, description);
  }

}
