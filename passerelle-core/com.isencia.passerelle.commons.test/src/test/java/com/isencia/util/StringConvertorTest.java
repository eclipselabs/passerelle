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
package com.isencia.util;

import java.io.File;
import junit.framework.TestCase;

/**
 * StringConvertorTest TODO: class comment
 * 
 * @author erwin
 */
public class StringConvertorTest extends TestCase {
  private static final String src = "blablbealjbf/7834thiure/iu3r5irf\\iuwerguier\\//blablabla//${be.isencia.home}37894te:${be.isencia.prop}";
  private static final String firstCharToUppercase = "Blablbealjbf/7834thiure/iu3r5irf\\iuwerguier\\//blablabla//${be.isencia.home}37894te:${be.isencia.prop}";
  private static final String substSysProps = "blablbealjbf/7834thiure/iu3r5irf\\iuwerguier\\//blablabla//HOME37894te:PROP";
  private static final String convertPathDelims = "blablbealjbf" + File.separatorChar + "7834thiure" + File.separatorChar + "iu3r5irf" + File.separatorChar
      + "iuwerguier" + File.separatorChar + File.separatorChar + File.separatorChar + "blablabla" + File.separatorChar + File.separatorChar
      + "${be.isencia.home}37894te:${be.isencia.prop}";

  private static final String booleanSrc1 = "y";
  private static final String booleanSrc2 = "Y";
  private static final String booleanSrc3 = "on";
  private static final String booleanSrc4 = "t";
  private static final String booleanSrc5 = "T";
  private static final String booleanSrc6 = "1";
  private static final String booleanSrc7 = "1and some more";
  private static final String booleanSrc8 = "off";
  private static final String booleanSrc9 = " t";

  /*
   * (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    System.setProperty("be.isencia.home", "HOME");
    System.setProperty("be.isencia.prop", "PROP");
  }

  public void testFirstCharToLowerCase() {
    assertEquals(src, StringConvertor.firstCharToLowerCase(src));
  }

  public void testConvertPathDelimiters() {
    assertEquals(convertPathDelims, StringConvertor.convertPathDelimiters(src));
  }

  public void testFirstCharToUpperCase() {
    assertEquals(firstCharToUppercase, StringConvertor.firstCharToUpperCase(src));
  }

  public void testStringToBoolean() {
    assertTrue(booleanSrc1 + " should be true", StringConvertor.stringToBoolean(booleanSrc1).booleanValue());
    assertTrue(booleanSrc2 + " should be true", StringConvertor.stringToBoolean(booleanSrc2).booleanValue());
    assertTrue(booleanSrc3 + " should be true", StringConvertor.stringToBoolean(booleanSrc3).booleanValue());
    assertTrue(booleanSrc4 + " should be true", StringConvertor.stringToBoolean(booleanSrc4).booleanValue());
    assertTrue(booleanSrc5 + " should be true", StringConvertor.stringToBoolean(booleanSrc5).booleanValue());
    assertTrue(booleanSrc6 + " should be true", StringConvertor.stringToBoolean(booleanSrc6).booleanValue());
    assertTrue(booleanSrc7 + " should be true", StringConvertor.stringToBoolean(booleanSrc7).booleanValue());
    assertFalse(booleanSrc8 + " should be false", StringConvertor.stringToBoolean(booleanSrc8).booleanValue());
    assertFalse(booleanSrc9 + " should be false", StringConvertor.stringToBoolean(booleanSrc9).booleanValue());
  }

  public void testSubstituteSystemProperties() {
    assertEquals(substSysProps, StringConvertor.substituteSystemProperties(src));
  }

}
