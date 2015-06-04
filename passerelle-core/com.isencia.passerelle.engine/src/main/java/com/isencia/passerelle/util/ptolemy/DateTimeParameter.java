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
package com.isencia.passerelle.util.ptolemy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * DateTimeParameter
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class DateTimeParameter extends StringParameter {

	private static final long serialVersionUID = -7023867564820251370L;

  private final static Logger logger = LoggerFactory.getLogger(DateTimeParameter.class);
  private final static DateFormat format1 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
  private final static DateFormat format2 = new SimpleDateFormat("dd MMM yyyy");

  private DateFormat format;

  /**
   * 
   * @param container
   * @param name
   * @param format
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DateTimeParameter(NamedObj container, String name, DateFormat format) throws IllegalActionException, NameDuplicationException {
    this(container, name);
    this.format = format;
  }
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public DateTimeParameter(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  public void setDateValue(Date value) {
    if(value!=null) {
      if (format != null) {
        setExpression(format.format(value));
      } else {
        setExpression(format1.format(value));
      }
    } else {
      setExpression("");
    }
  }
  public Date getDateValue() {

    if (format != null) {
      try {
        return format.parse(stringValue());
      } catch (IllegalActionException e) {
        logger.error("", e);
        return null;
      } catch (ParseException e) {
        return getDefaultDateValue();
      }
    }

    return getDefaultDateValue();
  }

  /**
   * 
   * @return Date based on one of the default DateFormats defined here(format1, format2..)
   */
  private Date getDefaultDateValue() {
    try {
      return format1.parse(stringValue());
    } catch (IllegalActionException e) {
      logger.error("",e);
      return null;
    } catch (ParseException e) {
      try {
        return format2.parse(stringValue());
      } catch (IllegalActionException ex) {
        logger.error("",ex);
        return null;
      } catch (ParseException ex) {
        return null;
      }
    }
  }
}
