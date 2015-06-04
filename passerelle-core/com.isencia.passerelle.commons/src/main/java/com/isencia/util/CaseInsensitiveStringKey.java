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

/**
 * Wrapper class to use Strings as case insensitive keys in java.util.Map.
 * Simple implementation based on converting the initial string value to all
 * upper case. This implies that the hashCode() and equals() comparisons will
 * work fine between two CaseInsensitiveStringKeys but are not guaranteed to
 * work intuitively for comparing with a plain (case sensitive) StringKey.
 * 
 * @author erwin
 */
public class CaseInsensitiveStringKey extends StringKey {
  /**
   * Calculates once the hash code of the given String value after converting it
   * to all uppercase.
   */
  public CaseInsensitiveStringKey(String aValue) {
    super(aValue.toUpperCase());
  }

  /**
   * Checks whether the two StringKey objects have values that are equal
   * ignoring uppercase/lowercase differences.
   * 
   * @param obj the Object to compare with
   * @return true if these Objects are equal; false otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    else if (obj == null || !StringKey.class.isInstance(obj)) return false;

    StringKey other = (StringKey) obj;
    return (value.equalsIgnoreCase(other.value));
  }
}