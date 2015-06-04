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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateHandler TODO: class comment
 * 
 * @author erwin
 */
public class DateHandler {
  // Static constants for the date format
  public static final String DATE_SEPARATOR = "/";
  public static final String DATE_FMT = "yyyy" + DATE_SEPARATOR + "MM" + DATE_SEPARATOR + "dd";
  public static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FMT);

  // Static constants for the time format
  public static final String TIME_SEPARATOR = ":";
  public static final String TIME_FMT = "HH" + TIME_SEPARATOR + "mm" + TIME_SEPARATOR + "ss";
  public static final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FMT);

  // Static constants for the date format and the seperators
  public static final String TIMESTAMP_FMT = DATE_FMT + " " + TIME_FMT;

  public static final SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FMT);

  /**
   * DateHandler constructor comment.
   */
  public DateHandler() {
    super();
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:31:49 AM)
   * 
   * @return java.util.Date
   * @param s java.lang.String
   */
  public static java.util.Date parseDateString(String s) throws InvalidDateFormatException {

    try {
      java.util.Date d = dateFormat.parse(s);
      return d;
    } catch (ParseException e) {
      throw new InvalidDateFormatException(s);
    }
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:31:49 AM)
   * 
   * @return java.util.Date
   * @param s java.lang.String
   */
  public static java.util.Date parseTimeStampString(String s) throws InvalidDateFormatException {

    try {
      java.util.Date d = timeStampFormat.parse(s);
      return d;
    } catch (ParseException e) {
      throw new InvalidDateFormatException(s);
    }
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:31:49 AM)
   * 
   * @return java.util.Date
   * @param s java.lang.String
   */
  public static java.util.Date parseTimeString(String s) throws InvalidDateFormatException {

    try {
      java.util.Date d = timeFormat.parse(s);
      return d;
    } catch (ParseException e) {
      throw new InvalidDateFormatException(s);
    }
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:29:22 AM)
   * 
   * @return java.lang.String
   * @param d be.tuple.xml.Date
   */
  public static String toDateString(Date d) {
    return dateFormat.format(d);
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:29:22 AM)
   * 
   * @return java.lang.String
   * @param d be.tuple.xml.Date
   */
  public static String toTimeStampString(Date d) {
    return timeStampFormat.format(d);
  }

  /**
   * Insert the method's description here. Creation date: (2/26/01 10:29:22 AM)
   * 
   * @return java.lang.String
   * @param d be.tuple.xml.Date
   */
  public static String toTimeString(Date d) {
    return timeFormat.format(d);
  }
}
