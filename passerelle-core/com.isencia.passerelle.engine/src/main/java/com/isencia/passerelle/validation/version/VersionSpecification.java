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
package com.isencia.passerelle.validation.version;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract base class for version specifications. 
 * Most important thing for a version specification is that it can be compared to another one.
 * <p>
 * Versions can currently be specified in two formats : a simple 3-digit spec (+ qualifiers) or a code/tag.
 * </p>
 * 
 * @author erwin
 */
public abstract class VersionSpecification implements Comparable<VersionSpecification> {

  protected String versionString;

  /**
   * Parses the given version String, using '.' , '-' , '_' as potential delimiters. 
   * <p>
   * For 3-digit version spec, the first 3 version ids are mandatory and should be integer numbers. Extra
   * (optional) trailing ids can be textual. Spaces are not allowed in a version string. 
   * E.g. "1.2_3-hello.world" is a valid version identifier.
   * </p>
   * <p>
   * If the received version string does not match this format, it is assumed to be a code/tag.
   * </p>
   * 
   * @param version
   * @return
   * @throws IllegalArgumentException
   * @throws NumberFormatException
   */
  public static VersionSpecification parse(String version) {
    VersionSpecification versionSpec = null;

    String[] versionIds = version.split("[\\.\\-_]");

    if (versionIds.length < 3) {
      versionSpec = new CodeVersionSpecification(version);
    } else {
      if (version.indexOf(' ') != -1) {
        throw new IllegalArgumentException("3-digit Version can not contain spaces <" + version + ">");
      }

      int major = Integer.parseInt(versionIds[0]);
      int minor = Integer.parseInt(versionIds[1]);
      int micro = Integer.parseInt(versionIds[2]);
      if (versionIds.length == 3) {
        versionSpec = new ThreeDigitVersionSpecification(major, minor, micro);
      } else {
        // This is for JDK 1.6 onwards, but JDK 1.5 compliance is still needed, so we need to hack a sub-array logic here
        // versionSpec = new VersionSpecification(major, minor, micro, Arrays.copyOfRange(versionIds, 3, versionIds.length));
        Collection<String> tail = new ArrayList<String>();
        for (int i = 3; i < versionIds.length; ++i) {
          tail.add(versionIds[i]);
        }
        versionSpec = new ThreeDigitVersionSpecification(major, minor, micro, tail.toArray(new String[versionIds.length - 3]));
      }
      versionSpec.versionString = version;
    }
    return versionSpec;
  }
}
