/* Copyright 2014 - iSencia Belgium NV

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
package com.isencia.passerelle.testsupport;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Assert;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.util.FutureValue;

/**
 * Utilities for more advanced unit tests, e.g. involving high counts of repeated model executions
 * sequentially or concurrently.
 * 
 * @author erwindl
 *
 */
public class FlowExecutionTester {
  
  /**
   * Execute a flow a number of times, one-after-the-other, and assert the results each time using the given assertion.
   * <p>
   * For each execution, a new instance of the Flow is constructed, using the given builder.
   * The flow gets as name the given flowName + the run's index as postfix.
   * The actors in the flow will be configured using the given paramOverrides map.
   * </p>
   * @param count
   * @param flowName
   * @param builder
   * @param paramOverrides a map between full names of selected model parameters, and their desired values 
   * @param assertion
   * @throws Exception
   */
  public static void runFlowSequentially(int count, String flowName, FlowBuilder builder, Map<String, String> paramOverrides, FlowStatisticsAssertion assertion) throws Exception {
    FlowManager flowManager = new FlowManager();
    for (int i = 0; i < 100; ++i) {
      final Flow flow = builder.buildFlow(flowName + "_" + i);
      final FutureValue<Boolean> modelFinished = new FutureValue<Boolean>();
      flowManager.execute(flow, paramOverrides, new ExecutionListener() {
        public void managerStateChanged(Manager manager) {
        }
        public void executionFinished(Manager manager) {
          modelFinished.set(Boolean.TRUE);
        }
        public void executionError(Manager manager, Throwable throwable) {
          modelFinished.set(Boolean.FALSE);
        }
      });
      try {
        modelFinished.get(5, TimeUnit.SECONDS);
//        System.out.println("run [" + i + "] finished");
        // now check if all went as expected
        // remark that in case of a deadlock in the ptolemy Manager error reporting
        // (combined with flow component changes inside our listener, e.g. flow.setManager(null))
        // the future above may return, but the assertions below may be blocked in the deadlock as well!
        assertion.assertFlow(flow);
      } catch (TimeoutException e) {
        Assert.fail("Flow execution timed out, probable deadlock in "+flow.getName());
      }
    }
  }

  /**
   * Execute a flow a number of times, concurrently, and assert the results each time using the given assertion.
   * <p>
   * For each execution, a new instance of the Flow is constructed, using the given builder.
   * The flow gets as name the given flowName + the run's index as postfix.
   * The actors in the flow will be configured using the given paramOverrides map.
   * </p>
   * @param count
   * @param flowName
   * @param builder
   * @param paramOverrides a map between full names of selected model parameters, and their desired values 
   * @param assertion
   * @throws Exception
   */
  public static void runFlowConcurrently(int count, String flowName, FlowBuilder builder, Map<String, String> paramOverrides, FlowStatisticsAssertion assertion) throws Exception {
    FlowManager flowManager = new FlowManager();
    Set<FutureValue<Flow>> modelFinishedFutures = new HashSet<FutureValue<Flow>>();
    for (int i = 0; i < 100; ++i) {
      final Flow flow = builder.buildFlow(flowName+"_"+i);
      final FutureValue<Flow> modelFinished = new FutureValue<Flow>();
      modelFinishedFutures.add(modelFinished);
      flowManager.execute(flow, paramOverrides, new ExecutionListener() {
        public void managerStateChanged(Manager manager) {
        }
        public void executionFinished(Manager manager) {
          modelFinished.set(flow);
        }
        public void executionError(Manager manager, Throwable throwable) {
          modelFinished.set(flow);
        }
      });
    }
    for(FutureValue<Flow> modelFinished : modelFinishedFutures) {
      try {
        Flow flow = modelFinished.get(5, TimeUnit.SECONDS);
//        System.out.println("run [" + flow.getName() + "] finished");
        // now check if all went as expected
        // remark that in case of a deadlock in the ptolemy Manager error reporting
        // (combined with flow component changes inside our listener, e.g. flow.setManager(null))
        // the future above may return, but the assertions below may be blocked in the deadlock as well!
        assertion.assertFlow(flow);
      } catch (TimeoutException e) {
        Assert.fail("Flow execution timed out, probable deadlock");
      }
    }
  }
}
