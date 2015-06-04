package com.isencia.passerelle.starter;

import java.util.Stack;
import java.util.StringTokenizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator that starts all installed and resolved bundles on the BundleContext, in the order defined by the
 * <code>Require-Bundle</code> list of each bundle.
 * 
 * @author Jan Vermeulen (verjan@isencia.com)
 */
public class Activator implements BundleActivator, BundleListener {
  private Stack<Bundle> bundles = new Stack<Bundle>();
  private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  public void start(BundleContext context) throws Exception {
    context.addBundleListener(this);

    for (Bundle bundle : context.getBundles()) {
      
      if (!(bundle.getSymbolicName().equals("com.isencia.passerelle.workbench")) && !(bundle.getSymbolicName().contains("eclipse") && !bundle.getSymbolicName().contains("persistence")) && !bundle.equals(context.getBundle()))
        bundles.push(bundle);

    }

    // while there are bundles still to be started...
    Bundle bundle = null;

    while (!bundles.isEmpty()) {
      bundle = bundles.pop();
      try {
        start(bundle);
      } catch (BundleException e) {
      }
    }
  }

  private void start(Bundle bundle) throws BundleException {
    // first start any required bundles
    String requiredBundles = (String) bundle.getHeaders().get("Require-Bundle");
    if (requiredBundles != null) {
      StringTokenizer tokenizer = new StringTokenizer(requiredBundles, ",");
      while (tokenizer.hasMoreTokens()) {
        String bundleName = tokenizer.nextToken();
        // strip version info
        int index = bundleName.indexOf(';');
        if (index > 0)
          bundleName = bundleName.substring(0, index);

        // look for the required bundle in the stack of bundles still to be started
        for (int i = 0; i < bundles.size(); i++) {
          Bundle requiredBundle = bundles.get(i);
          if (requiredBundle.getSymbolicName().equals(bundleName)) {
            // remove the required bundle from the stack
            bundles.remove(i);
            start(requiredBundle);
            break;
          }
        }
      }
    }

    // try to start the bundle
    bundle.start();
    LOGGER.debug(bundle.getSymbolicName() + " started.");
  }

  public void stop(BundleContext context) throws Exception {
    context.removeBundleListener(this);
  }

  public void bundleChanged(BundleEvent event) {
    if (event.getType() == 32)
      try {
        start(event.getBundle());
      } catch (BundleException e) {
    	  LOGGER.error(event.getBundle().getSymbolicName() + " start failed.", e);
      }
  }
}