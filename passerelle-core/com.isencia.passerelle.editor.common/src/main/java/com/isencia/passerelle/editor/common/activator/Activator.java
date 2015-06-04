package com.isencia.passerelle.editor.common.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class Activator implements BundleActivator {
  private ActorOrientedClassProviderTracker actorClassProviderTracker;

  private static Activator plugin;

  public static Activator getDefault() {
    return plugin;
  }

  public Activator() {
  }

  public void start(BundleContext context) throws Exception {

    plugin = this;
    actorClassProviderTracker = new ActorOrientedClassProviderTracker(context);
    actorClassProviderTracker.open();
  }

  public void stop(BundleContext context) throws Exception {
    actorClassProviderTracker.close();
  }

  public ActorOrientedClassProvider getActorOrientedClassProvider() {
    return actorClassProviderTracker != null ? actorClassProviderTracker.actorOrientedClassProvider : null;
  }

  private static class ActorOrientedClassProviderTracker extends ServiceTracker {

    private ActorOrientedClassProvider actorOrientedClassProvider;

    public ActorOrientedClassProviderTracker(BundleContext context) {
      super(context, ActorOrientedClassProvider.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
      actorOrientedClassProvider = (ActorOrientedClassProvider) super.addingService(reference);
      return actorOrientedClassProvider;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
      super.removedService(reference, service);
      actorOrientedClassProvider = null;
    }
  }

}
