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


public class ErrorCategory extends com.isencia.passerelle.core.ErrorCategory {
  private static final long serialVersionUID = 1754112489992245910L;
  
  public final static ErrorCategory JMX_ROOT = new ErrorCategory("JMX_ROOT", null, "JMX");
  public final static ErrorCategory JMX_TECHNICAL = new ErrorCategory("JMX_TECHNICAL", JMX_ROOT, "TECH");
  public final static ErrorCategory JMX_FUNCTIONAL = new ErrorCategory("JMX_FUNCTIONAL", JMX_ROOT, "FUNC");

  public ErrorCategory(String name, com.isencia.passerelle.core.ErrorCategory parent, String prefix) {
    super(name, parent, prefix);
  }

}
