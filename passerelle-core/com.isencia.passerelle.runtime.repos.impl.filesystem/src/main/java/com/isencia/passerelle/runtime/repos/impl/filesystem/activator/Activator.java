/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.runtime.repos.impl.filesystem.activator;

import java.io.File;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.repos.impl.filesystem.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;

public class Activator implements BundleActivator {

  private FlowRepositoryService repoSvc;
  private ServiceRegistration<FlowRepositoryService> repoSvcReg;

  public void start(BundleContext context) throws Exception {
    File userHome              = new File(System.getProperty("user.home"));
    File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
    String rootFolderPath      = System.getProperty("com.isencia.passerelle.repository.root", defaultRootFolderPath.getAbsolutePath());
    repoSvc = new FlowRepositoryServiceImpl(rootFolderPath);
    Hashtable<String, String> svcProps = new Hashtable<String, String>();
    svcProps.put("type", "FILE");
    repoSvcReg = (ServiceRegistration<FlowRepositoryService>) context.registerService(FlowRepositoryService.class.getName(), repoSvc, svcProps);
  }

  public void stop(BundleContext context) throws Exception {
    repoSvcReg.unregister();
    repoSvc = null;
  }
  
  public FlowRepositoryService getRepositoryService() {
    return repoSvc;
  }
}
