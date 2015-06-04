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
 * A utility class to handle special characters in XML documents.
 * 
 * @author dirk
 */
public class XmlNormalizer {
  /**
   * @return the text where all special chars have been replaced by
   *         corresponding XML entities.
   */
  public static String normalizeString(String s) {
    StringBuffer str = new StringBuffer();
    int len = (s != null) ? s.length() : 0;
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      switch (ch) {
      case '<': {
        str.append("&lt;");
        break;
      }
      case '>': {
        str.append("&gt;");
        break;
      }
      case '&': {
        str.append("&amp;");
        break;
      }
      case '"': {
        str.append("&quot;");
        break;
      }
      default: {
        if (ch >= 127)
          str.append("&#" + (int) ch + ";");
        else
          str.append(ch);
      }
      }
    }
    return (str.toString());
  }
}