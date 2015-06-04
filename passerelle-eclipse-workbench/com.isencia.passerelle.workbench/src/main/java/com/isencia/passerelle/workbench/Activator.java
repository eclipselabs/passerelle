package com.isencia.passerelle.workbench;

import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements BundleActivator {
  private Stack<Bundle> bundles = new Stack<Bundle>();
  // The plug-in ID
  public static final String PLUGIN_ID = "com.isencia.passerelle.workbench";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    DeviceData data = new DeviceData();

    data.tracking = true;

    Display display = new Display(data);

    // Sleak sleak = new Sleak();
    //
    // sleak.open();

    plugin = this;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private void start(Bundle bundle) throws BundleException {
    // fragments need not be started
    if (bundle.getHeaders().get("Fragment-Host") != null)
      return;

    // only start if bundle is resolved (4) or installed (2)
    if (bundle.getState() != 4 && bundle.getState() != 2)
      return;

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
        // application bundle should be started the last one
        // look for the required bundle in the stack
        // of bundles still to be started
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
  }

}
