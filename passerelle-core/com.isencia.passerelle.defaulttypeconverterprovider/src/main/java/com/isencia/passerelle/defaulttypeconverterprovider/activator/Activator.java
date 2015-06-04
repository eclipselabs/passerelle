package com.isencia.passerelle.defaulttypeconverterprovider.activator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.isencia.passerelle.defaulttypeconverterprovider.DefaultTypeConvertorProvider;
import com.isencia.passerelle.ext.TypeConverterProvider;


public class Activator implements BundleActivator {

	private ServiceRegistration svcReg;

	public void start(BundleContext context) throws Exception {
		svcReg = context.registerService(TypeConverterProvider.class.getName(), new DefaultTypeConvertorProvider(), null);
	}

	public void stop(BundleContext context) throws Exception {
		svcReg.unregister();
	}
}
