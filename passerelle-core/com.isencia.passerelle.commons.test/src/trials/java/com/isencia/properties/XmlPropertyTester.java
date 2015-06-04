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
 * Unit test script for be.isencia.properties.
 * Creation date: (2/6/01 11:11:38 AM)
 * @author: erwin
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlPropertyTester {
  private static final Logger logger = LoggerFactory.getLogger(XmlPropertyTester.class);

  /**
   * Starts the application.
   * 
   * @param args an array of command-line arguments
   */
  public static void main(java.lang.String[] args) {
    try {
      PropertyContainerFactory.instance().registerPropertySource("propertytest.xml");

      IPropertyContainer test = PropertyContainerFactory.instance().get("be.isencia.properties.test.properties");

      logger.info("Container contains:\n" + test);

      logger.info("Container name: " + test.getName());
      logger.info("Property Parsers.GeneratedEntities.WrapperClass value: " + test.getPropertyValue("Parsers.GeneratedEntities.WrapperClass"));
      logger.info("Property Parsers.GeneratedEntities.Package value: " + test.getPropertyValue("Parsers.GeneratedEntities.Package"));
      logger.info("Property Parsers.GeneratedEntities.BaseClass value: " + test.getPropertyValue("Parsers.GeneratedEntities.BaseClass"));

      logger.info("Testing list of properties");
      String[] values = test.getPropertyValueList("Entry");

      for (int i = 0; i < values.length; ++i) {
        logger.info("Found property list value " + values[i]);
      }

      logger.info("Testing conversion to plain java.util.Properties");
      java.util.Properties props = test.getLinearProperties();

      java.io.ByteArrayOutputStream bOut = new java.io.ByteArrayOutputStream();
      props.store(bOut, "Properties list");
      logger.info("Found following properties:\n" + bOut.toString());
      bOut.reset();

      logger.info("Writing properties to standard Java .properties file");
      java.io.FileOutputStream fo = new java.io.FileOutputStream("D:\\TupleTest\\propertytestout.properties");
      props.store(fo, "Result of conversion from XmlPropertyContainer to java.util.Properties");
      fo.close();

      logger.info("Reading from standard Java .properties file");
      java.util.Properties props2 = new java.util.Properties();
      java.io.FileInputStream fi = new java.io.FileInputStream("D:\\TupleTest\\propertytestout.properties");
      props2.load(fi);
      fi.close();

      props.store(bOut, "Properties list");
      logger.info("Found following properties in file:\n" + bOut.toString());

      bOut.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
