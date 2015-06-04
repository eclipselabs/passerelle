package com.isencia.passerelle.workbench.model.editor.ui;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.isencia.passerelle.editor.common.model.MomlClassRegistry;

import com.isencia.passerelle.project.repository.api.RepositoryService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.isencia.passerelle.workbench.model.editor.ui";

  // The shared instance
  private static Activator plugin;
  private BundleContext bundleContext;
  private ServiceTracker repoSvcTracker;
  private ServiceRegistration submodelSvcReg;

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
    this.bundleContext = context;
    plugin = this;
    repoSvcTracker = new ServiceTracker(context, RepositoryService.class.getName(), null);
    repoSvcTracker.open();

    MomlClassRegistry.setService(new MomlClassService());

    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    String submodelPath = store.getString(RepositoryService.SUBMODEL_ROOT);
    if (submodelPath == null || submodelPath.trim().equals("")) {
      File userHome = new File(System.getProperty("user.home"));
      File defaultSubmodelPath = new File(userHome, ".passerelle/submodel-repository");
      submodelPath = System.getProperty(RepositoryService.SUBMODEL_ROOT, defaultSubmodelPath.getAbsolutePath());
      store.setValue(RepositoryService.SUBMODEL_ROOT, submodelPath);
    } else {
      System.setProperty(RepositoryService.SUBMODEL_ROOT, submodelPath);
    }
    // just call this here, so we're sure the submodel folder pref has been read and applied to the repo svc,
    // before opening any editor
    getRepositoryService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    this.bundleContext = null;
    super.stop(context);
    repoSvcTracker.close();
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
    return getImageDescriptor(PLUGIN_ID, path);
  }

  public static ImageDescriptor getImageDescriptor(String plugin, String path) {
    return imageDescriptorFromPlugin(plugin, path);
  }

  public RepositoryService getRepositoryService() {
    try {
      RepositoryService repositoryService = (RepositoryService) (repoSvcTracker != null ? repoSvcTracker.waitForService(3000) : null);
      if (repositoryService == null){
        return null;
      }
      File userHome = new File(System.getProperty("user.home"));
      File defaultSubmodelPath = new File(userHome, ".passerelle/submodel-repository");
      File folder = new File(System.getProperty(RepositoryService.SUBMODEL_ROOT, defaultSubmodelPath.getAbsolutePath()));
      if (!folder.exists()) {
        folder.mkdirs();
      }
      repositoryService.setSubmodelFolder(folder);
      return repositoryService;
    } catch (InterruptedException e) {
      return null;
    }
  }

  public BundleContext getBundleContext() {
    return this.bundleContext;
  }
}
