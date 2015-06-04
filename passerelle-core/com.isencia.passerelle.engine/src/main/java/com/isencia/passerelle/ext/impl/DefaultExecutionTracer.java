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
package com.isencia.passerelle.ext.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.domain.cap.ProcessThread;
import com.isencia.passerelle.ext.ExecutionTracer;
import com.isencia.passerelle.util.ExecutionTracerService;
import com.isencia.passerelle.util.Level;

/**
 * Default implementation of an execution tracer, which just logs the traces to a "trace" logger, on the INFO level.
 * 
 * Depending on the execution environment, different results can be obtained by modifying the logging properties. E.g.
 * trace output can be sent to a log file, or to a database etc.
 * 
 * <code>ExecutionTracers</code> are typically used through the {@link ExecutionTracerService}. In order to ensure only
 * one instance of a {@link DefaultExecutionTracer} is active inside the {@link ExecutionTracerService}, the
 * <code>equals()</code> and <code>hashCode()</code> methods have been defined to just check on the class name being
 * equal.
 * 
 * @author erwin
 * 
 */
public class DefaultExecutionTracer implements ExecutionTracer {
  private final static Logger logger = LoggerFactory.getLogger("trace");

  public void trace(Actor source, String message, Level level) {
    MDC.put(ProcessThread.ACTOR_MDC_NAME, getFullNameButWithoutModelName((NamedObj) source));
    logMessage(message, level);
  }

  private void logMessage(String message, Level level) {
    if (level.equals(Level.ERROR)) {
      logger.error(message);
    } else if (level.equals(Level.INFO)) {
      logger.info(message);
    } else if (level.equals(Level.WARN)) {
      logger.warn(message);
    } else if (level.equals(Level.DEBUG)) {
      logger.debug(message);
    }
  }

  public void trace(Director source, String message, Level level) {
    MDC.put(ProcessThread.ACTOR_MDC_NAME, source.getName());
    logMessage(message, level);
  }

  public void trace(Actor source, String message) {
    trace(source,message,Level.INFO);
  }

  public void trace(Director source, String message) {
    trace(source,message,Level.INFO);
  }

  /**
   * returns true if obj is also an instance of DefaultExecutionTracer (not of a subclass)
   */
  @Override
  public boolean equals(Object obj) {
    return (obj != null && obj.getClass().equals(this.getClass()));
  }

  @Override
  public int hashCode() {
    return this.getClass().getName().hashCode();
  }

  public static String getFullNameButWithoutModelName(NamedObj actor) {
    // the first string is the name of the model
    String fullName = actor.getFullName();
    int i = fullName.indexOf(".", 1);
    if (i > 0) {
      // there's always an extra '.' in front of the model name...
      // and a trailing '.' just behind it...
      fullName = fullName.substring(i + 1);
    }

    return fullName;
  }
}
