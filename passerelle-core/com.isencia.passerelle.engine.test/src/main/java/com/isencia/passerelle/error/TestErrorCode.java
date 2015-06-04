/**
 * 
 */
package com.isencia.passerelle.error;

import com.isencia.passerelle.core.ErrorCategory;
import com.isencia.passerelle.core.ErrorCode;

/**
 * @author delerw
 *
 */
@SuppressWarnings("serial")
public class TestErrorCode extends ErrorCode {

  protected TestErrorCode(String name, String code, ErrorCategory category, Severity severity, String description) {
    super(name, code, category, severity, description);
  }

  protected TestErrorCode(String name, String code, String topic, ErrorCategory category, Severity severity, String description) {
    super(name, code, topic, category, severity, description);
  }
  
  protected static void clear() {
    ErrorCode.clear(TestErrorCode.class);
  }
}
