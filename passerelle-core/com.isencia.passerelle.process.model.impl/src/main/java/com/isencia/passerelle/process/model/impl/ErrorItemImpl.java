/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.process.model.ErrorItem;

/**
 * @author "puidir"
 * 
 */
public class ErrorItemImpl implements ErrorItem, Serializable {
  private static final long serialVersionUID = -6470343890215671989L;

  private ErrorCategory category;
  private Severity severity;
  private String code;
  private Set<String> relatedDataTypes = new TreeSet<String>();
  private String shortDescription;
  private String description;
  private List<String> details = new ArrayList<String>();

  /**
   * 
   * @param severity
   * @param category
   * @param code
   * @param shortDescription
   * @param description
   * @param relatedDataTypes
   */
  public ErrorItemImpl(Severity severity, ErrorCategory category, String code, String shortDescription, String description, Set<String> relatedDataTypes) {
    super();
    this.severity = severity;
    this.category = category;
    this.code = code;
    if(relatedDataTypes!=null) {
      this.relatedDataTypes.addAll(relatedDataTypes);
    }
    setShortDescription(shortDescription);
    setDescription(description);
  }

  /**
   * Creates an error item based on a Throwable that was caught during the request processing.
   * <p>
   * The cause's stack trace is stored in the description of the error item.
   * </p>
   * 
   * @param severity
   * @param category
   * @param code
   * @param shortDescription
   * @param cause
   * @param relatedDataTypes
   */
  public ErrorItemImpl(Severity severity, ErrorCategory category, String code, String shortDescription, Throwable cause, Set<String> relatedDataTypes) {
    super();
    this.severity = severity;
    this.category = category;
    this.code = code;
    if(relatedDataTypes!=null) {
      this.relatedDataTypes.addAll(relatedDataTypes);
    }
    setShortDescription(shortDescription);
    setDescription(buildDescription(cause));
  }

  public ErrorItemImpl(Severity severity, ErrorCategory category, String code, String shortDescription, String description, List<String> details, Set<String> relatedDataTypes) {
    this(severity, category, code, shortDescription, description, relatedDataTypes);
    if(details!=null) {
      this.details.addAll(details);
    }
  }

  private String buildDescription(Throwable cause) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    cause.printStackTrace(pw);
    return sw.toString();
  }

  public ErrorCategory getCategory() {
    return category;
  }

  public String getCode() {
    return code;
  }
  
  public Set<String> getRelatedDataTypes() {
    return relatedDataTypes;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  protected void setShortDescription(String shortDescr) {
    this.shortDescription = shortDescr;
  }

  public String getDescription() {
    return description;
  }
  
  @Override
  public List<String> getDetails() {
    return details;
  }
  
  // remark : this rebuilds the result string, no caching to avoid memory consumption
  @Override
  public String getDescriptionWithDetails() {
    StringBuilder sb = new StringBuilder(200);
    sb.append("description = ");
    sb.append(description);
    for (String d : details) {
      sb.append("\n    detail=");
      sb.append(d);
    }
    sb.append("\n");
    return sb.toString();
  }

  protected void setDescription(String descr) {
    this.description = descr;
  }

  public Severity getSeverity() {
    return severity;
  }

  public int compareTo(ErrorItem o) {
    if (o == this)
      return 0;
    if (o == null)
      return 1;

    int result = this.severity.compareTo(o.getSeverity());
    if (result == 0) {
      if (code == null || o.getCode() == null || (this.code.compareTo(o.getCode()) == 0)) {
        if (description == null || o.getDescription() == null)
          result = 1;
        else
          result = this.description.compareTo(o.getDescription());
      } else {
        result = this.code.compareTo(o.getCode());
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((relatedDataTypes == null) ? 0 : relatedDataTypes.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((severity == null) ? 0 : severity.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ErrorItemImpl other = (ErrorItemImpl) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (relatedDataTypes == null) {
      if (other.relatedDataTypes != null)
        return false;
    } else if (!relatedDataTypes.equals(other.relatedDataTypes))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (severity == null) {
      if (other.severity != null)
        return false;
    } else if (!severity.equals(other.severity))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ErrorItemImpl [severity=");
    builder.append(severity);
    builder.append(", category=");
    builder.append(category);
    builder.append(", code=");
    builder.append(code);
    builder.append(", relatedDataTypes=");
    builder.append(relatedDataTypes);
    builder.append(", short description=");
    builder.append(shortDescription);
    builder.append(", description=");
    builder.append(description);
    builder.append(", details=");
    builder.append(details);
    builder.append("]");
    return builder.toString();
  }
}
