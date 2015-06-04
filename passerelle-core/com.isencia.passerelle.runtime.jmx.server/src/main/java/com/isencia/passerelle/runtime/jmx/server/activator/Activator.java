package com.isencia.passerelle.runtime.jmx.server.activator;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.isencia.passerelle.runtime.jmx.server.FlowProcessor;
import com.isencia.passerelle.runtime.jmx.server.FlowRepository;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class Activator implements BundleActivator {

  static final String FLOWREPOS_SERVICE_FILTER = "(&("+Constants.OBJECTCLASS+"="+FlowRepositoryService.class.getName()+")(type=FILE))";
  static final String FLOWPROC_SERVICE_FILTER = "("+Constants.OBJECTCLASS+"="+FlowProcessingService.class.getName()+")";
  
  static final String FLOWREPOS_MXBEAN_NAME = "com.isencia.passerelle.runtime:type=FlowRepository";
  static final String FLOWPROCESSOR_MXBEAN_NAME = "com.isencia.passerelle.runtime:type=FlowProcessor";
  
  private static BundleContext context;
  private static Activator instance;
  
  private ServiceTracker<Object, Object> flowRepositorySvcTracker;
  private ServiceTracker<Object, Object> flowProcessingSvcTracker;

  private FlowRepositoryService flowRepositorySvc;
  private FlowProcessingService flowProcessingSvc;
  private MBeanServer mbeanServer;
  private ObjectName flowReposMxbeanName;
  private ObjectName flowProcMxbeanName;

  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;
    Activator.instance = this;
    Filter reposSvcFilter = context.createFilter(FLOWREPOS_SERVICE_FILTER);
    Filter processSvcFilter = context.createFilter(FLOWPROC_SERVICE_FILTER);
    flowRepositorySvcTracker = new ServiceTracker<Object, Object>(bundleContext, reposSvcFilter, createSvcTrackerCustomizer());
    flowProcessingSvcTracker = new ServiceTracker<Object, Object>(bundleContext, processSvcFilter, createSvcTrackerCustomizer());
    flowRepositorySvcTracker.open();
    flowProcessingSvcTracker.open();
    
    FlowRepository flowReposMXB = new FlowRepository();
    FlowProcessor flowProcMXB = new FlowProcessor();
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
    flowReposMxbeanName = new ObjectName(FLOWREPOS_MXBEAN_NAME);
    mbeanServer.registerMBean(flowReposMXB, flowReposMxbeanName);
    flowProcMxbeanName = new ObjectName(FLOWPROCESSOR_MXBEAN_NAME);
    mbeanServer.registerMBean(flowProcMXB, flowProcMxbeanName);
  }

  public void stop(BundleContext bundleContext) throws Exception {
    if(mbeanServer!=null) {
      mbeanServer.unregisterMBean(flowReposMxbeanName);
      mbeanServer.unregisterMBean(flowProcMxbeanName);
    }
    flowRepositorySvcTracker.close();
    flowProcessingSvcTracker.close();
    Activator.context = null;
    Activator.instance = null;
  }
  
  public static Activator getInstance() {
    return instance;
  }
  
  public FlowRepositoryService getFlowReposSvc() {
    return flowRepositorySvc;
  }

  public FlowProcessingService getFlowProcessingSvc() {
    return flowProcessingSvc;
  }

  private ServiceTrackerCustomizer<Object, Object> createSvcTrackerCustomizer() {
    return new ServiceTrackerCustomizer<Object, Object>() {
      public void removedService(ServiceReference<Object> ref, Object svc) {
        synchronized (Activator.this) {
          if (svc == Activator.this.flowRepositorySvc) {
            Activator.this.flowRepositorySvc = null;
          } else if(svc == Activator.this.flowProcessingSvc) {
            Activator.this.flowProcessingSvc = null;
          } else {
            return;
          }
          context.ungetService(ref);
        }
      }

      public void modifiedService(ServiceReference<Object> ref, Object svc) {
      }

      public Object addingService(ServiceReference<Object> ref) {
        Object svc = context.getService(ref);
        synchronized (Activator.this) {
          if ((svc instanceof FlowRepositoryService) && (Activator.this.flowRepositorySvc == null)) {
            Activator.this.flowRepositorySvc = (FlowRepositoryService) svc;
          } else if ((svc instanceof FlowProcessingService) && (Activator.this.flowProcessingSvc == null)) {
            Activator.this.flowProcessingSvc = (FlowProcessingService) svc;
          } 
        }
        return svc;
      }
    };
  }
}
