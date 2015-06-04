/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.repository;

import java.util.Arrays;

/**
 * A simple implementation of a version specification, based on a merge of OSGi-conventions and Ptolemy (which in turn seems to be based on JNLP).
 * <p>
 * Concretely, this means :
 * <ul>
 * <li>Reuse concept of numeric (int) major.minor.micro version number with optional trailing string-based qualifier</li>
 * <li>Don't care about the concrete concatenated format, possible delimiters etc. (i.e. do not enforce the usage of "."-separated version formatting)</li>
 * <li>Allow an arbitrary count of qualifiers</li>
 * <li>Compare qualifiers using plain text-compare</li>
 * </ul>
 * </p>
 * 
 * @author erwin
 */
public class ThreeDigitVersionSpecification extends VersionSpecification implements Comparable<VersionSpecification> {

  private int major;
  private int minor;
  private int micro;
  private String[] qualifiers = new String[0];

  /**
   * @param major
   * @param minor
   * @param micro
   * @param qualifiers
   */
  public ThreeDigitVersionSpecification(int major, int minor, int micro, String... qualifiers) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
    if (qualifiers != null) {
      this.qualifiers = qualifiers;
    }
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getMicro() {
    return micro;
  }

  public String[] getQualifiers() {
    return Arrays.copyOf(qualifiers, qualifiers.length);
  }
  
  public ThreeDigitVersionSpecification increaseMicro() {
    return new ThreeDigitVersionSpecification(major, minor, micro+1);
  }

  public ThreeDigitVersionSpecification increaseMinor() {
    return new ThreeDigitVersionSpecification(major, minor+1, micro);
  }

  public ThreeDigitVersionSpecification increaseMajor() {
    return new ThreeDigitVersionSpecification(major+1, minor, micro);
  }

  public int compareTo(VersionSpecification otherVersSpec) {
    if (otherVersSpec == this)
      return 0;
    if (otherVersSpec instanceof ThreeDigitVersionSpecification) {
      ThreeDigitVersionSpecification other = (ThreeDigitVersionSpecification) otherVersSpec;
      int result = major - other.major;
      if (result != 0)
        return result;
      result = minor - other.minor;
      if (result != 0)
        return result;
      result = micro - other.micro;
      if (result != 0)
        return result;
      else if (qualifiers.length > 0) {
        if (other.qualifiers.length > 0) {
          int maxQualifierCount = Math.max(qualifiers.length, other.qualifiers.length);
          for (int i = 0; i < maxQualifierCount; ++i) {
            String myQualifier = "";
            String otherQualifier = "";
            if (i < qualifiers.length) {
              myQualifier = qualifiers[i];
            }
            if (i < other.qualifiers.length) {
              otherQualifier = other.qualifiers[i];
            }
            int cmp = myQualifier.compareTo(otherQualifier);
            if (cmp > 0) {
              return 1;
            } else if (cmp < 0) {
              return -1;
            }
          }
          return 0;
        } else {
          return 1;
        }
      } else if (other.qualifiers.length > 0) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return this.versionString.compareTo(otherVersSpec.versionString);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + major;
    result = prime * result + micro;
    result = prime * result + minor;
    result = prime * result + qualifiers.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ThreeDigitVersionSpecification other = (ThreeDigitVersionSpecification) obj;
    if (major != other.major)
      return false;
    if (micro != other.micro)
      return false;
    if (minor != other.minor)
      return false;
    if (!Arrays.equals(qualifiers,other.qualifiers))
      return false;
    return true;
  }

  /**
   * Produces a string representation that is itself valid again to be parsed as a VersionSpecification.
   */
  @Override
  public String toString() {
    if (versionString == null) {
      StringBuilder versionStrBldr = new StringBuilder(major + "." + minor + "." + micro);
      for (String qualifier : qualifiers) {
        versionStrBldr.append("-" + qualifier);
      }
      versionString = versionStrBldr.toString();
    }
    return versionString;
  }
}
