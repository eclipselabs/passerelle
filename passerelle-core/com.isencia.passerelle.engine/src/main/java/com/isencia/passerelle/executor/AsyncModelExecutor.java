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

package com.isencia.passerelle.executor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;

/**
 * The AsyncModelExecutor acts as a Basic Executor that can be used as a
 * Runnable in a separate Thread.
 */
public class AsyncModelExecutor implements Runnable {
  private static Logger logger = LoggerFactory.getLogger(AsyncModelExecutor.class);

  private String[] args;
  private FlowManager flowManager;

  public AsyncModelExecutor(String[] args) {
    this.args = args;
    flowManager = new FlowManager();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    logger.debug("Passerelle started");

    AsyncModelExecutor executor = new AsyncModelExecutor(args);
    executor.run();
    logger.debug("Passerelle stopped");
  }

  /**
   * DOCUMENT ME!
   */
  public void run() {
    try {
      File file = new File(args[0]);

      if ((file == null) || !file.exists()) {
        throw new IllegalArgumentException("File " + file.getAbsolutePath() + " not found, execution failed ");
      }

      if (!file.getName().endsWith(".moml") && !file.getName().endsWith(".xml")) {
        throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not a moml file, execution failed ");
      }

      Flow flow = FlowManager.readMoml(file.toURI().toURL());
      Map<String, String> props = new HashMap<String, String>();
      for (int i = 1; i < args.length; i++) {
        String arg = args[i];
        String[] parts = arg.split("=");
        if (parts.length != 2) {
          throw new Exception("Invalid parameter override definition " + arg);
        } else {
          props.put(parts[0], parts[1]);
        }
      }
      flowManager.executeBlockingLocally(flow, props);

    } catch (Exception ex) {
      logger.error("Execution failed", ex);
    }
  }
}