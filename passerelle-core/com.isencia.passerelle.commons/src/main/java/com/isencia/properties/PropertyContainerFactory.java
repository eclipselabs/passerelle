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
package com.isencia.properties;

/**
 * Factory class used to build IPropertyContainer instances from given source
 * identifiers. Currently only implemented to handle XMLPropertyContainers, with
 * XML documents read from file.
 * 
 * @author erwin
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.constants.IPropertyNames;
import com.isencia.util.StringConvertor;

public class PropertyContainerFactory {
  private static final Logger logger = LoggerFactory.getLogger(PropertyContainerFactory.class);

  /**
   * A singleton instance
   */
  private static PropertyContainerFactory instance = null;

  /**
   * Map to maintain references to property containers based on the source name.
   */
  private Map<String, IPropertyContainer> propCntsBySrcName = null;

  /**
   * Map to maintain references to property containers based on the
   * container/bundle name.
   */
  private Map<String, IPropertyContainer> propCntsByCntName = null;

  /**
   * The path where the properties files must be found.
   */
  private String propsHome = null;

  /**
   * PropertyContainerFactory constructor. Constructs the lookup maps, and the
   * path where the property files will be searched for.
   */
  public PropertyContainerFactory() {
    super();
    propCntsBySrcName = new HashMap<String, IPropertyContainer>();
    propCntsByCntName = new HashMap<String, IPropertyContainer>();
    String appRoot = System.getProperty(IPropertyNames.APP_HOME);
    propsHome = appRoot + File.separatorChar + IPropertyNames.APP_CFG_DEFAULT + File.separatorChar;
  }

  /**
   * Returns the property container instance for the given name. Typically, the
   * name consists of a package name followed by ".properties". E.g.
   * be.isencia.persist.properties
   * 
   * @return be.isencia.properties.IPropertyContainer
   * @param propCntName java.lang.String
   */
  public IPropertyContainer get(String propCntName) {
    IPropertyContainer tmp = propCntsByCntName.get(propCntName);

    return tmp;
  }

  /**
   * Returns the singleton instance.
   * 
   * @return be.isencia.properties.PropertyContainerFactory
   */
  public static PropertyContainerFactory instance() {
    if (instance == null) instance = new PropertyContainerFactory();
    return instance;
  }

  /**
   * Registers a new hierarchical properties bundle. Currently, only an
   * implementation for XML files where the propSrcName must be the short file
   * name, pointing to a file in the config directory, as defined by the system
   * property IPropertyNames.APP_HOME, and the system property
   * IPropertyNames.APP_CFG.
   * 
   * @param propSrcName java.lang.String
   * @return IPropertyContainer a container with the loaded properties
   */
  public IPropertyContainer registerPropertySource(String propSrcName) throws PropertiesLoadingException {
    if (logger.isTraceEnabled()) {
      logger.trace("registerPropertySource() - entry - name :" + propSrcName);
    }

    String fullName = propsHome + propSrcName;
    // ensure platform specific path delims are used
    fullName = StringConvertor.convertPathDelimiters(fullName);

    if (propCntsBySrcName.containsKey(propSrcName)) {
      // remove existing entry
      IPropertyContainer tmp = propCntsBySrcName.get(propSrcName);
      propCntsBySrcName.remove(propSrcName);
      propCntsByCntName.remove(tmp.getName());
    }

    IPropertyContainer cnt = new XmlPropertyContainer();
    try {
      Reader r = new FileReader(fullName);
      cnt.loadProperties(r);
      cnt.setSourceName(fullName);
      r.close();

      if (logger.isDebugEnabled()) logger.debug("registerPropertySource() - loaded properties :" + cnt);

    } catch (FileNotFoundException e) {
      throw new PropertiesLoadingException("Property file " + fullName + " not found");
    } catch (IOException e) {
      throw new PropertiesLoadingException("Property file " + fullName + " could not be read");
    }

    // store propcontainer in the maps by sourcename (filename) and
    // containername (logical name in the file)
    propCntsBySrcName.put(propSrcName, cnt);
    propCntsByCntName.put(cnt.getName(), cnt);

    if (logger.isTraceEnabled()) {
      logger.trace("registerPropertySource() - exit");
    }
    return cnt;
  }
}