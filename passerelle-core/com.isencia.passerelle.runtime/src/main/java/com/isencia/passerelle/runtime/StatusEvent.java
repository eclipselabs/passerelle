package com.isencia.passerelle.runtime;

import com.isencia.passerelle.runtime.process.ProcessStatus;

public interface StatusEvent extends Event {
  ProcessStatus getStatus();

  Throwable getThrowable();
}
