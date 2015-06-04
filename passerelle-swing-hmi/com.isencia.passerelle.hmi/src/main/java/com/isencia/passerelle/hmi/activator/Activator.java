package com.isencia.passerelle.hmi.activator;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.isencia.passerelle.hmi.generic.PasserelleGUIBuilder;

public class Activator implements BundleActivator {

  /*
   * (non-Javadoc)
   * @see
   * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    // look & feel
    try {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
    } catch (final InstantiationException e) {
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    } catch (final UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
    (new Thread() {

      @Override
      public void run() {
        new PasserelleGUIBuilder(true, true, true);
      }
    }).start();
  }

  /*
   * (non-Javadoc)
   * @see
   * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
  }

}
