package com.isencia.passerelle.testsupport.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import com.isencia.passerelle.testsupport.actor.AsynchDelay;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;
import com.isencia.passerelle.testsupport.actor.ForLoop;
import com.isencia.passerelle.testsupport.actor.Forwarder;
import com.isencia.passerelle.testsupport.actor.MapBasedRouter;
import com.isencia.passerelle.testsupport.actor.MapModifier;
import com.isencia.passerelle.testsupport.actor.MapSource;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;
import com.isencia.passerelle.testsupport.actor.MultiBlockingInputActor;
import com.isencia.passerelle.testsupport.actor.RandomMatrixSource;
import com.isencia.passerelle.testsupport.actor.SlowLifecycleMethodsActor;
import com.isencia.passerelle.testsupport.actor.TextSource;

public class Activator implements BundleActivator {
  private static Activator instance = null;
  private BundleContext context;
  private ServiceRegistration<?> apSvcReg;
  
  public static Activator getInstance() {
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
            AsynchDelay.class,
            Const.class,
            Delay.class,
            DevNullActor.class,
            ExceptionGenerator.class,
            ForLoop.class,
            Forwarder.class,
            MapBasedRouter.class,
            MapModifier.class,
            MapSource.class,
            MessageHistoryStack.class,
            MultiBlockingInputActor.class,
            RandomMatrixSource.class,
            SlowLifecycleMethodsActor.class,
            TextSource.class
            ),
        null);
  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();
    instance = null;
  }
}
