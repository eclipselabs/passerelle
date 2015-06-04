package com.isencia.passerelle.process.service.impl;

import java.util.Date;
import java.util.UUID;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.ProcessStatus;

public class ProcessHandleImpl implements ProcessHandle {
  private static final long serialVersionUID = 1L;

  private FlowHandle flowHandle;
  private String processId;
  private ProcessStatus executionStatus;
  private String[] suspendedElements;
  private Date creationTS;

  public ProcessHandleImpl() {
    this(UUID.randomUUID().toString(), null);
  }

  public ProcessHandleImpl(String processId) {
    this(processId, null);
  }

  public ProcessHandleImpl(Flow flow) {
    this(UUID.randomUUID().toString(), flow);
  }

  public ProcessHandleImpl(String processId, Flow flow) {
    this.processId = processId;
    if (flow != null) {
      this.flowHandle = new FlowHandleImpl(flow);
    }
    this.creationTS = new Date();
  }

  @Override
  public ProcessStatus getExecutionStatus() {
    return executionStatus;
  }

  @Override
  public FlowHandle getFlowHandle() {
    return flowHandle;
  }

  @Override
  public String getProcessId() {
    return processId;
  }

  @Override
  public String[] getSuspendedElements() {
    return suspendedElements;
  }

  public Date getCreationTS() {
    return creationTS;
  }

  public void setExecutionStatus(ProcessStatus executionStatus) {
    this.executionStatus = executionStatus;
  }

  public void setFlowHandle(FlowHandle flowHandle) {
    this.flowHandle = flowHandle;
  }

  public void setSuspendedElements(String[] suspendedElements) {
    this.suspendedElements = suspendedElements;
  }
}
