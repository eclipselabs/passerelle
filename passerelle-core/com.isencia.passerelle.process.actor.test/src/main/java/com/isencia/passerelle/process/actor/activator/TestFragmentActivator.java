/* Copyright 2012 - iSencia Belgium NV

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
package com.isencia.passerelle.process.actor.activator;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import com.isencia.passerelle.process.actor.test.suite.TestRunner;
import com.isencia.passerelle.process.actor.trial.ContextTracerConsole;
import com.isencia.passerelle.process.actor.trial.MockEventGenerator;
import com.isencia.passerelle.process.actor.trial.MockTaskActor;

public class TestFragmentActivator implements BundleActivator  {

  private ServiceRegistration<?> testCmdProvider;
  private static TestFragmentActivator instance = null;
  private BundleContext context;
  private ServiceRegistration<?> apSvcReg;
  
  public static TestFragmentActivator getInstance() {
    return instance;
  }
  
  public BundleContext getBundleContext() {
    return context;
  }

  public void start(BundleContext context) throws Exception {
    this.context = context;
    instance = this;
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), 
        new DefaultModelElementClassProvider(
            ContextTracerConsole.class,MockEventGenerator.class,MockTaskActor.class
            ),
        null);
    testCmdProvider = context.registerService(CommandProvider.class.getName(), new TestRunner(), null);
  }

  public void stop(BundleContext context) throws Exception {
    testCmdProvider.unregister();
    apSvcReg.unregister();
    instance = null;
  }

}
