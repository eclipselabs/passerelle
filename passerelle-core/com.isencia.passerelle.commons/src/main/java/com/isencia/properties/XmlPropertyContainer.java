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
 * Implementation of a hierarchical properties container based on an XML
 * document.
 * 
 * @author erwin
 */

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlPropertyContainer extends XmlHierarchicProperty implements IPropertyContainer {

  /**
   * Mandatory tag name of the root element for an XML document that contains
   * hierarchical properties.
   */
  private final static String rootTag = "PropertiesBag";

  /**
   * The JDOM document containing the parsed XML.
   */
  private Document properties = null;

  /**
   * The name of the properties bundle, as defined in the XML document
   */
  private String name = null;

  /**
   * The source name, e.g. a file name, where the XML was read from.
   */
  private String sourceName = null;

  /**
   * XmlPropertyContainer constructor.
   */
  protected XmlPropertyContainer() {
    super();
  }

  /**
   * Returns the name of this property container.
   * 
   * @return java.lang.String
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the name of the source where the property settings were obtained.
   * 
   * @return java.lang.String
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Load the hierarchical property definitions from the given Reader.
   * 
   * @param inp java.io.Reader
   */
  public void loadProperties(java.io.Reader inp) throws PropertiesLoadingException {

    try {
      SAXBuilder builder = new SAXBuilder(false);
      properties = builder.build(inp);
      parseProperties();
    } catch (JDOMException e) {
      throw new PropertiesLoadingException("Properties input read failed", e);
    } catch (IOException e) {
      throw new PropertiesLoadingException("Properties input read failed", e);
    }
  }

  /**
   * Checks whether the XML's root element complies with the expected root tag
   * name for an XML properties container, and identifies the name of this
   * hierarchical properties bundle.
   */
  private void parseProperties() throws PropertiesLoadingException {

    rootElement = properties.getRootElement();

    if (rootElement == null || !rootElement.getName().equals(rootTag)) {
      throw new PropertiesLoadingException("Root element name " + rootTag + " not found in properties file.");
    }

    name = rootElement.getAttributeValue("name");
  }

  /**
   * Sets the name of the source where the property settings were obtained.
   * 
   * @param newSourceName java.lang.String
   */
  public void setSourceName(String newSourceName) {
    sourceName = newSourceName;
  }

  /**
   * Returns a String that represents the value of this object.
   * 
   * @return a string representation of the receiver
   */
  public String toString() {
    Writer w = new StringWriter();
    try {
      outputter.output(properties, w);
    } catch (Exception e) {
    }

    return w.toString();
  }
}