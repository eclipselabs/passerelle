package com.isencia.passerelle.runtime.jmx.server;

import java.util.Map;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.jmx.ProcessHandleBean;
import com.isencia.passerelle.runtime.jmx.server.activator.Activator;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class FlowProcessor implements FlowProcessorMXBean {

  @Override
  public ProcessHandleBean start(String mode, String code, String processContextId) throws EntryNotFoundException {
    FlowHandle handle = getFlowRepositoryService().getActiveFlow(code);
    StartMode _mode = StartMode.valueOf(mode);
    ProcessHandle localHandle = getFlowProcessingService().start(_mode, handle, processContextId, null, null);
    return ProcessHandleBean.buildProcessHandleBean(localHandle);
  }

  @Override
  public ProcessHandleBean start(String mode, FlowHandleBean flowHandle, String processContextId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessHandleBean start(String mode, FlowHandleBean flowHandle, String processContextId, Map<String, String> parameterOverrides,
      String... breakpointNames) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public ProcessHandleBean getHandle(String processContextId) throws FlowNotExecutingException {
    ProcessHandle localProcHandle = getFlowProcessingService().getHandle(processContextId);
    return ProcessHandleBean.buildProcessHandleBean(localProcHandle);
  };
  
  public ProcessHandleBean terminate(String processContextId) throws FlowNotExecutingException {
    ProcessHandle localProcHandle = getFlowProcessingService().getHandle(processContextId);
    localProcHandle = getFlowProcessingService().terminate(localProcHandle);
    return ProcessHandleBean.buildProcessHandleBean(localProcHandle);
  }

  public ProcessHandleBean suspend(String processContextId) throws FlowNotExecutingException {
    ProcessHandle localProcHandle = getFlowProcessingService().getHandle(processContextId);
    localProcHandle = getFlowProcessingService().suspend(localProcHandle);
    return ProcessHandleBean.buildProcessHandleBean(localProcHandle);
  }

  public ProcessHandleBean resume(String processContextId) throws FlowNotExecutingException {
    ProcessHandle localProcHandle = getFlowProcessingService().getHandle(processContextId);
    localProcHandle = getFlowProcessingService().resume(localProcHandle);
    return ProcessHandleBean.buildProcessHandleBean(localProcHandle);
  }

  private FlowProcessingService getFlowProcessingService() {
    return Activator.getInstance().getFlowProcessingSvc();
  }
  private FlowRepositoryService getFlowRepositoryService() {
    return Activator.getInstance().getFlowReposSvc();
  }
}
