package com.isencia.passerelle.actor.activator;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.actor.test.TestRunner;

public class TestFragmentActivator implements BundleActivator  {

  private ServiceRegistration<?> testCmdProvider;

  public void start(BundleContext context) throws Exception {
    testCmdProvider = context.registerService(CommandProvider.class.getName(), new TestRunner(), null);
    
  }

  public void stop(BundleContext arg0) throws Exception {
    testCmdProvider.unregister();
  }

}
