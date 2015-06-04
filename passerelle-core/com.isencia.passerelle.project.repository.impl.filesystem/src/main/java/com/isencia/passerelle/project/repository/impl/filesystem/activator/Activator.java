package com.isencia.passerelle.project.repository.impl.filesystem.activator;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.project.repository.impl.filesystem.FileSystemBasedRepositoryService;

public class Activator implements BundleActivator {

  private RepositoryService repoSvc;
  public RepositoryService getRepositoryService() {
    return repoSvc;
  }
  private ServiceRegistration submodelSvcReg;
  private ServiceRegistration repoSvcReg;
  private static Activator plugin;

  public void start(BundleContext context) throws Exception {
    File userHome              = new File(System.getProperty("user.home"));
    File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
    File defaultSubmodelPath   = new File(userHome, ".passerelle/submodel-repository");
    String rootFolderPath      = System.getProperty("com.isencia.passerelle.project.root", defaultRootFolderPath.getAbsolutePath());
    String submodelPath        = System.getProperty("com.isencia.passerelle.submodel.root", defaultSubmodelPath.getAbsolutePath());
    repoSvc = new FileSystemBasedRepositoryService(rootFolderPath, submodelPath);
    repoSvcReg = context.registerService(RepositoryService.class.getName(), repoSvc, null);

    submodelSvcReg = context.registerService(ActorOrientedClassProvider.class.getName(), new SubmodelProvider(), null);

    plugin = this;
  }

  public void stop(BundleContext context) throws Exception {
    repoSvcReg.unregister();

    repoSvc = null;
  }
  public static Activator getDefault() {
    return plugin;
  }
}
