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


/**
 * A simple implementation of a version specification, based on a single textual code.
 * 
 * @author erwin
 */
public class CodeVersionSpecification extends VersionSpecification implements Comparable<VersionSpecification> {

  /**
   * @param major
   * @param minor
   * @param micro
   * @param qualifiers
   */
  public CodeVersionSpecification(String versionCode) {
    this.versionString = versionCode;
  }
  

  public int compareTo(VersionSpecification other) {
    if (other == this)
      return 0;
    return this.versionString.compareTo(other.versionString);
  }

  @Override
  public int hashCode() {
    return versionString.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CodeVersionSpecification other = (CodeVersionSpecification) obj;
    if (versionString != other.versionString)
      return false;
    return true;
  }

  /**
   * Produces a string representation that is itself valid again to be parsed
   * as a VersionSpecification.
   */
  @Override
  public String toString() {
    return versionString;
  }
}
