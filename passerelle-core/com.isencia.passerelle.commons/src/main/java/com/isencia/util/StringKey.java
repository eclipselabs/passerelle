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
 * Wrapper class to optimize the use of Strings as keys in java.util.Map.
 * 
 * @author erwin
 */
public class StringKey {
  protected int hashCode = 0;

  protected String value = null;

  /**
   * StringKey constructor. Calculates once the hash code of the given String
   * value.
   */
  public StringKey(String aValue) {
    super();
    value = aValue;
    if (value != null) {
      hashCode = value.hashCode();
    } else {
      hashCode = 0;
    }
  }

  /**
   * Compares two objects for equality. Returns a boolean that indicates whether
   * this object is equivalent to the specified object. This method is often
   * used when an object is stored in a hashtable, using a StringKey as the
   * map's key.
   * 
   * @param obj the Object to compare with
   * @return true if these Objects are equal; false otherwise.
   * @see java.util.Hashtable
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    else if (obj == null || !StringKey.class.isInstance(obj)) return false;

    StringKey other = (StringKey) obj;
    return (value.equals(other.value));
  }

  /**
   * Generates a hash code for the receiver. This method is supported primarily
   * for hash tables, such as those provided in java.util.
   * 
   * @return an integer hash code for the receiver
   * @see java.util.Hashtable
   */
  public int hashCode() {
    return hashCode;
  }

  /**
   * Returns a String that represents the value of this object.
   * 
   * @return a string representation of the receiver
   */
  public String toString() {
    return value;
  }
}