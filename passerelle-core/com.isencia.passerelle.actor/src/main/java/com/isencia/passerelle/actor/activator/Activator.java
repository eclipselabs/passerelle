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
package com.isencia.passerelle.actor.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.actor.advanced.AdvancedActor;
import com.isencia.passerelle.actor.advanced.DelayActor;
import com.isencia.passerelle.actor.advanced.MultiThreadedActor;
import com.isencia.passerelle.actor.control.Stop;
import com.isencia.passerelle.actor.control.Timer;
import com.isencia.passerelle.actor.control.Trigger;
import com.isencia.passerelle.actor.convert.HeaderModifier;
import com.isencia.passerelle.actor.dynaport.DynamicNamedInputPortsActor;
import com.isencia.passerelle.actor.dynaport.DynamicNamedOutputPortsActor;
import com.isencia.passerelle.actor.dynaport.DynamicPortsActor;
import com.isencia.passerelle.actor.dynaport.InputPortBuilder;
import com.isencia.passerelle.actor.dynaport.InputPortConfigurationExtender;
import com.isencia.passerelle.actor.dynaport.InputPortSetterBuilder;
import com.isencia.passerelle.actor.dynaport.OutputPortBuilder;
import com.isencia.passerelle.actor.dynaport.OutputPortConfigurationExtender;
import com.isencia.passerelle.actor.dynaport.OutputPortSetterBuilder;
import com.isencia.passerelle.actor.eip.MessageFilter;
import com.isencia.passerelle.actor.eip.MessageRouter;
import com.isencia.passerelle.actor.error.AbstractErrorHandlerActor;
import com.isencia.passerelle.actor.error.ErrorCatcher;
import com.isencia.passerelle.actor.error.ErrorHandlerByCausingActor;
import com.isencia.passerelle.actor.error.ErrorHandlerByCodeRange;
import com.isencia.passerelle.actor.error.ErrorHandlerBySeverity;
import com.isencia.passerelle.actor.error.ErrorObserver;
import com.isencia.passerelle.actor.filter.HeaderFilter;
import com.isencia.passerelle.actor.filter.RegExpFilter;
import com.isencia.passerelle.actor.flow.Delay;
import com.isencia.passerelle.actor.flow.LoopController;
import com.isencia.passerelle.actor.flow.RetryLoopActor;
import com.isencia.passerelle.actor.flow.Switch;
import com.isencia.passerelle.actor.flow.Synchronizer;
import com.isencia.passerelle.actor.ftp.FtpFileWriter;
import com.isencia.passerelle.actor.ftp.FtpReader;
import com.isencia.passerelle.actor.ftp.FtpWriter;
import com.isencia.passerelle.actor.general.CommandExecutor;
import com.isencia.passerelle.actor.general.Console;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.actor.general.Counter;
import com.isencia.passerelle.actor.general.DevNullActor;
import com.isencia.passerelle.actor.general.ErrorConsole;
import com.isencia.passerelle.actor.general.TracerConsole;
import com.isencia.passerelle.actor.io.FileReader;
import com.isencia.passerelle.actor.io.FileWriter;
import com.isencia.passerelle.actor.mail.CreateMimeMessageActor;
import com.isencia.passerelle.actor.mail.MailReceiver;
import com.isencia.passerelle.actor.mail.SMTPSender;
import com.isencia.passerelle.actor.net.MulticastReceiver;
import com.isencia.passerelle.actor.net.MulticastSender;
import com.isencia.passerelle.actor.net.SocketClientSender;
import com.isencia.passerelle.actor.net.SocketCltSndOptionsFactory;
import com.isencia.passerelle.actor.net.SocketServerReceiver;
import com.isencia.passerelle.actor.net.SocketServerRequestReplier;
import com.isencia.passerelle.actor.net.SocketSvrRcvOptionsFactory;
import com.isencia.passerelle.actor.net.SocketSvrReqReplierOptionsFactory;
import com.isencia.passerelle.actor.sequence.ArrayToSequenceConverter;
import com.isencia.passerelle.actor.sequence.MessagesToArrayConverter;
import com.isencia.passerelle.actor.sequence.SequenceToArrayConverter;
import com.isencia.passerelle.actor.sequence.SequenceTracker;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.ext.impl.DefaultModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class Activator implements BundleActivator {

  private ServiceRegistration apSvcReg;
  private BundleActivator testFragmentActivator;

  public void start(BundleContext context) throws Exception {
    apSvcReg = context.registerService(ModelElementClassProvider.class.getName(), new DefaultModelElementClassProvider(Stop.class, Timer.class, Trigger.class,
        HeaderModifier.class, DynamicNamedInputPortsActor.class, DynamicNamedOutputPortsActor.class, DynamicPortsActor.class, InputPortBuilder.class,
        InputPortConfigurationExtender.class, InputPortSetterBuilder.class, OutputPortBuilder.class, OutputPortConfigurationExtender.class, OutputPortSetterBuilder.class,
        MessageFilter.class, MessageRouter.class, AbstractErrorHandlerActor.class, ErrorCatcher.class, ErrorHandlerByCausingActor.class, ErrorHandlerByCodeRange.class,
        ErrorHandlerBySeverity.class, ErrorObserver.class, HeaderFilter.class, RegExpFilter.class, Delay.class, LoopController.class, RetryLoopActor.class, Switch.class,
        Synchronizer.class, FtpFileWriter.class, FtpReader.class, FtpWriter.class, CommandExecutor.class, Console.class, Const.class, Counter.class, DevNullActor.class,
        ErrorConsole.class, TracerConsole.class, FileReader.class, FileWriter.class, MailReceiver.class, SMTPSender.class, MulticastReceiver.class, MulticastSender.class,
        SocketClientSender.class, SocketCltSndOptionsFactory.class, SocketServerReceiver.class, SocketServerRequestReplier.class, SocketSvrRcvOptionsFactory.class,
        SocketSvrReqReplierOptionsFactory.class, ArrayToSequenceConverter.class, MessagesToArrayConverter.class, SequenceToArrayConverter.class, SequenceTracker.class,
        AdvancedActor.class, DelayActor.class, MultiThreadedActor.class, CreateMimeMessageActor.class) {

      @Override
      public Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
        if (className.startsWith("be.isencia")) {
          return super.getClass(className.replace("be.isencia", "com.isencia"), versionSpec);
        }
        return super.getClass(className, versionSpec);
      }

    }, null);

    try {
      Class<? extends BundleActivator> svcTester = (Class<? extends BundleActivator>) Class.forName("com.isencia.passerelle.actor.activator.TestFragmentActivator");
      testFragmentActivator = svcTester.newInstance();
      testFragmentActivator.start(context);
    } catch (ClassNotFoundException e) {
      // ignore, means the test fragment is not present...
      // it's a dirty way to find out, but don't know how to discover fragment contribution in a better way...
    }
  }

  public void stop(BundleContext context) throws Exception {
    apSvcReg.unregister();

    if (testFragmentActivator != null) {
      testFragmentActivator.stop(context);
    }
  }

}
