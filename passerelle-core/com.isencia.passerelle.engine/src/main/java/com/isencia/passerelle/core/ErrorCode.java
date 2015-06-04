/**
 * 
 */
package com.isencia.passerelle.core;

import java.util.SortedSet;
import java.util.regex.Pattern;
import com.isencia.sherpa.commons.Enumerated;

/**
 * ErrorCodes should be used for all Passerelle-related exceptions. They group info on :
 * <ul>
 * <li>a unique code to identify the type of error, which can be referred to in operational docs etc</li>
 * <li>a severity indicator</li>
 * <li>a topic to which the error is related, which can be used e.g. to publish error info on an event bus</li>
 * </ul>
 * 
 * @author erwin
 */
public class ErrorCode extends Enumerated<ErrorCode> {
  private static final long serialVersionUID = 1L;

  private static final char FORMATTEDCODE_SEPARATOR_CHAR = '-';
  private static final String TOSTRING_SEPARATOR = " - ";
  private static final char TOPIC_SEPARATOR = '/';
  private static final Pattern CODE_FORMAT_PATTERN = Pattern.compile("\\d{4}");

  public static final ErrorCode INFO = new ErrorCode("INFO", "9997", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.INFO, "");
  public static final ErrorCode ERROR = new ErrorCode("ERROR", "9998", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Undefined error");
  public static final ErrorCode FATAL = new ErrorCode("FATAL", "9999", ErrorCategory.TECHNICAL, ErrorCode.Severity.FATAL, "Undefined fatal error");
  public static final ErrorCode ERROR_PROCESSING_FAILURE = new ErrorCode("ERROR_PROCESSING_FAILURE", "9900", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error processing a previous error");
  public static final ErrorCode SYSTEM_CONFIGURATION_ERROR = new ErrorCode("SYSTEM_CONFIGURATION_ERROR", "9908", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "System configuration error");
  public static final ErrorCode SYSTEM_CONFIGURATION_FATAL = new ErrorCode("SYSTEM_CONFIGURATION_FATAL", "9909", ErrorCategory.TECHNICAL, ErrorCode.Severity.FATAL, "System configuration fatal error");

  public static final ErrorCode MSG_CONSTRUCTION_ERROR = new ErrorCode("MSG_CONSTRUCTION_ERROR", "0100", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Message construction error");
  public static final ErrorCode MSG_DELIVERY_FAILURE = new ErrorCode("MSG_DELIVERY_FAILURE", "0110", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Message delivery failure");
  public static final ErrorCode MSG_CONTENT_TYPE_ERROR = new ErrorCode("MSG_CONTENT_TYPE_ERROR", "0190", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.ERROR, "Message contents are not of correct type");

  public static final ErrorCode ACTOR_INITIALISATION_ERROR = new ErrorCode("ACTOR_INITIALISATION_ERROR", "0210", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.ERROR, "Actor initialisation failed");
  public static final ErrorCode ACTOR_EXECUTION_ERROR = new ErrorCode("ACTOR_EXECUTION_ERROR", "0220", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.ERROR, "Error in Actor execution");
  public static final ErrorCode ACTOR_EXECUTION_FATAL = new ErrorCode("ACTOR_EXECUTION_FATAL", "0229", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.FATAL, "Fatal error in Actor execution");

  public static final ErrorCode FLOW_LOADING_ERROR = new ErrorCode("FLOW_LOADING_ERROR", "0300", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error loading Flow");
  public static final ErrorCode FLOW_SAVING_ERROR_TECH = new ErrorCode("FLOW_SAVING_ERROR_TECH", "0301", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error saving Flow");
  public static final ErrorCode FLOW_SAVING_ERROR_FUNC = new ErrorCode("FLOW_SAVING_ERROR_FUNC", "0302", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.ERROR, "Error saving Flow");
  public static final ErrorCode FLOW_VALIDATION_WARNING = new ErrorCode("FLOW_VALIDATION_WARNING", "0310", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.WARNING, "Warning validating Flow");
  public static final ErrorCode FLOW_VALIDATION_ERROR = new ErrorCode("FLOW_VALIDATION_ERROR", "0311", ErrorCategory.FUNCTIONAL, ErrorCode.Severity.ERROR, "Error validating Flow");
  public static final ErrorCode FLOW_CONFIGURATION_ERROR = new ErrorCode("FLOW_CONFIGURATION_ERROR", "0320", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error configuring Flow");
  public static final ErrorCode FLOW_EXECUTION_ERROR = new ErrorCode("FLOW_EXECUTION_ERROR", "0330", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error executing Flow");
  public static final ErrorCode FLOW_EXECUTION_FATAL = new ErrorCode("FLOW_EXECUTION_FATAL", "0339", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Fatal error executing Flow");
  public static final ErrorCode FLOW_STATE_ERROR = new ErrorCode("FLOW_STATE_ERROR", "0390", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Internal state error in Flow");

  public static final ErrorCode RUNTIME_COMMUNICATION_ERROR = new ErrorCode("RUNTIME_COMMUNICATION_ERROR", "0900", ErrorCategory.TECHNICAL, ErrorCode.Severity.ERROR, "Error communicating with runtime");
  public static final ErrorCode RUNTIME_PERFORMANCE_INFO = new ErrorCode("RUNTIME_PERFORMANCE_INFO", "0920", ErrorCategory.TECHNICAL, ErrorCode.Severity.INFO, "Performance issue in runtime");

  public static enum Severity {
    INFO, WARNING, ERROR, FATAL;
  }

  private String code;
  private Integer codeAsInteger;
  private ErrorCategory category;
  private Severity severity;
  private String topic;
  private String description;
  
  private String formattedCode;
  private String formattedString;

  /**
   * ErrorCode constructor with automatically generated topic, based on the category prefix and the severity :
   * PREFIX/SEVERITY.
   * 
   * @param name
   * @param code
   * @param category
   * @param severity
   * @param description
   */
  protected ErrorCode(String name, String code, ErrorCategory category, Severity severity, String description) {
    super(name);
    assert CODE_FORMAT_PATTERN.matcher(code).matches();
    this.code = code;
    this.codeAsInteger = Integer.parseInt(code);
    this.category = category;
    this.severity = severity;
    this.description = description;
    this.topic = category.getPrefix() + TOPIC_SEPARATOR + severity + TOPIC_SEPARATOR + name;
  }

  /**
   * @param name
   * @param code
   * @param topic
   * @param category
   * @param severity
   * @param description
   */
  protected ErrorCode(String name, String code, String topic, ErrorCategory category, Severity severity, String description) {
    this(name, code, category, severity, description);
    this.topic = topic;
  }

  public String getCode() {
    return code;
  }
  
  public int getCodeAsInteger() {
    return codeAsInteger;
  }

  public ErrorCategory getCategory() {
    return category;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getTopic() {
    return topic;
  }

  public String getDescription() {
    return description;
  }
  
  public String getFormattedCode() {
    if(formattedCode==null) {
      formattedCode = "[" + getCategory().getPrefixChain() + FORMATTEDCODE_SEPARATOR_CHAR + getCode() + "]";
    }
    return formattedCode;
  }

  public static ErrorCode valueOf(String name) {
    return (Enumerated.valueOf(ErrorCode.class, name));
  }

  public static ErrorCode valueOf(int ordinal) {
    return (Enumerated.valueOf(ErrorCode.class, ordinal));
  }

  public static SortedSet<ErrorCode> values() {
    return (Enumerated.values(ErrorCode.class));
  }

  @Override
  public String toString() {
    if(formattedString==null) {
      formattedString = name() + TOSTRING_SEPARATOR + getSeverity() + TOSTRING_SEPARATOR + getFormattedCode() + TOSTRING_SEPARATOR + getDescription();
    }
    return formattedString;
  }
}
