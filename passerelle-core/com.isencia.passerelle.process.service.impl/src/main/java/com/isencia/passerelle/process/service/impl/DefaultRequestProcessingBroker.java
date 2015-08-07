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
package com.isencia.passerelle.process.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.RequestProcessingBroker;
import com.isencia.passerelle.process.service.RequestProcessingBrokerTracker;
import com.isencia.passerelle.process.service.RequestProcessingService;

/**
 * A simple default implementation in case no custom implementations get registered as service impl.
 * 
 * @author erwin
 */
public class DefaultRequestProcessingBroker implements RequestProcessingBroker<Task> {
  private final static Logger LOGGER = LoggerFactory.getLogger(DefaultRequestProcessingBroker.class);

  // a collection of all registered services, ordered by version in a set per name
  private Map<String, SortedSet<ServiceEntry>> services = new ConcurrentHashMap<String, SortedSet<ServiceEntry>>();

  private static ScheduledExecutorService delayTimer = Executors.newScheduledThreadPool(50);

  @Override
  public Future<Task> process(Task task, Long timeout, TimeUnit unit) throws ProcessingException {
    // Get timeout handling working before accessing the services
    // to make sure that bad/blocking service implementations don't interfere with it.
    registerTimeOutHandler(task, timeout, unit);

    Future<Task> futResult = null;
    for(SortedSet<ServiceEntry> svcSet : services.values()) {
      final ServiceEntry svcEntry = svcSet.last();
      RequestProcessingService<Task> service = svcEntry.service;
      if (service.canProcess(task)) {
        futResult = service.process(task, timeout, unit);
        if (futResult != null) {
          LOGGER.debug("Task {} will be processed by service {}", task.getId(), service.getName());
          break;
        }
      }
    }
    if (futResult != null) {
      return futResult;
    } else {
      throw new ProcessingException(ErrorCode.TASK_UNHANDLED, "No service found for " + task, null, null);
    }
  }
  
  private void registerTimeOutHandler(final Task task, Long timeout, TimeUnit unit) {
    if (timeout == null || unit == null || (timeout <= 0)) {
      return;
    }
    delayTimer.schedule(new TimeoutHandler(task.getProcessingContext().getProcessId(), task.getId()), timeout, unit);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Task {} timeout set to {} {}", new Object[] { task.getId(), timeout, unit });
    }
  }

  /**
   * remark that the services are registered typically via OSGi DS, and the register/remove methods are not expected to
   * be invoked from code.
   */
  @Override
  public synchronized boolean registerService(RequestProcessingService<Task> service) {
    return internalRegisterService(service, Version.emptyVersion);
  }

  public void registerServiceReference(ServiceReference<RequestProcessingService<Task>> svcRef) {
    RequestProcessingService<Task> service = svcRef.getBundle().getBundleContext().getService(svcRef);
    Version svcVersion = svcRef.getBundle().getVersion();
    internalRegisterService(service, svcVersion);
  }

  private boolean internalRegisterService(RequestProcessingService<Task> service, Version svcVersion) {
    boolean result = false;
    SortedSet<ServiceEntry> svcSet = services.get(service.getName());
    if (svcSet == null) {
      svcSet = new ConcurrentSkipListSet<DefaultRequestProcessingBroker.ServiceEntry>();
      services.put(service.getName(), svcSet);
    }
    if (svcSet.add(new ServiceEntry(service, svcVersion))) {
      result = true;
      LOGGER.debug("Registered service {} with version {}", service.getName(), svcVersion);
    } else {
      LOGGER.debug("Ignored duplicate service {} with version {}", service.getName(), svcVersion);
    }
    return result;
  }

  /**
   * remark that the services are registered typically via OSGi DS, and the register/remove methods are not expected to
   * be invoked from code.
   */
  @Override
  public boolean removeService(RequestProcessingService<Task> service) {
    return internalRemoveService(service, Version.emptyVersion);
  }

  public boolean removeServiceReference(ServiceReference<RequestProcessingService<Task>> svcRef) {
    RequestProcessingService<Task> svc = svcRef.getBundle().getBundleContext().getService(svcRef);
    Version svcVersion = svcRef.getBundle().getVersion();
    return internalRemoveService(svc, svcVersion);
  }

  private boolean internalRemoveService(RequestProcessingService<Task> service, Version svcVersion) {
    boolean result = false;
    Set<ServiceEntry> svcSet = services.get(service.getName());
    if (svcSet != null) {
      if (svcSet.remove(new ServiceEntry(service, svcVersion))) {
        result = true;
        LOGGER.debug("Removed service {} with version {}", service.getName(), svcVersion);
      } else {
        LOGGER.debug("Did not remove unknown service {} with version {}", service.getName(), svcVersion);
      }
    }
    return result;
  }

  @Override
  public void clearServices() {
    services.clear();
  }

  public void init() {
    RequestProcessingBrokerTracker.setService(this);
  }

  public void destroy() {
    RequestProcessingBrokerTracker.setService(null);
    clearServices();
    try {
      delayTimer.shutdownNow();
    } catch (Exception e) { // NOSONAR
      // ignore this one
    }
  }

  public static final class TimeoutHandler implements Callable<Void> {
    private final String processID;
    private final Long taskID;

    public TimeoutHandler(String processID, Long taskID) {
      this.processID = processID;
      this.taskID = taskID;
    }

    public Void call() {
      ProcessManager procMgr = ProcessManagerServiceTracker.getService().getProcessManager(processID);
      if (procMgr != null) {
        Task task = procMgr.getTask(taskID);
        if (task != null && !task.getProcessingContext().isFinished()) {
          procMgr.notifyTimeOut(task);
        }
      }
      return null;
    }
  }

  public static final class ServiceEntry implements Comparable<ServiceEntry> {

    RequestProcessingService<Task> service;
    Version version;

    public ServiceEntry(RequestProcessingService<Task> service, Version version) {
      this.service = service;
      this.version = version;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((service == null) ? 0 : service.hashCode());
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ServiceEntry other = (ServiceEntry) obj;
      if (service == null) {
        if (other.service != null)
          return false;
      } else if (!service.getName().equals(other.service.getName()))
        return false;
      if (version == null) {
        if (other.version != null)
          return false;
      } else if (!version.equals(other.version))
        return false;
      return true;
    }

    @Override
    public int compareTo(ServiceEntry o) {
      int res = service.getName().compareTo(o.service.getName());
      if(res==0) {
        res = version.compareTo(o.version);
      }
      return res;
    }

  }
}
