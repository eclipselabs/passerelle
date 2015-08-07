package com.isencia.passerelle.runtime.process.impl.event;

import com.isencia.passerelle.runtime.StatusEvent;
import com.isencia.passerelle.runtime.process.ProcessStatus;

public class StatusProcessEvent extends ProcessEvent implements StatusEvent {

  private static final long serialVersionUID = 1L;

  private ProcessStatus status;
  private Throwable throwable;

  public StatusProcessEvent(String processContextId, ProcessStatus status, Throwable throwable) {
    super(processContextId, Kind.UNSPECIFIED, Detail.UNSPECIFIED);
    this.status = status;
    this.throwable = throwable;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public Throwable getThrowable() {
    return throwable;
  }

}
