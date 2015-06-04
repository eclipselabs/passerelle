/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import com.isencia.constants.IPropertyNames;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.MoMLParser;

public class ModelUtils {
  private static Configuration configuration = null;

  /**
   * Property names should follow a nested naming format<br>
   * <code>name1.name2.name3.name4=value</code> Where <code>name4</code> is the
   * parameter name and all preceeding names are nested actor names (actors
   * within composite actors within...)
   * 
   * @param rootEntity
   * @param cfgProps
   * @throws IllegalActionException
   */
  public static void configure(final CompositeActor rootEntity, final Properties cfgProps) throws IllegalActionException {

    for (final Object element : cfgProps.keySet()) {
      final String propKey = (String) element;
      final String propValue = cfgProps.getProperty(propKey);
      configure(rootEntity, propKey, propValue);
    }
  }

  /**
   * Array elements should follow format of an entry in a properties file<br>
   * <code>name1.name2.name3.name4=value</code> Where <code>name4</code> is the
   * parameter name and all preceeding names are nested actor names (actors
   * within composite actors within...)
   * 
   * @param rootEntity
   * @param cfgProps
   * @throws IllegalActionException
   */
  public static void configure(final CompositeActor rootEntity, final String[] cfgProps) throws IllegalActionException {

    for (final String propDef : cfgProps) {
      final String[] parts = propDef.split("=");
      if (parts.length < 2) {
        throw new IllegalActionException("Invalid parameter override definition " + propDef);
      }

      configure(rootEntity, parts[0], parts[1]);
    }
  }

  /**
   * Property names should follow a nested naming format<br>
   * <code>name1.name2.name3.name4=value</code> Where <code>name4</code> is the
   * parameter name and all preceeding names are nested actor names (actors
   * within composite actors within...)
   * 
   * @param rootEntity
   * @param propValue
   * @throws IllegalActionException
   */
  public static void configure(final CompositeActor rootEntity, final String propName, final String propValue) throws IllegalActionException {
    final Parameter p = getParameter(rootEntity, propName);
    p.setExpression(propValue);
  }

  /**
   * @param rootEntity
   * @param propName
   * @return
   * @throws IllegalActionException
   */
  public static Parameter getParameter(final CompositeActor rootEntity, final String propName) throws IllegalActionException {
    final String[] parts = propName.split("[\\.=]");
    if (parts.length == 0) {
      throw new IllegalActionException("Invalid parameter name " + propName);
    }

    Entity e = rootEntity;
    // parts[parts.length-1] is the parameter name
    // all the parts[] before that are part of the nested Parameter name
    for (int j = 0; j < parts.length - 1; j++) {
      if (e instanceof CompositeActor) {
        e = ((CompositeActor) e).getEntity(parts[j]);
        if (e == null) {
          throw new IllegalActionException("Invalid parameter name " + propName);
        }
      } else {
        break;
      }
    }

    final Parameter p = (Parameter) e.getAttribute(parts[parts.length - 1], Parameter.class);
    return p;
  }

  public static String getFullNameButWithoutModelName(final CompositeActor model, final Nameable namedObject) {
    // We need to obtain the full name of the NamedObj,
    // but without the model name in it.
    // But the namedObject might also be the root CompositeActor of the model,
    // in which case the Ptolemy name in fullName is the same as the model name.
    // Then we don't need to chop it.
		if (namedObject == null || (model!=null && namedObject.getName().equals(model.getName()))) {
      return "";
    }
    String fullName = namedObject.getFullName();
		if(model==null) {
			// just chop off leading '.'
			fullName = fullName.substring(1);
		} else if (model.getName()!=null && (fullName.length() > model.getName().length() + 1)) {
      final int i = fullName.indexOf(model.getName());
      if (i > 0) {
        // there's always an extra '.' in front of the model name...
        // and a trailing '.' just behind it...
        fullName = fullName.substring(i + model.getName().length() + 1);
      }
    }
    return fullName;
  }

  public static String getFullNameButWithoutModelName(final CompositeActor model, String fullName) {
    // we need to obtain the full name of the NamedObj,
    // but without the model name in it
		if(model==null || model.getName()==null)
			return fullName;
		
    final int i = fullName.indexOf(model.getName());
    if (i > 0) {
      // there's always an extra '.' in front of the model name...
      // and a trailing '.' just behind it...
      fullName = fullName.substring(i + model.getName().length() + 1);
    }

    return fullName;
  }

  public static Flow loadModel(final File filePath) throws Exception {
    if (filePath == null) {
      return null;
    }
    return loadModel(filePath.toURL());
  }

  /**
   * @param xmlFile
   * @return
   * @throws IOException
   * @throws Exception
   */
  public static Flow loadModel(final URL xmlFile) throws IOException, Exception {
    return FlowManager.readMoml(xmlFile);
  }

  /**
   * Tries to locate a file with the given relative or absolute path. <br>
   * If a straight search using java.io.File does not find it, the file is
   * searched for on the classpath using ClassLoader.getResource().
   * 
   * @param file
   * @return
   */
  public static File findFile(final String filePath) throws FileNotFoundException {
    File f = new File(filePath);
    if (!f.exists()) {
      f = new File(ModelUtils.class.getClassLoader().getResource(filePath).getPath());
      if (!f.exists()) throw new FileNotFoundException(filePath);
    }
    return f;
  }

  public static Configuration getConfiguration() {
    if (configuration == null) {
      try {
        final URL cfgURL = MoMLApplication.specToURL(System.getProperty(IPropertyNames.APP_HOME, IPropertyNames.APP_HOME_DEFAULT) + File.separator
						+ System.getProperty("com.isencia.config", "conf") + File.separator + "runConfiguration.xml");
        final MoMLParser parser = new MoMLParser();

        configuration = (Configuration) parser.parse(cfgURL, cfgURL.openStream());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return configuration;

  }
}
