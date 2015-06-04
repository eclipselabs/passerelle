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
package com.isencia.passerelle.runtime.jmx.server;

import java.util.Map;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.jmx.ProcessHandleBean;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;

public interface FlowProcessorMXBean {

  ProcessHandleBean start(String mode, String code, String processContextId) throws EntryNotFoundException;
  ProcessHandleBean start(String mode, FlowHandleBean flowHandle, String processContextId) throws EntryNotFoundException;
  ProcessHandleBean start(String mode, FlowHandleBean flowHandle, String processContextId, Map<String, String> parameterOverrides, String... breakpointNames) throws EntryNotFoundException;
  
  ProcessHandleBean getHandle(String processContextId) throws FlowNotExecutingException;
  
  ProcessHandleBean terminate(String processContextId) throws FlowNotExecutingException;
  ProcessHandleBean suspend(String processContextId) throws FlowNotExecutingException;
  ProcessHandleBean resume(String processContextId) throws FlowNotExecutingException;
}
