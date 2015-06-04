package com.isencia.passerelle.process.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import com.isencia.passerelle.process.actor.ServiceBasedActor;
import com.isencia.passerelle.process.actor.event.EventsToTaskCollector;
import com.isencia.passerelle.process.actor.event.TaskToEventsGenerator;
import com.isencia.passerelle.process.actor.flow.BufferedTrigger;
import com.isencia.passerelle.process.actor.flow.ContextEntryModifier;
import com.isencia.passerelle.process.actor.flow.Fork;
import com.isencia.passerelle.process.actor.flow.Join;
import com.isencia.passerelle.process.actor.flow.StartActor;
import com.isencia.passerelle.project.repository.api.RepositoryService;

public class Activator implements BundleActivator {
  private static Activator defaultInstance;

  private ServiceRegistration apSvcReg;
  private BundleActivator testFragmentActivator;

  private ServiceTracker repoSvcTracker;

  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), 
        new DefaultModelElementClassProvider(
            BufferedTrigger.class, Fork.class, Join.class,
            StartActor.class, ContextEntryModifier.class,
            ServiceBasedActor.class,
            EventsToTaskCollector.class,
            TaskToEventsGenerator.class
            ),
        null);

    repoSvcTracker = new ServiceTracker(context, RepositoryService.class.getName(), null);
    repoSvcTracker.open();

    defaultInstance = this;
    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("com.isencia.passerelle.process.actor.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
  }

  public void stop(BundleContext context) throws Exception {
    if (testFragmentActivator != null) {
      testFragmentActivator.stop(context);
    }
    apSvcReg.unregister();
    defaultInstance = null;
  }

  public static Activator getDefault() {
    return defaultInstance;
  }

  public RepositoryService getRepositoryService() {
    try {
      if (repoSvcTracker == null) {
        return null;
      }

      return (RepositoryService) repoSvcTracker.waitForService(3000);
    } catch (InterruptedException ex) {
      return null;
    }
  }
}
