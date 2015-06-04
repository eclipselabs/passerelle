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
package com.isencia.passerelle.runtime.ws.rest.client.activator;

import java.util.Arrays;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.ws.rest.client.FlowProcessingServiceRESTClient;
import com.isencia.passerelle.runtime.ws.rest.client.FlowRepositoryServiceRESTClient;

public class Activator implements BundleActivator {

  private FlowRepositoryServiceRESTClient repoSvc;
  private FlowProcessingServiceRESTClient procSvc;
  private ServiceRegistration<FlowRepositoryService> repoSvcReg;
  private ServiceRegistration<FlowProcessingService> procSvcReg;
  private static Activator plugin;

  public void start(BundleContext context) throws Exception {
    String debugStr      = System.getProperty("com.isencia.passerelle.runtime.ws.rest.client.debug", "false");
    String resourceRootURL      = System.getProperty("com.isencia.passerelle.runtime.ws.rest.client.resourceURL", "http://localhost/rest");
    Hashtable<String, String> svcProps = new Hashtable<String, String>();
    svcProps.put("debug", debugStr);
    svcProps.put("resourceURL", resourceRootURL+"/flows");
    svcProps.put("type", "REST");
    
    repoSvc = new FlowRepositoryServiceRESTClient();
    repoSvc.init(svcProps);
    
    repoSvcReg = (ServiceRegistration<FlowRepositoryService>) 
        context.registerService(FlowRepositoryService.class.getName(), repoSvc, svcProps);

    svcProps.put("resourceURL", resourceRootURL+"/processes");
    
    procSvc = new FlowProcessingServiceRESTClient();
    procSvc.init(svcProps);
    
    procSvcReg = (ServiceRegistration<FlowProcessingService>) 
        context.registerService(FlowProcessingService.class.getName(), procSvc, svcProps);
    plugin = this;
  }

  public void stop(BundleContext context) throws Exception {
    repoSvcReg.unregister();
    procSvcReg.unregister();
    procSvc = null;
    repoSvc = null;
  }
  
  public FlowRepositoryService getRepositoryService() {
    return repoSvc;
  }
  
  public FlowProcessingService getProcessingService() {
    return procSvc;
  }
  
  public static Activator getDefault() {
    return plugin;
  }
}