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
package com.isencia.passerelle.engine.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.TypeConverterProvider;
import com.isencia.passerelle.ext.impl.OSGiClassLoadingStrategy;
import com.isencia.passerelle.ext.impl.SimpleClassLoadingStrategy;
import com.isencia.passerelle.message.type.TypeConversionChain;
import com.isencia.passerelle.model.util.MoMLParser;


public class Activator implements BundleActivator {
	
	private BundleContext bundleContext;
	
	private ServiceTracker typeCvtSvcTracker;
	private ServiceTracker mecpSvcTracker;
  private ServiceTracker aocpSvcTracker;
  
  private BundleActivator testFragmentActivator;
  
  private OSGiClassLoadingStrategy classLoadingStrategy;

	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;
		typeCvtSvcTracker = new TypeConverterProviderSvcTracker(context);
		typeCvtSvcTracker.open();
		
    classLoadingStrategy = new OSGiClassLoadingStrategy(new SimpleClassLoadingStrategy());
    MoMLParser.setClassLoadingStrategy(classLoadingStrategy);
    
		mecpSvcTracker = new ServiceTracker(context, ModelElementClassProvider.class.getName(), createClassProviderSvcTrackerCustomizer());
		mecpSvcTracker.open();
		aocpSvcTracker = new ServiceTracker(context, ActorOrientedClassProvider.class.getName(), createClassProviderSvcTrackerCustomizer());
		aocpSvcTracker.open();

    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("com.isencia.passerelle.engine.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
	}

	public void stop(BundleContext context) throws Exception {
    if(testFragmentActivator!=null) {
      testFragmentActivator.stop(context);
    }
    MoMLParser.setClassLoadingStrategy(new SimpleClassLoadingStrategy());
		typeCvtSvcTracker.close();
		mecpSvcTracker.close();
		aocpSvcTracker.close();
	}
	
  private ServiceTrackerCustomizer createClassProviderSvcTrackerCustomizer() {
    return new ServiceTrackerCustomizer() {
      public void removedService(ServiceReference ref, Object svc) {
        synchronized (Activator.this) {
          if (svc instanceof ModelElementClassProvider) {
            classLoadingStrategy.removeModelElementClassProvider((ModelElementClassProvider) svc);
          } else if(svc instanceof ActorOrientedClassProvider) {
            classLoadingStrategy.removeActorOrientedClassProvider((ActorOrientedClassProvider) svc);
          }
        }
      }
      public void modifiedService(ServiceReference ref, Object svc) {
      }
      public Object addingService(ServiceReference ref) {
        Object svc = bundleContext.getService(ref);
        synchronized (Activator.this) {
          if (svc instanceof ModelElementClassProvider) {
            classLoadingStrategy.addModelElementClassProvider((ModelElementClassProvider) svc);
          } else if(svc instanceof ActorOrientedClassProvider) {
            classLoadingStrategy.addActorOrientedClassProvider((ActorOrientedClassProvider) svc);
          }
        }
        return svc;
      }
    };
  }


	
	static class TypeConverterProviderSvcTracker extends ServiceTracker {
		public TypeConverterProviderSvcTracker(BundleContext context) {
			super(context, TypeConverterProvider.class.getName(), null);
		}

		@Override
		public TypeConverterProvider addingService(ServiceReference reference) {
			TypeConverterProvider provider = (TypeConverterProvider) super.addingService(reference);
			TypeConversionChain.getInstance().setConverterProvider(provider);
			return provider;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object service) {
			super.modifiedService(reference, service);
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			super.removedService(reference, service);
			TypeConversionChain.getInstance().setConverterProvider(null);
		}
	}
}
