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

import java.lang.reflect.Array;

/**
 * While waiting for JDK 1.5, this utility class supports conversions of arrays
 * to string. Based on work by Jerome Lacoste at www.javapractices.com
 * 
 * @author erwin
 */
public final class ArrayUtil {
  // PRIVATE //
  private static final String START_CHAR = "[";

  private static final String END_CHAR = "]";

  private static final String SEPARATOR = ", ";

  private static final String NULL = "null";

  /**
   * <code>aArray</code> is a possibly-null array whose elements are primitives
   * or objects; arrays of arrays are also valid, in which case
   * <code>aArray</code> is rendered in a nested, recursive fashion. The array
   * delimiter and start/end characters may be customized. When any of these is
   * null, the default value is used, i.e. </ul> <li>array delimiter (",") <li>
   * start/end characters ("[" and "]") <li>null representation ("null") </ul>
   * 
   * @param aArray
   * @param startChar
   * @param endChar
   * @param separator
   * @param nullRepresentation
   * @return
   */
  public static String toString(Object aArray, String startChar, String endChar, String separator, String nullRepresentation) {
    if (nullRepresentation == null) nullRepresentation = NULL;

    if (aArray == null) return nullRepresentation;

    checkObjectIsArray(aArray);

    if (startChar == null) startChar = START_CHAR;
    if (endChar == null) endChar = END_CHAR;
    if (separator == null) separator = SEPARATOR;

    StringBuffer result = new StringBuffer(startChar);
    int length = Array.getLength(aArray);
    for (int idx = 0; idx < length; ++idx) {
      Object item = Array.get(aArray, idx);
      if (isNonNullArray(item)) {
        // recursive call!
        result.append(toString(item, startChar, endChar, separator, nullRepresentation));
      } else {
        result.append(item);
      }
      if (!isLastItem(idx, length)) {
        result.append(separator);
      }
    }
    result.append(endChar);
    return result.toString();
  }

  /**
   * <code>aArray</code> is a possibly-null array whose elements are primitives
   * or objects; arrays of arrays are also valid, in which case
   * <code>aArray</code> is rendered in a nested, recursive fashion. The default
   * array delimiter (",") and start/end characters ("[" and "]") are used,
   * similar to what <code>AbstractCollection.toString()</code> does.
   */
  public static String toString(Object aArray) {
    return toString(aArray, START_CHAR, END_CHAR, SEPARATOR, NULL);
  }

  private static void checkObjectIsArray(Object aArray) {
    if (!aArray.getClass().isArray()) {
      throw new IllegalArgumentException("Object is not an array.");
    }
  }

  private static boolean isNonNullArray(Object aItem) {
    return aItem != null && aItem.getClass().isArray();
  }

  private static boolean isLastItem(int aIdx, int aLength) {
    return (aIdx == aLength - 1);
  }
}
