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
package com.isencia.passerelle.runtime.ws.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import com.isencia.passerelle.core.ErrorCode.Severity;

/**
 * @author erwin
 */
@XmlRootElement(name = "ErrorInfo")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorInfo {

  private Severity severity;
  private String code;
  private String description;
  private String extraInfo;

  protected ErrorInfo() {
  }

  public ErrorInfo(Severity severity, String code, String description, String extraInfo) {
    super();
    this.severity = severity;
    this.code = code;
    setExtraInfo(extraInfo);
    setDescription(description);
  }

  public String getCode() {
    return code;
  }

  public String getExtraInfo() {
    return extraInfo;
  }

  protected void setExtraInfo(String extraInfo) {
    this.extraInfo = extraInfo;
  }

  public String getDescription() {
    return description;
  }

  protected void setDescription(String descr) {
    this.description = descr;
  }

  public Severity getSeverity() {
    return severity;
  }

  @Override
  public String toString() {
    return "ErrorInfo [severity=" + severity + ", code=" + code + ", description=" + description + ", extraInfo=" + extraInfo + "]";
  }
}
