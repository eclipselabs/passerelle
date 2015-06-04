/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.passerelle.domain.et;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;

public class Activator implements BundleActivator {

  private ServiceRegistration<ModelElementClassProvider> apSvcReg;
  
  @SuppressWarnings("unchecked")
  public void start(BundleContext context) throws Exception {
    apSvcReg = (ServiceRegistration<ModelElementClassProvider>) context.registerService(ModelElementClassProvider.class.getName(), new DefaultModelElementClassProvider(ETDirector.class), null);

  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();
    
  }

}
