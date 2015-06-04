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
 * StringConvertor Utility class for String processing
 * 
 * @author dirk
 */
public class StringConvertor {

  /**
   * This method returns the input string with the first character converted to
   * lower case.
   * 
   * @param string java.lang.String
   * @return Boolean
   */
  public static String firstCharToLowerCase(String string) {
    char[] ch = string.toCharArray();
    ch[0] = Character.toLowerCase(ch[0]);

    return new String(ch);
  }

  /**
   * Replaces forward\backward slashes in a string with the desired path
   * separator character for the current platform.
   * 
   * @return java.lang.String
   * @param aString java.lang.String
   */
  public static String convertPathDelimiters(String aString) {
    String tmpStr = new String(aString);

    if (java.io.File.separatorChar != '/') {
      tmpStr = tmpStr.replace('/', java.io.File.separatorChar);
    } else {
      tmpStr = tmpStr.replace('\\', java.io.File.separatorChar);
    }

    return tmpStr;
  }

  /**
   * This method returns the input string with the first character converted to
   * upper case.
   * 
   * @param string java.lang.String
   * @return Boolean
   */
  public static String firstCharToUpperCase(String string) {
    char[] ch = string.toCharArray();
    ch[0] = Character.toUpperCase(ch[0]);

    return new String(ch);
  }

  /**
   * This method returns a corresponding boolean type depending on the input
   * string (e.g. true for 'Y' and false null).
   * 
   * @param a String
   * @return Boolean
   */
  public static Boolean stringToBoolean(String a) {
    char c;

    if (a == null || a.length() == 0) return new Boolean(false);
    if (a.equalsIgnoreCase("on"))
      return new Boolean(true);
    else if (a.equalsIgnoreCase("off")) return new Boolean(false);

    c = a.charAt(0);
    return new Boolean(c == 'y' || c == 'Y' || c == '1' || c == 't' || c == 'T');
  }

  /**
   * Looks for declarations of system properties, and replaces them with their
   * value. All sequences ${any.sys.prop.name} that are encountered, are
   * replaced by the value for the system property with the respective name.
   * 
   * @return java.lang.String
   * @param aString java.lang.String
   */
  public static String substituteSystemProperties(String aString) {
    if (aString == null) {
      return null;
    }
    java.util.StringTokenizer st = new java.util.StringTokenizer(aString, "$", true);
    StringBuffer tmpStrB = new StringBuffer(aString);
    int substrStart = 0;
    int substrEnd = 0;

    try {
      while (st.hasMoreTokens()) {
        String t = st.nextToken("$").trim();
        if (t.equals("$")) {
          st.nextToken("{");
          String sysPropName = st.nextToken("}").trim();
          String sysPropValue = System.getProperty(sysPropName);
          substrStart = tmpStrB.toString().indexOf("${" + sysPropName + "}", substrEnd);
          // +3 for the ${ and }
          substrEnd = substrStart + sysPropName.length() + 3;

          tmpStrB = tmpStrB.replace(substrStart, substrEnd, sysPropValue);
          substrEnd = substrEnd + sysPropValue.length() - sysPropName.length() - 3;
        }
      }
    } catch (java.util.NoSuchElementException e) {
      // problem with a trailing $ without following envVarName token
      // ignore and hope for the best...
    }

    return tmpStrB.toString();
  }
}