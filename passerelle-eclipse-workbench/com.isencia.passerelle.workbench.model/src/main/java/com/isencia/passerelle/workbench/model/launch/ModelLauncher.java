package com.isencia.passerelle.workbench.model.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ModelLauncher {
  
    //private static final String WS_DELIM = " \t\n\r\f";
    protected static final String FILE_SCHEME = "file:";
    protected static final String FRAMEWORK_BUNDLE_NAME = "org.eclipse.osgi";
    protected static final String STARTER =
            "org.eclipse.core.runtime.adaptor.EclipseStarter";
    protected static final String FRAMEWORKPROPERTIES =
            "org.eclipse.osgi.framework.internal.core.FrameworkProperties";
    protected static final String NULL_IDENTIFIER = "@null";
    protected static final String OSGI_FRAMEWORK = "osgi.framework";
    protected static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    protected static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
    protected static final String OSGI_INSTALL_AREA = "osgi.install.area";
    protected static final String OSGI_FORCED_RESTART = "osgi.forcedRestart";
    
    private static Logger logger = LoggerFactory.getLogger(ModelLauncher.class.getName());
    
    
	public Logger getLogger() {
		return logger;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
         try {
        	ModelLauncher runner = new ModelLauncher();
        	runner.start(args);
			//EclipseStarter.startup(args, endSplashHandler)(new String[0]);
		} catch (Exception e) {
			logger.error("Error launching model",e);
		}
	}
	    /**
	     * start is used to "start" an OSGi framework
	     */
	    public synchronized void start(String[] args) {
	        Map<String, String> initialPropsMap = new HashMap<String, String>();

	        try {
	        	// TODO Use startup of SherpaBeans to automatically find the dependent bundles
	            initialPropsMap.put("osgi.bundles", 
	            		"org.eclipse.equinox.common@2:start," +
	            		"org.eclipse.core.runtime@2:start," +
	            		"org.eclipse.ant.core@3:start," +
	            		"org.eclipse.core.expressions@3:start," +
	            		"org.eclipse.core.filesystem@3:start," +
	            		"org.eclipse.core.variables@3:start," +
	            		"org.eclipse.core.contenttype@3:start," +
	            		"org.eclipse.core.jobs@3:start," +
	            		"org.eclipse.core.resources@3:start," +
	            		"org.eclipse.equinox.registry@3:start," +
	            		"org.eclipse.equinox.preferences@3:start," +
	            		"org.eclipse.equinox.app@3:start," +
	            		"org.apache.log4j," +
	            		"org.slf4j," +
	            		"org.slf4j.impl," +
	            		"org.jdom," +
	            		"mail," +
	            		"activation," +
	            		"com.microstar@3:start," +
	            		"ptolemy.core@3:start," +
	            		"javax.xml," +
	            		"org.apache.commons.beanutils," +
	            		"org.apache.commons.codec," +
	            		"org.apache.commons.collections," +
	            		"org.apache.httpcomponents.httpclient," +
                  "org.apache.httpcomponents.httpcore," +
	            		"org.apache.commons.logging," +
	            		"org.apache.commons.math," +
	            		"org.apache.xerces," +
	            		"org.apache.xml.serializer," +
	            		"org.apache.xml.resolver," +
	            		"com.isencia.passerelle.workbench.model@3:start,"
	            		+ "com.isencia.passerelle.workbench.logging.development,"
//	            		+ "com.isencia.passerelle.workbench.logging.production"
	            		);
	            
	            initialPropsMap.put("osgi.clean", "true");
	            EclipseStarter.setInitialProperties(initialPropsMap);
	            BundleContext bc = EclipseStarter.startup(args, null);
                Bundle[] bundles = bc.getBundles();
                for (int i = 0; i < bundles.length; i++) {
                	if( getLogger().isDebugEnabled() ) {
                		getLogger().debug("Bundle : "+ bundles[i].getSymbolicName() + ", state : "+bundles[i].getState());
                	}
                }
                
	        	// Invoke the specified application
	            EclipseStarter.run(null);
	            EclipseStarter.shutdown();
	        } catch (InvocationTargetException ite) {
	            Throwable t = ite.getTargetException();
	            if (t == null) {
	                t = ite;
	            }
	            throw new RuntimeException(t.getMessage());
	        } catch (Exception e) {
	        	getLogger().error("Error launching model",e);
	            throw new RuntimeException(e.getMessage());
	        } 
	    }

	    private String concatArg(String arguments, String arg) {
			if (arguments.length() > 0 && !arguments.endsWith(" ")) //$NON-NLS-1$
				arguments = arguments.concat(" "); //$NON-NLS-1$
			return arguments.concat(arg);
		}


}
