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
package com.isencia.passerelle.validation;

import junit.framework.TestCase;
import com.isencia.passerelle.validation.version.CodeVersionSpecification;
import com.isencia.passerelle.validation.version.ThreeDigitVersionSpecification;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class VersionSpecificationTest extends TestCase {

  public void testParseAndToString3Digit() {
    String VERSION_SPEC = "1.2.3_hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertNotNull("Version should parse correctly and be not-null",version);
    assertTrue("Version should parse as 3-digit version",(version instanceof ThreeDigitVersionSpecification));
    assertEquals("Version should have original string specification", VERSION_SPEC, version.toString());
  }

  public void testParseAndToStringCode() {
    String VERSION_SPEC = "hello-world";
    VersionSpecification version = VersionSpecification.parse(VERSION_SPEC);
    assertNotNull("Version should parse correctly and be not-null",version);
    assertTrue("Version should parse as code version",(version instanceof CodeVersionSpecification));
    assertEquals("Version should have original string specification", VERSION_SPEC, version.toString());
  }

  public void testParseAndMajor3Digit() {
    String VERSION_SPEC = "1.2.3_hello-world";
    ThreeDigitVersionSpecification version = (ThreeDigitVersionSpecification) VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version major not correctly parsed", 1, version.getMajor());
  }

  public void testParseAndMinor3Digit() {
    String VERSION_SPEC = "1.2.3_hello-world";
    ThreeDigitVersionSpecification version = (ThreeDigitVersionSpecification) VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version minor not correctly parsed", 2, version.getMinor());
  }

  public void testParseAndMicro3Digit() {
    String VERSION_SPEC = "1.2.3_hello-world";
    ThreeDigitVersionSpecification version = (ThreeDigitVersionSpecification) VersionSpecification.parse(VERSION_SPEC);
    assertEquals("Version micro not correctly parsed", 3, version.getMicro());
  }

  public void testEquality3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.3_hello-world");
    
    assertEquals(version1, version2);
  }

  public void testEqualityCode() {
    VersionSpecification version1 = VersionSpecification.parse("hello-world");
    VersionSpecification version2 = VersionSpecification.parse("hello-world");
    
    assertEquals(version1, version2);
  }

  public void testNotEqual3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("2.2.3");
    
    assertFalse("1.2.3_hello-world should not be equal to 2.2.3", version1.equals(version2));
  }

  public void testCompareMajorDiff3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("2.2.3");
    
    assertTrue("1.2.3_hello-world must be smaller than 2.2.3", version1.compareTo(version2)<0);
  }

  public void testCompareMinorDiff3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.3.3");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.3.3", version1.compareTo(version2)<0);
  }

  public void testCompareMicroDiff3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.4");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.2.4", version1.compareTo(version2)<0);
  }

  public void testCompareQualifierDiff3Digit() {
    VersionSpecification version1 = VersionSpecification.parse("1.2.3_hello-world");
    VersionSpecification version2 = VersionSpecification.parse("1.2.3_hello-world2");
    
    assertTrue("1.2.3_hello-world must be smaller than 1.2.3_hello-world2", version1.compareTo(version2)<0);
  }

  public void testCompareCodeDiff() {
    VersionSpecification version1 = VersionSpecification.parse("hello-world");
    VersionSpecification version2 = VersionSpecification.parse("hello-world2");
    
    assertTrue("hello-world must be smaller than hello-world2", version1.compareTo(version2)<0);
  }
}
