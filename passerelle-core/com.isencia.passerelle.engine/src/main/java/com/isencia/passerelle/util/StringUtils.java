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
package com.isencia.passerelle.util;

/**
 * StringUtils
 * 
 * @author erwin
 */
public class StringUtils {

	/**
	 * Return the original string if it is shorter than
	 * maxLength, 
	 * otherwise return a substring with length maxLength.
	 * 
	 * @param s
	 * @param maxLength
	 * @return
	 */
	public static String chop(String s, int maxLength) {
		if(s==null)
			return null;
		
		if(maxLength<=s.length())
			return s;
		
		return s.substring(0,maxLength-1)+" ...";
	}
}
