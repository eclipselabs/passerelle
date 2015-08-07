package com.isencia.passerelle.process.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerService;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.runtime.ProcessHandle;

public class ProcessManagerServiceImpl implements ProcessManagerService {

  protected Collection<ContextProcessingCallback> overallCallbacks;
  protected Map<String, ProcessManager> processManagers = new ConcurrentHashMap<String, ProcessManager>(16, 0.9F, 1);

  protected ProcessFactory factory;
  protected ProcessPersister persister;

  public ProcessManager addProcessManager(ProcessManager processManager) {
    return (processManagers.put(processManager.getHandle().getProcessId(), processManager));
  }

  public void destroy() {
    ProcessManagerServiceTracker.setService(null);
  }

  @Override
  public ProcessFactory getFactory() {
    return factory;
  }

  @Override
  public ProcessPersister getPersister() {
    return persister;
  }

  @Override
  public ProcessManager getProcessManager(Request request) {
    if (request.getProcessingContext().getProcessId() != null) {
      return getProcessManager(request.getProcessingContext().getProcessId());
    } else {
      // if the process ID is somehow unknown, we'll try to find the processmanager for the same request ID
      ProcessManager result = null;
      for (ProcessManager processManager : processManagers.values()) {
        Long id = processManager.getRequest().getId();
        if ((id != null) && (id == request.getId())) {
          result = processManager;
          break;
        }
      }
      return result;
    }
  }

  @Override
  public ProcessManager getProcessManager(ProcessHandle handle) {
    ProcessManager processManager = processManagers.get(handle.getProcessId());
    if (processManager != null)
      return (processManager);

    return (null);
  }

  @Override
  public ProcessManager getProcessManager(String id) {
    return (processManagers.get(id));
  }

  @Override
  public Set<ProcessHandle> getProcessHandles(String userId, boolean master) {
    return getProcessHandles(userId, master, null);
  }

  @Override
  public Set<ProcessHandle> getProcessHandles(String userId, boolean master, String type) {
    Set<ProcessHandle> set = new HashSet<ProcessHandle>();

    for (ProcessManager processManager : processManagers.values()) {
      // skip processes that the user is not allowed to see
      String initiator = processManager.getRequest().getInitiator();
      String requestType = processManager.getRequest().getType();
      if (!master && (type != null && !type.equals(requestType)) &&(userId != null && initiator == null || userId == null && initiator != null || userId != null && initiator != null && !userId.equals(initiator)))
        continue;

      set.add(processManager.getHandle());
    }

    return set;
  }

  public void init() {
    ProcessManagerServiceTracker.setService(this);
  }

  public ProcessManager removeProcessManager(String id) {
    return (processManagers.remove(id));
  }

  public void setFactory(ProcessFactory factory) {
    this.factory = factory;
  }

  public void setPersister(ProcessPersister persister) {
    this.persister = persister;
  }

  @Override
  public boolean subscribeToAll(ContextProcessingCallback callback) {
    return overallCallbacks.add(callback);
  }

  @Override
  public boolean unsubscribe(ContextProcessingCallback callback) {
    return overallCallbacks.remove(callback);
  }

  @Override
  public Collection<ContextProcessingCallback> getOverallCallbacks() {
    return (overallCallbacks);
  }
}
