/**
 * 
 */
package com.isencia.passerelle.process.model.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.process.model.ResultItemFromRawBuilderRegistry;
import com.isencia.passerelle.process.model.impl.ResultItemFromRawBuilderNOP;
import com.isencia.passerelle.process.model.impl.ResultItemFromRawBuilderRegistryImpl;

/**
 * @author "puidir"
 * 
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<ResultItemFromRawBuilderRegistry> registryServiceRegistration;

  public void start(BundleContext bundleContext) throws Exception {
    ResultItemFromRawBuilderRegistryImpl rawBuilderRegistryImpl = new ResultItemFromRawBuilderRegistryImpl();
    rawBuilderRegistryImpl.registerBuilder(new ResultItemFromRawBuilderNOP());
    registryServiceRegistration = bundleContext.registerService(ResultItemFromRawBuilderRegistry.class, rawBuilderRegistryImpl, null);
  }

  public void stop(BundleContext bundleContext) throws Exception {
    if (registryServiceRegistration != null) {
      registryServiceRegistration.unregister();
    }
  }

}
