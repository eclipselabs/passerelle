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
package com.isencia.passerelle.runtime.process.impl.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.impl.FlowProcessingServiceImpl;

public class Activator implements BundleActivator {
	private FlowProcessingService processSvc;
	private ServiceRegistration<FlowProcessingService> processSvcreg;

	// TODO make max concurrent runs configurable
	public void start(BundleContext context) throws Exception {
	  processSvc = new FlowProcessingServiceImpl(Runtime.getRuntime().availableProcessors());
	  processSvcreg = (ServiceRegistration<FlowProcessingService>) context.registerService(FlowProcessingService.class.getName(), processSvc, null);
	}

	public void stop(BundleContext context) throws Exception {
	  processSvcreg.unregister();
	  processSvc = null;
	}
}
