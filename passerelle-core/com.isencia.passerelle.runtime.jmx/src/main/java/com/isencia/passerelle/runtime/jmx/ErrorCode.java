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
package com.isencia.passerelle.runtime.jmx;


public class ErrorCode extends com.isencia.passerelle.core.ErrorCode {
  private static final long serialVersionUID = 7180075316667266695L;
  
  public final static ErrorCode MISSING_PARAM = new ErrorCode("MISSING_PARAM", "1000", ErrorCategory.JMX_FUNCTIONAL, ErrorCode.Severity.WARNING,"Request parameter error");
  public final static ErrorCode MISSING_CONTENT = new ErrorCode("MISSING_CONTENT", "1001", ErrorCategory.JMX_FUNCTIONAL, ErrorCode.Severity.WARNING,"Request content error");
  public final static ErrorCode INVALID_PARAM = new ErrorCode("INVALID_PARAM", "1010", ErrorCategory.JMX_FUNCTIONAL, ErrorCode.Severity.WARNING,"Request parameter error");
  public final static ErrorCode INVALID_CONTENT = new ErrorCode("INVALID_CONTENT", "1011", ErrorCategory.JMX_FUNCTIONAL, ErrorCode.Severity.WARNING,"Request content error");

  public ErrorCode(String name, String code, ErrorCategory category, Severity severity, String description) {
    super(name, code, category, severity, description);
  }

  public ErrorCode(String name, String code, String topic, ErrorCategory category, Severity severity, String description) {
    super(name, code, topic, category, severity, description);
  }

}
