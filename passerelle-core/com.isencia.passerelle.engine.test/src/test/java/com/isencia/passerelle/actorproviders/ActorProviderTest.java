package com.isencia.passerelle.actorproviders;

import junit.framework.TestCase;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import com.isencia.passerelle.engine.activator.TestFragmentActivator;
import com.isencia.passerelle.ext.ModelElementClassProvider;

public class ActorProviderTest extends TestCase {

  
  public void testRegisteredProviders() throws InvalidSyntaxException {
    ServiceReference<?>[] serviceReferences = TestFragmentActivator.getInstance().getBundleContext().getServiceReferences(ModelElementClassProvider.class.getName(), null);
    for (ServiceReference<?> serviceReference : serviceReferences) {
      System.out.println("Found actor bundle "+serviceReference.getBundle().getSymbolicName());
    }
  }
  
  
}
