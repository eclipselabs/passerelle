/**
 * 
 */
package com.isencia.passerelle.process.service.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.edm.backend.service.common.BackendService;

/**
 * Track presence of the asynchronous service registered with a unique service name This tracker always returns the most
 * recent version of a Service.
 * 
 * @author puidir
 * 
 */
public class BackendServiceTracker extends ServiceTracker<BackendService, BackendService> {

  public BackendServiceTracker(BundleContext bundleContext, String serviceName) throws InvalidSyntaxException {
    super(bundleContext, bundleContext.createFilter("(serviceName=" + serviceName + ")"), null);
  }

  @Override
  public BackendService getService() {

    // Get the most recent version of the same Service (i.e. registered under the same name)

    if (getServiceReferences() == null) {
      return null;
    }

    if (getServiceReferences().length == 1) {
      return super.getService();
    }

    ServiceReference<BackendService> mostRecentServiceRef = null;

    for (ServiceReference<BackendService> reference : getServiceReferences()) {
      if (mostRecentServiceRef == null) {
        mostRecentServiceRef = reference;
        continue;
      }

      // Remark: with newer osgi versions, use Bundle.getVersion
      Version mostRecentBundleVersion = new Version((String) mostRecentServiceRef.getBundle().getHeaders().get("Bundle-Version"));
      Version checkedBundleVersion = new Version((String) reference.getBundle().getHeaders().get("Bundle-Version"));

      if (checkedBundleVersion.compareTo(mostRecentBundleVersion) > 0) {
        mostRecentServiceRef = reference;
      }
    }

    return getService(mostRecentServiceRef);
  }

}
