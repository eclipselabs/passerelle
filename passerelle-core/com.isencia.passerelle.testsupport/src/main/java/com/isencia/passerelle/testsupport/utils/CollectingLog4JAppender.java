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
package com.isencia.passerelle.testsupport.utils;

import java.util.regex.Pattern;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A Log4J appender that can be used to count log messages matching a given pattern.
 * 
 * As Log4J manages the construction and lifecycle of appender instances,
 * our configuration and counting is done with static properties.
 * 
 * So this is not thread-safe and only reliable for single test executions!
 * 
 * @author erwin
 *
 */
public class CollectingLog4JAppender extends AppenderSkeleton {
  private static int loggedMessages;
  
  private static Pattern msgPattern;
  
  public static int getLoggedMessages() {
    return loggedMessages;
  }
  
  /**
   * Initialize the appender(s) with the given pattern.
   * I.e. it will reset its counter for logged messages,
   * and start counting messages that match the given regex pattern.
   * If pattern is null, all received log messages will be counted.
   * @param pattern
   */
  public static void initWithPattern(String pattern) {
    loggedMessages=0;
    CollectingLog4JAppender.msgPattern = pattern!=null ? Pattern.compile(pattern) : null;
  }

  @Override
  protected void append(LoggingEvent evt) {
    if(msgPattern==null || msgPattern.matcher(evt.getRenderedMessage()).matches()) {
      loggedMessages++;
    }
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

}
